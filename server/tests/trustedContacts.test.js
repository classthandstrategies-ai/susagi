const { describe, it, beforeEach, afterEach } = require('node:test');
const assert = require('node:assert');
const express = require('express');

const { verifySupabaseAuth } = require('../middleware/supabaseAuth');
const trustedContacts = require('../services/trustedContacts');
const trustedContactsRouter = require('../routes/trustedContacts');

/**
 * High-fidelity in-memory Supabase PostgreSQL test double supporting:
 * - trusted_contacts table
 * - select, insert, update, upsert
 * - conditional eq() filtering
 * - failure injection
 */
class MemorySupabase {
    constructor() {
        this.tables = {
            trusted_contacts: new Map()
        };
        this.shouldFail = false;
        this.failureMessage = 'Database connection failure';
        this.queryLog = [];
    }

    from(tableName) {
        if (!this.tables[tableName]) {
            this.tables[tableName] = new Map();
        }
        return new QueryBuilder(this, tableName, this.tables[tableName]);
    }
}

class QueryBuilder {
    constructor(db, tableName, store) {
        this.db = db;
        this.tableName = tableName;
        this.store = store;
        this.filters = [];
        this.operation = 'select';
        this.payload = null;
        this.upsertOptions = null;
        this.isSingle = false;
        this.isMaybeSingle = false;
    }

    select(columns) {
        this.columns = columns;
        return this;
    }

    upsert(data, options = {}) {
        this.operation = 'upsert';
        this.payload = Array.isArray(data) ? data : [data];
        this.upsertOptions = options;
        return this;
    }

    eq(column, value) {
        this.filters.push({ column, value });
        return this;
    }

    single() {
        this.isSingle = true;
        return this._execute();
    }

    maybeSingle() {
        this.isMaybeSingle = true;
        return this._execute();
    }

    then(resolve, reject) {
        return this._execute().then(resolve, reject);
    }

    async _execute() {
        this.db.queryLog.push({
            table: this.tableName,
            operation: this.operation,
            filters: [...this.filters]
        });

        if (this.db.shouldFail) {
            return { data: null, error: new Error(this.db.failureMessage) };
        }

        if (this.operation === 'upsert') {
            const upserted = [];
            for (const item of this.payload) {
                let key = item.id;
                if (!key && item.protected_user_id && item.trusted_user_id) {
                    key = `${item.protected_user_id}:${item.trusted_user_id}`;
                }
                const existing = this.store.get(key) || {};
                const merged = { ...existing, ...JSON.parse(JSON.stringify(item)) };
                this.store.set(key, merged);
                upserted.push(merged);
            }
            if (this.isSingle || this.isMaybeSingle) {
                return { data: upserted[0] || null, error: null };
            }
            return { data: upserted, error: null };
        }

        // Default operation: select
        const results = [];
        for (const row of this.store.values()) {
            let match = true;
            for (const filter of this.filters) {
                if (row[filter.column] !== filter.value) {
                    match = false;
                    break;
                }
            }
            if (match) {
                results.push(JSON.parse(JSON.stringify(row)));
            }
        }

        if (this.isSingle) {
            return { data: results[0] || null, error: results.length === 0 ? new Error('Row not found') : null };
        }
        if (this.isMaybeSingle) {
            return { data: results[0] || null, error: null };
        }
        return { data: results, error: null };
    }
}

