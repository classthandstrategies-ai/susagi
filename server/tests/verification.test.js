const { describe, it, beforeEach, afterEach } = require('node:test');
const assert = require('node:assert');
const express = require('express');

const { verifySupabaseAuth } = require('../middleware/supabaseAuth');
const deviceRegistry = require('../services/deviceRegistry');
const trustedContacts = require('../services/trustedContacts');
const verificationService = require('../services/verificationService');
const devicesRouter = require('../routes/devices');
const verificationsRouter = require('../routes/verifications');

/**
 * High-fidelity in-memory Supabase PostgreSQL test double supporting:
 * - tables: devices, trusted_contacts, verification_sessions
 * - select, insert, update, upsert
 * - conditional eq() filtering with atomic compare-and-set semantics
 * - simulated latency to verify concurrent race conditions
 */
class MemorySupabase {
    constructor() {
        this.tables = {
            devices: new Map(),
            trusted_contacts: new Map(),
            verification_sessions: new Map()
        };
        // Simulated latency delay in ms for concurrency race simulation
        this.simulatedLatencyMs = 0;
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

    select() {
        if (this.operation === 'select') {
            // normal select
        }
        // If chaining after update/insert/upsert, keep current operation
        return this;
    }

    insert(data) {
        this.operation = 'insert';
        this.payload = Array.isArray(data) ? data : [data];
        return this;
    }

    update(fields) {
        this.operation = 'update';
        this.payload = fields;
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
        if (this.db.simulatedLatencyMs > 0) {
            await new Promise((r) => setTimeout(r, this.db.simulatedLatencyMs));
        }

        if (this.operation === 'insert') {
            const inserted = [];
            for (const item of this.payload) {
                const key = item.id || `${item.user_id}:${item.device_id}` || `${item.protected_user_id}:${item.trusted_user_id}`;
                const copy = JSON.parse(JSON.stringify(item));
                this.store.set(key, copy);
                inserted.push(copy);
            }
            if (this.isSingle || this.isMaybeSingle) {
                return { data: inserted[0] || null, error: null };
            }
            return { data: inserted, error: null };
        }

        if (this.operation === 'upsert') {
            const upserted = [];
            for (const item of this.payload) {
                let key = item.id;
                if (!key) {
                    if (item.user_id && item.device_id) {
                        key = `${item.user_id}:${item.device_id}`;
                    } else if (item.protected_user_id && item.trusted_user_id) {
                        key = `${item.protected_user_id}:${item.trusted_user_id}`;
                    }
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

        if (this.operation === 'update') {
            // Atomic filter matching
            const matchingEntries = [];
            for (const [key, row] of this.store.entries()) {
                let match = true;
                for (const filter of this.filters) {
                    if (row[filter.column] !== filter.value) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    matchingEntries.push([key, row]);
                }
            }

            const updatedRows = [];
            for (const [key, row] of matchingEntries) {
                const updated = { ...row, ...JSON.parse(JSON.stringify(this.payload)) };
                this.store.set(key, updated);
                updatedRows.push(updated);
            }

            if (this.isSingle || this.isMaybeSingle) {
                return { data: updatedRows[0] || null, error: null };
            }
            return { data: updatedRows, error: null };
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

describe('Authoritative Verification Backend with Supabase (CP3)', () => {
    let memoryDb;
    let server;
    let baseUrl;

    beforeEach(async () => {
        memoryDb = new MemorySupabase();

        const app = express();
        app.use(express.json());
        app.locals.db = memoryDb;

        // Diagnostic route for auth middleware tests
        app.get('/test/auth-guard', verifySupabaseAuth, (req, res) => {
            res.json({ ok: true, user: req.user });
        });

        // Test harness router simulating authenticated req.user based on Bearer token value
        const testAuthMiddleware = (req, res, next) => {
            const authHeader = req.headers.authorization;
            if (!authHeader || !authHeader.startsWith('Bearer ')) {
                return res.status(401).json({ error: 'Unauthorized', message: 'Missing Authorization' });
            }
            const token = authHeader.split('Bearer ')[1]?.trim();
            if (!token) {
                return res.status(401).json({ error: 'Unauthorized', message: 'Empty token' });
            }
            // Bind simulated authenticated user
            req.user = { uid: token };
            next();
        };

        app.locals.authMiddleware = testAuthMiddleware;

        // Mount authenticated API routes for tests
        app.use('/api/v1/devices', devicesRouter);
        app.use('/api/v1/verifications', verificationsRouter);

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
    // AUTHENTICATION SUITE (3 Tests)
    // ==========================================
    describe('Authentication Verification', () => {
        it('1. rejects request when Authorization header is missing', async () => {
            const res = await fetch(`${baseUrl}/test/auth-guard`);
            assert.strictEqual(res.status, 401);
            const body = await res.json();
            assert.strictEqual(body.error, 'Unauthorized');
            assert.match(body.message, /Missing or malformed Authorization header/i);
        });

        it('2. rejects request when Bearer token is empty or malformed', async () => {
            const res1 = await fetch(`${baseUrl}/test/auth-guard`, {
                headers: { Authorization: 'Basic dXNlcjpwYXNz' }
            });
            assert.strictEqual(res1.status, 401);
            const body1 = await res1.json();
            assert.match(body1.message, /malformed/i);

            const res2 = await fetch(`${baseUrl}/test/auth-guard`, {
                headers: { Authorization: 'Bearer ' }
            });
            assert.strictEqual(res2.status, 401);
            const body2 = await res2.json();
            assert.match(body2.message, /malformed|empty/i);
        });

        it('3. fails closed when Supabase credentials are unconfigured', async () => {
            // In test environment without environment variables, middleware fails closed
            const res = await fetch(`${baseUrl}/test/auth-guard`, {
                headers: { Authorization: 'Bearer some_jwt_token' }
            });
            assert.strictEqual(res.status, 401);
            const body = await res.json();
            assert.strictEqual(body.error, 'Unauthorized');
            assert.match(body.message, /credentials not configured/i);
        });
    });

    // ==========================================
    // DEVICE REGISTRY SUITE (5 Tests)
    // ==========================================
    describe('Device Registry', () => {
        it('4. successfully registers valid device for authenticated user', async () => {
            const res = await fetch(`${baseUrl}/api/v1/devices/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer user_alpha'
                },
                body: JSON.stringify({
                    deviceId: 'dev_pixel_9',
                    fcmToken: 'fcm_token_alpha_123',
                    platform: 'android'
                })
            });

            assert.strictEqual(res.status, 200);
            const body = await res.json();
            assert.strictEqual(body.success, true);
            assert.strictEqual(body.device.deviceId, 'dev_pixel_9');
            assert.strictEqual(body.device.platform, 'android');
            assert.strictEqual(body.device.enabled, true);
            assert.ok(typeof body.device.createdAt === 'number');
            assert.ok(typeof body.device.updatedAt === 'number');

            // Verify stored in canonical Supabase devices table
            const stored = await memoryDb
                .from('devices')
                .select('*')
                .eq('user_id', 'user_alpha')
                .eq('device_id', 'dev_pixel_9')
                .single();

            assert.strictEqual(stored.data.fcm_token, 'fcm_token_alpha_123');
        });

        it('5. updates existing device without duplicate records on re-registration', async () => {
            // First registration
            await fetch(`${baseUrl}/api/v1/devices/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer user_beta'
                },
                body: JSON.stringify({
                    deviceId: 'dev_iphone_16',
                    fcmToken: 'token_v1',
                    platform: 'ios'
                })
            });

            // Second registration with updated token
            const res2 = await fetch(`${baseUrl}/api/v1/devices/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer user_beta'
                },
                body: JSON.stringify({
                    deviceId: 'dev_iphone_16',
                    fcmToken: 'token_v2',
                    platform: 'ios'
                })
            });

            assert.strictEqual(res2.status, 200);

            const stored = await memoryDb
                .from('devices')
                .select('*')
                .eq('user_id', 'user_beta')
                .eq('device_id', 'dev_iphone_16')
                .single();

            assert.strictEqual(stored.data.fcm_token, 'token_v2');
        });

        it('6. preserves original createdAt timestamp on re-registration', async () => {
            const initialRes = await fetch(`${baseUrl}/api/v1/devices/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer user_gamma'
                },
                body: JSON.stringify({
                    deviceId: 'dev_tab',
                    fcmToken: 'token_tab_1',
                    platform: 'android'
                })
            });
            const initialBody = await initialRes.json();
            const originalCreatedAt = initialBody.device.createdAt;

            // Small delay to ensure timestamp progression
            await new Promise((r) => setTimeout(r, 20));

            const reRes = await fetch(`${baseUrl}/api/v1/devices/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer user_gamma'
                },
                body: JSON.stringify({
                    deviceId: 'dev_tab',
                    fcmToken: 'token_tab_2',
                    platform: 'android'
                })
            });
            const reBody = await reRes.json();

            assert.strictEqual(reBody.device.createdAt, originalCreatedAt);
            assert.ok(reBody.device.updatedAt >= originalCreatedAt);
        });

        it('7. rejects device registration with invalid platform', async () => {
            const res = await fetch(`${baseUrl}/api/v1/devices/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer user_delta'
                },
                body: JSON.stringify({
                    deviceId: 'dev_invalid',
                    fcmToken: 'token_123',
                    platform: 'windows_phone'
                })
            });

            assert.strictEqual(res.status, 400);
            const body = await res.json();
            assert.match(body.message, /invalid platform/i);
        });

        it('8. never exposes raw FCM token in registration response', async () => {
            const res = await fetch(`${baseUrl}/api/v1/devices/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer user_epsilon'
                },
                body: JSON.stringify({
                    deviceId: 'dev_secret',
                    fcmToken: 'SUPER_SECRET_FCM_REGISTRATION_TOKEN_9999',
                    platform: 'web'
                })
            });

            const bodyText = await res.text();
            assert.strictEqual(bodyText.includes('SUPER_SECRET_FCM_REGISTRATION_TOKEN_9999'), false);
        });
    });

    // ==========================================
    // TRUST & CREATION SUITE (5 Tests)
    // ==========================================
    describe('Trusted Contact Authorization & Session Creation', () => {
        it('9. rejects verification creation when target is not a trusted contact (403)', async () => {
            const res = await fetch(`${baseUrl}/api/v1/verifications`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer user_victim'
                },
                body: JSON.stringify({
                    trustedUserId: 'random_stranger_uid',
                    claimedIdentity: 'State Bank of India',
                    requestedAction: 'Transfer to safe account',
                    requestSummary: 'Suspected digital arrest impersonation',
                    riskScoreAtCreation: 85
                })
            });

            assert.strictEqual(res.status, 403);
            const body = await res.json();
            assert.strictEqual(body.error, 'Forbidden');
            assert.match(body.message, /not an authorized.*trusted contact/i);
        });

        it('10. rejects verification creation when trusted relationship is disabled (403)', async () => {
            await trustedContacts.setTrustedContact({
                protectedUid: 'user_victim',
                trustedUid: 'user_disabled_guardian',
                displayName: 'Former Contact',
                enabled: false,
                db: memoryDb
            });

            const res = await fetch(`${baseUrl}/api/v1/verifications`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer user_victim'
                },
                body: JSON.stringify({
                    trustedUserId: 'user_disabled_guardian',
                    claimedIdentity: 'Police Inspector',
                    requestedAction: 'Send money',
                    requestSummary: 'Caller threatened immediate arrest',
                    riskScoreAtCreation: 90
                })
            });

            assert.strictEqual(res.status, 403);
        });

        it('11. rejects self-verification attempt', async () => {
            const res = await fetch(`${baseUrl}/api/v1/verifications`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer user_victim'
                },
                body: JSON.stringify({
                    trustedUserId: 'user_victim',
                    claimedIdentity: 'Tax Department',
                    requestedAction: 'Pay penalty',
                    requestSummary: 'Urgent tax audit demand',
                    riskScoreAtCreation: 75
                })
            });

            assert.strictEqual(res.status, 400);
            const body = await res.json();
            assert.match(body.message, /self-verification is forbidden/i);
        });

        it('12. creates authoritative verification session with valid trusted contact', async () => {
            await trustedContacts.setTrustedContact({
                protectedUid: 'user_protected_1',
                trustedUid: 'user_trusted_1',
                displayName: 'Daughter',
                relationship: 'Family',
                enabled: true,
                db: memoryDb
            });

            const res = await fetch(`${baseUrl}/api/v1/verifications`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer user_protected_1'
                },
                body: JSON.stringify({
                    trustedUserId: 'user_trusted_1',
                    claimedIdentity: 'Cyber Crime Cell',
                    requestedAction: 'Share screen and approve payment',
                    requestSummary: 'Claimed bank account seized in money laundering',
                    riskScoreAtCreation: 92
                })
            });

            assert.strictEqual(res.status, 201);
            const body = await res.json();
            assert.strictEqual(body.success, true);
            const session = body.session;

            assert.ok(session.id.startsWith('sess_'));
            assert.strictEqual(session.protectedUserId, 'user_protected_1');
            assert.strictEqual(session.trustedUserId, 'user_trusted_1');
            assert.strictEqual(session.claimedIdentity, 'Cyber Crime Cell');
            assert.strictEqual(session.requestedAction, 'Share screen and approve payment');
            assert.strictEqual(session.requestSummary, 'Claimed bank account seized in money laundering');
            assert.strictEqual(session.riskScoreAtCreation, 92);
            assert.strictEqual(session.status, 'PENDING');
            assert.strictEqual(session.respondedAt, null);
            assert.strictEqual(session.responseDeviceId, null);
            assert.strictEqual(session.version, 1);
            assert.strictEqual(session.expiresAt - session.createdAt, 60000);
        });

        it('13. binds protectedUserId strictly to authenticated UID, ignoring body spoofing', async () => {
            await trustedContacts.setTrustedContact({
                protectedUid: 'legitimate_uid',
                trustedUid: 'trusted_guardian_9',
                displayName: 'Guardian',
                enabled: true,
                db: memoryDb
            });

            const res = await fetch(`${baseUrl}/api/v1/verifications`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer legitimate_uid'
                },
                body: JSON.stringify({
                    protectedUserId: 'spoofed_impersonated_uid',
                    trustedUserId: 'trusted_guardian_9',
                    claimedIdentity: 'Bank Rep',
                    requestedAction: 'Share OTP',
                    requestSummary: 'SMS verification',
                    riskScoreAtCreation: 65
                })
            });

            assert.strictEqual(res.status, 201);
            const body = await res.json();
            assert.strictEqual(body.session.protectedUserId, 'legitimate_uid');
            assert.notStrictEqual(body.session.protectedUserId, 'spoofed_impersonated_uid');
        });
    });

    // ==========================================
    // RESPONSE & TERMINAL STATE SUITE (5 Tests)
    // ==========================================
    describe('Verification Response & Terminal State Machine', () => {
        let activeSessionId;

        beforeEach(async () => {
            await trustedContacts.setTrustedContact({
                protectedUid: 'victim_user',
                trustedUid: 'trusted_guardian',
                displayName: 'Son',
                enabled: true,
                db: memoryDb
            });

            await deviceRegistry.registerDevice({
                uid: 'trusted_guardian',
                deviceId: 'guardian_device_alpha',
                fcmToken: 'fcm_token_guardian',
                platform: 'android',
                db: memoryDb
            });

            const session = await verificationService.createSession({
                protectedUserId: 'victim_user',
                trustedUserId: 'trusted_guardian',
                claimedIdentity: 'CBI Officer',
                requestedAction: 'Deposit surety bond',
                requestSummary: 'Fake arrest warrant shown on video call',
                riskScoreAtCreation: 95,
                db: memoryDb
            });

            activeSessionId = session.id;
        });

        it('14. rejects response from user who is not designated trusted contact (403)', async () => {
            await deviceRegistry.registerDevice({
                uid: 'unauthorized_stranger',
                deviceId: 'stranger_dev',
                fcmToken: 'token_stranger',
                platform: 'android',
                db: memoryDb
            });

            const res = await fetch(`${baseUrl}/api/v1/verifications/${activeSessionId}/respond`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer unauthorized_stranger'
                },
                body: JSON.stringify({
                    response: 'VERIFIED',
                    deviceId: 'stranger_dev'
                })
            });

            assert.strictEqual(res.status, 403);
            const body = await res.json();
            assert.match(body.message, /only the designated trusted contact/i);
        });

        it('15. returns 404 when responding to nonexistent session', async () => {
            const res = await fetch(`${baseUrl}/api/v1/verifications/sess_nonexistent_999/respond`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer trusted_guardian'
                },
                body: JSON.stringify({
                    response: 'VERIFIED',
                    deviceId: 'guardian_device_alpha'
                })
            });

            assert.strictEqual(res.status, 404);
        });

        it('16. rejects response from unregistered or disabled device (403)', async () => {
            const res = await fetch(`${baseUrl}/api/v1/verifications/${activeSessionId}/respond`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer trusted_guardian'
                },
                body: JSON.stringify({
                    response: 'VERIFIED',
                    deviceId: 'unregistered_rogue_device'
                })
            });

            assert.strictEqual(res.status, 403);
            const body = await res.json();
            assert.match(body.message, /responding device is not registered/i);
        });

        it('17. transitions PENDING session to VERIFIED on trusted approval', async () => {
            const res = await fetch(`${baseUrl}/api/v1/verifications/${activeSessionId}/respond`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer trusted_guardian'
                },
                body: JSON.stringify({
                    response: 'VERIFIED',
                    deviceId: 'guardian_device_alpha'
                })
            });

            assert.strictEqual(res.status, 200);
            const body = await res.json();
            const session = body.session;

            assert.strictEqual(session.status, 'VERIFIED');
            assert.strictEqual(session.responseDeviceId, 'guardian_device_alpha');
            assert.ok(typeof session.respondedAt === 'number');
            assert.strictEqual(session.version, 2);

            const stored = await memoryDb.from('verification_sessions').select('*').eq('id', activeSessionId).single();
            assert.strictEqual(stored.data.status, 'VERIFIED');
            assert.strictEqual(stored.data.response_device_id, 'guardian_device_alpha');
        });

        it('18. transitions PENDING session to REJECTED on trusted rejection', async () => {
            const res = await fetch(`${baseUrl}/api/v1/verifications/${activeSessionId}/respond`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer trusted_guardian'
                },
                body: JSON.stringify({
                    response: 'REJECTED',
                    deviceId: 'guardian_device_alpha'
                })
            });

            assert.strictEqual(res.status, 200);
            const body = await res.json();
            const session = body.session;

            assert.strictEqual(session.status, 'REJECTED');
            assert.strictEqual(session.responseDeviceId, 'guardian_device_alpha');
            assert.ok(typeof session.respondedAt === 'number');
            assert.strictEqual(session.version, 2);
        });
    });

    // ==========================================
    // TERMINAL INVARIANTS & EXPIRY (3 Tests)
    // ==========================================
    describe('Terminal State Invariants & Lazy Authoritative Expiry', () => {
        let terminalSessionId;

        beforeEach(async () => {
            await trustedContacts.setTrustedContact({
                protectedUid: 'victim_user',
                trustedUid: 'trusted_guardian',
                enabled: true,
                db: memoryDb
            });

            await deviceRegistry.registerDevice({
                uid: 'trusted_guardian',
                deviceId: 'guardian_device_alpha',
                fcmToken: 'fcm_token_guardian',
                platform: 'android',
                db: memoryDb
            });

            const session = await verificationService.createSession({
                protectedUserId: 'victim_user',
                trustedUserId: 'trusted_guardian',
                claimedIdentity: 'Telecom Officer',
                requestedAction: 'Verify Aadhaar details',
                requestSummary: 'Threatened phone disconnection in 2 hours',
                riskScoreAtCreation: 78,
                db: memoryDb
            });

            terminalSessionId = session.id;
        });

        it('19. first terminal state wins: VERIFIED then REJECTED stays VERIFIED', async () => {
            // First response: VERIFIED
            const firstRes = await fetch(`${baseUrl}/api/v1/verifications/${terminalSessionId}/respond`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer trusted_guardian'
                },
                body: JSON.stringify({
                    response: 'VERIFIED',
                    deviceId: 'guardian_device_alpha'
                })
            });
            const firstBody = await firstRes.json();
            assert.strictEqual(firstBody.session.status, 'VERIFIED');
            const initialRespondedAt = firstBody.session.respondedAt;
            const initialVersion = firstBody.session.version;

            // Second conflicting response: REJECTED
            const secondRes = await fetch(`${baseUrl}/api/v1/verifications/${terminalSessionId}/respond`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer trusted_guardian'
                },
                body: JSON.stringify({
                    response: 'REJECTED',
                    deviceId: 'guardian_device_alpha'
                })
            });
            const secondBody = await secondRes.json();

            // First terminal state MUST win: stays VERIFIED
            assert.strictEqual(secondBody.session.status, 'VERIFIED');
            assert.strictEqual(secondBody.session.respondedAt, initialRespondedAt);
            assert.strictEqual(secondBody.session.version, initialVersion);
        });

        it('20. first terminal state wins: REJECTED then VERIFIED stays REJECTED, and same replay is idempotent', async () => {
            // First response: REJECTED
            const firstRes = await fetch(`${baseUrl}/api/v1/verifications/${terminalSessionId}/respond`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer trusted_guardian'
                },
                body: JSON.stringify({
                    response: 'REJECTED',
                    deviceId: 'guardian_device_alpha'
                })
            });
            const firstBody = await firstRes.json();
            assert.strictEqual(firstBody.session.status, 'REJECTED');
            const initialRespondedAt = firstBody.session.respondedAt;
            const initialVersion = firstBody.session.version;

            // Conflicting response: VERIFIED
            const conflictRes = await fetch(`${baseUrl}/api/v1/verifications/${terminalSessionId}/respond`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer trusted_guardian'
                },
                body: JSON.stringify({
                    response: 'VERIFIED',
                    deviceId: 'guardian_device_alpha'
                })
            });
            const conflictBody = await conflictRes.json();
            assert.strictEqual(conflictBody.session.status, 'REJECTED');

            // Identical replay: REJECTED
            const replayRes = await fetch(`${baseUrl}/api/v1/verifications/${terminalSessionId}/respond`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer trusted_guardian'
                },
                body: JSON.stringify({
                    response: 'REJECTED',
                    deviceId: 'guardian_device_alpha'
                })
            });
            const replayBody = await replayRes.json();
            assert.strictEqual(replayBody.session.status, 'REJECTED');
            assert.strictEqual(replayBody.session.respondedAt, initialRespondedAt);
            assert.strictEqual(replayBody.session.version, initialVersion);
        });

        it('21. transitions expired PENDING session to EXPIRED on read or late response, never REJECTED', async () => {
            const pastTimestamp = Date.now() - 100000;
            await memoryDb.from('verification_sessions').insert({
                id: 'sess_expired_1',
                protected_user_id: 'victim_user',
                trusted_user_id: 'trusted_guardian',
                claimed_identity: 'Electricity Board',
                requested_action: 'Pay bill immediately',
                request_summary: 'Power disconnect scam',
                risk_score_at_creation: 80,
                status: 'PENDING',
                created_at: pastTimestamp - 60000,
                expires_at: pastTimestamp,
                responded_at: null,
                response_device_id: null,
                version: 1
            });

            // Lazy read should authoritatively transition PENDING -> EXPIRED
            const readRes = await fetch(`${baseUrl}/api/v1/verifications/sess_expired_1`, {
                headers: {
                    Authorization: 'Bearer victim_user'
                }
            });

            assert.strictEqual(readRes.status, 200);
            const readBody = await readRes.json();
            assert.strictEqual(readBody.session.status, 'EXPIRED');
            assert.notStrictEqual(readBody.session.status, 'REJECTED');
            assert.strictEqual(readBody.session.respondedAt, null);

            // Attempting to respond to expired session returns EXPIRED
            const lateResponseRes = await fetch(`${baseUrl}/api/v1/verifications/sess_expired_1/respond`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: 'Bearer trusted_guardian'
                },
                body: JSON.stringify({
                    response: 'VERIFIED',
                    deviceId: 'guardian_device_alpha'
                })
            });

            assert.strictEqual(lateResponseRes.status, 200);
            const lateBody = await lateResponseRes.json();
            assert.strictEqual(lateBody.session.status, 'EXPIRED');
        });
    });

    // ==========================================
    // CONCURRENCY & RACE CONDITIONS SUITE (3 Tests)
    // ==========================================
    describe('Concurrency & Atomic Compare-and-Set Race Tests', () => {
        let raceSessionId;

        beforeEach(async () => {
            await trustedContacts.setTrustedContact({
                protectedUid: 'victim_user',
                trustedUid: 'trusted_guardian',
                enabled: true,
                db: memoryDb
            });

            await deviceRegistry.registerDevice({
                uid: 'trusted_guardian',
                deviceId: 'device_race_1',
                fcmToken: 'token_race_1',
                platform: 'android',
                db: memoryDb
            });

            const session = await verificationService.createSession({
                protectedUserId: 'victim_user',
                trustedUserId: 'trusted_guardian',
                claimedIdentity: 'Digital Arrest Official',
                requestedAction: 'Wire money',
                requestSummary: 'Extortion call in progress',
                riskScoreAtCreation: 99,
                db: memoryDb
            });

            raceSessionId = session.id;
        });

        it('22. concurrent race: VERIFIED vs REJECTED results in exactly one winning terminal state', async () => {
            // Send both VERIFIED and REJECTED concurrently
            const [res1, res2] = await Promise.all([
                fetch(`${baseUrl}/api/v1/verifications/${raceSessionId}/respond`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: 'Bearer trusted_guardian'
                    },
                    body: JSON.stringify({
                        response: 'VERIFIED',
                        deviceId: 'device_race_1'
                    })
                }),
                fetch(`${baseUrl}/api/v1/verifications/${raceSessionId}/respond`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: 'Bearer trusted_guardian'
                    },
                    body: JSON.stringify({
                        response: 'REJECTED',
                        deviceId: 'device_race_1'
                    })
                })
            ]);

            assert.strictEqual(res1.status, 200);
            assert.strictEqual(res2.status, 200);

            const body1 = await res1.json();
            const body2 = await res2.json();

            // Both calls must receive the exact same winning terminal state
            assert.strictEqual(body1.session.status, body2.session.status);
            assert.ok(body1.session.status === 'VERIFIED' || body1.session.status === 'REJECTED');
            assert.strictEqual(body1.session.version, 2);
            assert.strictEqual(body2.session.version, 2);

            // Canonical DB row must match
            const canonical = await memoryDb.from('verification_sessions').select('*').eq('id', raceSessionId).single();
            assert.strictEqual(canonical.data.status, body1.session.status);
            assert.strictEqual(canonical.data.version, 2);
        });

        it('23. concurrent race: response vs expiry results in exactly one winning terminal state', async () => {
            // Seed a session that expired 5ms ago
            const pastTimestamp = Date.now() - 1000;
            const expiringId = 'sess_expiring_race';
            await memoryDb.from('verification_sessions').insert({
                id: expiringId,
                protected_user_id: 'victim_user',
                trusted_user_id: 'trusted_guardian',
                claimed_identity: 'Police Inspector',
                requested_action: 'Send bail money',
                request_summary: 'Threatening phone call',
                risk_score_at_creation: 91,
                status: 'PENDING',
                created_at: pastTimestamp - 60000,
                expires_at: pastTimestamp,
                responded_at: null,
                response_device_id: null,
                version: 1
            });

            // Concurrently perform lazy getSession and respondToSession
            const [getRes, respondRes] = await Promise.all([
                fetch(`${baseUrl}/api/v1/verifications/${expiringId}`, {
                    headers: { Authorization: 'Bearer victim_user' }
                }),
                fetch(`${baseUrl}/api/v1/verifications/${expiringId}/respond`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: 'Bearer trusted_guardian'
                    },
                    body: JSON.stringify({
                        response: 'VERIFIED',
                        deviceId: 'device_race_1'
                    })
                })
            ]);

            assert.strictEqual(getRes.status, 200);
            assert.strictEqual(respondRes.status, 200);

            const getBody = await getRes.json();
            const respondBody = await respondRes.json();

            // Both must converge on an authoritative terminal state (either EXPIRED or VERIFIED)
            assert.ok(getBody.session.status === 'EXPIRED' || getBody.session.status === 'VERIFIED');
            assert.ok(respondBody.session.status === 'EXPIRED' || respondBody.session.status === 'VERIFIED');
            assert.notStrictEqual(getBody.session.status, 'REJECTED');
            assert.notStrictEqual(respondBody.session.status, 'REJECTED');
        });

        it('24. concurrent duplicate identical responses are idempotent and do not increment version twice', async () => {
            const [res1, res2] = await Promise.all([
                fetch(`${baseUrl}/api/v1/verifications/${raceSessionId}/respond`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: 'Bearer trusted_guardian'
                    },
                    body: JSON.stringify({
                        response: 'VERIFIED',
                        deviceId: 'device_race_1'
                    })
                }),
                fetch(`${baseUrl}/api/v1/verifications/${raceSessionId}/respond`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: 'Bearer trusted_guardian'
                    },
                    body: JSON.stringify({
                        response: 'VERIFIED',
                        deviceId: 'device_race_1'
                    })
                })
            ]);

            assert.strictEqual(res1.status, 200);
            assert.strictEqual(res2.status, 200);

            const body1 = await res1.json();
            const body2 = await res2.json();

            assert.strictEqual(body1.session.status, 'VERIFIED');
            assert.strictEqual(body2.session.status, 'VERIFIED');
            assert.strictEqual(body1.session.respondedAt, body2.session.respondedAt);
            assert.strictEqual(body1.session.version, 2);
            assert.strictEqual(body2.session.version, 2);

            const canonical = await memoryDb.from('verification_sessions').select('*').eq('id', raceSessionId).single();
            assert.strictEqual(canonical.data.version, 2);
        });
    });
});