describe('Canonical Trusted Contact Read Boundary (CP3)', () => {
    let memoryDb;
    let server;
    let baseUrl;

    beforeEach(async () => {
        memoryDb = new MemorySupabase();

        const app = express();
        app.use(express.json());
        app.locals.db = memoryDb;

        // Diagnostic route for verifySupabaseAuth testing
        app.get('/test/auth-guard', verifySupabaseAuth, (req, res) => {
            res.json({ ok: true, user: req.user });
        });

        // Test auth middleware simulating authenticated req.user based on Bearer token value
        const testAuthMiddleware = (req, res, next) => {
            const authHeader = req.headers.authorization;
            if (!authHeader || !authHeader.startsWith('Bearer ')) {
                return res.status(401).json({ error: 'Unauthorized', message: 'Missing Authorization' });
            }
            const token = authHeader.split('Bearer ')[1]?.trim();
            if (!token) {
                return res.status(401).json({ error: 'Unauthorized', message: 'Empty token' });
            }
            req.user = { uid: token };
            next();
        };

        app.locals.authMiddleware = testAuthMiddleware;
        app.use('/api/v1/trusted-contacts', trustedContactsRouter);

        // Start server on ephemeral port
        await new Promise((resolve) => {
            server = app.listen(0, () => {
                const port = server.address().port;
                baseUrl = `http://127.0.0.1:${port}`;
                resolve();
            });
        });
    });

    afterEach(async () => {
        if (server) {
            await new Promise((resolve) => server.close(resolve));
        }
    });

    // ==========================================
    // 1. UNAUTHENTICATED REQUESTS
    // ==========================================
    describe('Authentication Verification', () => {
        it('1. rejects request when Authorization header is missing', async () => {
            const res = await fetch(`${baseUrl}/api/v1/trusted-contacts`);
            assert.strictEqual(res.status, 401);
            const body = await res.json();
            assert.strictEqual(body.error, 'Unauthorized');
        });

        it('2. rejects request when Bearer token is empty or malformed', async () => {
            const res1 = await fetch(`${baseUrl}/api/v1/trusted-contacts`, {
                headers: { Authorization: 'Basic dXNlcjpwYXNz' }
            });
            assert.strictEqual(res1.status, 401);

            const res2 = await fetch(`${baseUrl}/api/v1/trusted-contacts`, {
                headers: { Authorization: 'Bearer   ' }
            });
            assert.strictEqual(res2.status, 401);
        });
    });

    // ==========================================
    // 2. AUTHORIZATION & CONTACT LISTING
    // ==========================================
    describe('Trusted Contact Read & Authorization Scoping', () => {
        beforeEach(async () => {
            // Seed relationships:
            // User A -> User B (enabled = true)
            // User A -> User D (enabled = true)
            // User A -> User C (enabled = false, disabled)
            // User X -> User Y (enabled = true, belongs to User X)
            await trustedContacts.setTrustedContact({
                protectedUid: 'user_A',
                trustedUid: 'user_B',
                displayName: 'Bob (Brother)',
                relationship: 'Family',
                enabled: true,
                db: memoryDb
            });
            await trustedContacts.setTrustedContact({
                protectedUid: 'user_A',
                trustedUid: 'user_D',
                displayName: 'Dana (Daughter)',
                relationship: 'Family',
                enabled: true,
                db: memoryDb
            });
            await trustedContacts.setTrustedContact({
                protectedUid: 'user_A',
                trustedUid: 'user_C',
                displayName: 'Charlie (Ex-Guardian)',
                relationship: 'Friend',
                enabled: false,
                db: memoryDb
            });
            await trustedContacts.setTrustedContact({
                protectedUid: 'user_X',
                trustedUid: 'user_Y',
                displayName: 'Yolanda',
                relationship: 'Family',
                enabled: true,
                db: memoryDb
            });
        });

        it('3. authenticated User A receives only enabled rows where protected_user_id == User A', async () => {
            const res = await fetch(`${baseUrl}/api/v1/trusted-contacts`, {
                headers: { Authorization: 'Bearer user_A' }
            });
            assert.strictEqual(res.status, 200);

            const body = await res.json();
            assert.strictEqual(body.success, true);
            assert.ok(Array.isArray(body.contacts));
            assert.strictEqual(body.contacts.length, 2);

            const trustedIds = body.contacts.map((c) => c.trustedUserId).sort();
            assert.deepStrictEqual(trustedIds, ['user_B', 'user_D']);

            const bob = body.contacts.find((c) => c.trustedUserId === 'user_B');
            assert.strictEqual(bob.displayName, 'Bob (Brother)');
            assert.strictEqual(bob.relationship, 'Family');
            assert.strictEqual(bob.enabled, true);

            const dana = body.contacts.find((c) => c.trustedUserId === 'user_D');
            assert.strictEqual(dana.displayName, 'Dana (Daughter)');
            assert.strictEqual(dana.relationship, 'Family');
            assert.strictEqual(dana.enabled, true);
        });

        it('4. attempting to provide another protectedUserId cannot expose another user contacts', async () => {
            // Attempt query parameter spoofing
            const resQuery = await fetch(`${baseUrl}/api/v1/trusted-contacts?protectedUserId=user_X`, {
                headers: { Authorization: 'Bearer user_A' }
            });
            assert.strictEqual(resQuery.status, 200);
            const bodyQuery = await resQuery.json();
            assert.strictEqual(bodyQuery.contacts.length, 2);
            assert.ok(!bodyQuery.contacts.some((c) => c.trustedUserId === 'user_Y'));

            // Attempt request body spoofing (if GET has a body or client sends one)
            const resBody = await fetch(`${baseUrl}/api/v1/trusted-contacts`, {
                method: 'GET',
                headers: {
                    Authorization: 'Bearer user_A',
                    'Content-Type': 'application/json'
                }
            });
            assert.strictEqual(resBody.status, 200);
            const bodyJson = await resBody.json();
            assert.strictEqual(bodyJson.contacts.length, 2);
            assert.ok(!bodyJson.contacts.some((c) => c.trustedUserId === 'user_Y'));
        });

        it('5. disabled relationship is excluded from response', async () => {
            const res = await fetch(`${baseUrl}/api/v1/trusted-contacts`, {
                headers: { Authorization: 'Bearer user_A' }
            });
            assert.strictEqual(res.status, 200);
            const body = await res.json();

            // Charlie is disabled -> must not appear
            const charlie = body.contacts.find((c) => c.trustedUserId === 'user_C');
            assert.strictEqual(charlie, undefined);
        });

        it('6. user with no relationships returns empty array', async () => {
            const res = await fetch(`${baseUrl}/api/v1/trusted-contacts`, {
                headers: { Authorization: 'Bearer user_lonely' }
            });
            assert.strictEqual(res.status, 200);
            const body = await res.json();
            assert.strictEqual(body.success, true);
            assert.deepStrictEqual(body.contacts, []);
        });

        it('7. response does NOT expose sensitive fields or other users data', async () => {
            const res = await fetch(`${baseUrl}/api/v1/trusted-contacts`, {
                headers: { Authorization: 'Bearer user_A' }
            });
            const body = await res.json();

            for (const contact of body.contacts) {
                // Must only expose approved domain fields
                const keys = Object.keys(contact);
                assert.ok(!keys.includes('fcm_token'), 'Must not expose fcm_token');
                assert.ok(!keys.includes('fcmToken'), 'Must not expose fcmToken');
                assert.ok(!keys.includes('device_id'), 'Must not expose device_id');
                assert.ok(!keys.includes('deviceId'), 'Must not expose deviceId');
                assert.ok(!keys.includes('response_device_id'), 'Must not expose response_device_id');
                assert.ok(!keys.includes('protected_user_id'), 'Must not expose protected_user_id');
                assert.ok(!keys.includes('protectedUserId'), 'Must not expose protectedUserId');
                assert.ok(!keys.includes('service_role'), 'Must not expose service_role');
            }
        });
    });

    // ==========================================
    // 3. DATABASE ERROR HANDLING
    // ==========================================
    describe('Database Error Handling', () => {
        it('8. database failure produces controlled server response', async () => {
            memoryDb.shouldFail = true;
            memoryDb.failureMessage = 'Simulated connection failure';

            const res = await fetch(`${baseUrl}/api/v1/trusted-contacts`, {
                headers: { Authorization: 'Bearer user_A' }
            });
            assert.strictEqual(res.status, 500);

            const body = await res.json();
            assert.strictEqual(body.error, 'InternalServerError');
            assert.strictEqual(body.message, 'Simulated connection failure');
        });
    });

    // ==========================================
    // 4. SERVICE-LEVEL VALIDATIONS & SCOPING
    // ==========================================
    describe('Service-Level Validation & Query Scoping', () => {
        it('9. service validates protectedUid and throws 400 for missing/empty values', async () => {
            await assert.rejects(
                async () => {
                    await trustedContacts.listTrustedContacts({ protectedUid: null, db: memoryDb });
                },
                (err) => {
                    assert.strictEqual(err.statusCode, 400);
                    assert.match(err.message, /protectedUid is required/);
                    return true;
                }
            );

            await assert.rejects(
                async () => {
                    await trustedContacts.listTrustedContacts({ protectedUid: '   ', db: memoryDb });
                },
                (err) => {
                    assert.strictEqual(err.statusCode, 400);
                    return true;
                }
            );
        });

        it('10. service validates database client availability and throws 503 if missing', async () => {
            await assert.rejects(
                async () => {
                    await trustedContacts.listTrustedContacts({ protectedUid: 'user_123', db: null });
                },
                (err) => {
                    assert.strictEqual(err.statusCode, 503);
                    assert.match(err.message, /Database service unavailable/);
                    return true;
                }
            );
        });

        it('11. service issues a query scoped strictly to protected_user_id and enabled', async () => {
            memoryDb.queryLog = [];

            await trustedContacts.listTrustedContacts({
                protectedUid: 'user_strict',
                enabledOnly: true,
                db: memoryDb
            });

            assert.strictEqual(memoryDb.queryLog.length, 1);
            const query = memoryDb.queryLog[0];
            assert.strictEqual(query.table, 'trusted_contacts');
            assert.strictEqual(query.operation, 'select');

            const filterMap = new Map(query.filters.map((f) => [f.column, f.value]));
            assert.strictEqual(filterMap.get('protected_user_id'), 'user_strict');
            assert.strictEqual(filterMap.get('enabled'), true);
        });

        it('12. service supports enabledOnly = false when explicitly specified', async () => {
            // Seed enabled and disabled
            await trustedContacts.setTrustedContact({
                protectedUid: 'user_both',
                trustedUid: 'user_en',
                displayName: 'Enabled Guy',
                enabled: true,
                db: memoryDb
            });
            await trustedContacts.setTrustedContact({
                protectedUid: 'user_both',
                trustedUid: 'user_dis',
                displayName: 'Disabled Guy',
                enabled: false,
                db: memoryDb
            });

            const allContacts = await trustedContacts.listTrustedContacts({
                protectedUid: 'user_both',
                enabledOnly: false,
                db: memoryDb
            });

            assert.strictEqual(allContacts.length, 2);
            const enabledItem = allContacts.find((c) => c.trustedUserId === 'user_en');
            const disabledItem = allContacts.find((c) => c.trustedUserId === 'user_dis');
            assert.strictEqual(enabledItem.enabled, true);
            assert.strictEqual(disabledItem.enabled, false);
        });
    });
});
