const { describe, it, beforeEach, afterEach } = require('node:test');
const assert = require('node:assert');
const http = require('http');
const express = require('express');

const { verifyFirebaseAuth } = require('../middleware/firebaseAuth');
const deviceRegistry = require('../services/deviceRegistry');
const trustedContacts = require('../services/trustedContacts');
const verificationService = require('../services/verificationService');
const devicesRouter = require('../routes/devices');
const verificationsRouter = require('../routes/verifications');

/**
 * High-fidelity in-memory Firestore test double supporting:
 * - nested collections (users/{uid}/devices/{devId}, users/{uid}/trustedContacts/{tId})
 * - root collections (verificationSessions/{id})
 * - set, get, update, and transactional runTransaction semantics
 */
class MemoryDocRef {
    constructor(db, pathSegments) {
        this.db = db;
        this.pathSegments = pathSegments;
        this.id = pathSegments[pathSegments.length - 1];
    }

    get path() {
        return this.pathSegments.join('/');
    }

    collection(name) {
        return new MemoryCollection(this.db, [...this.pathSegments, name]);
    }

    async get() {
        const fullPath = this.path;
        if (this.db.store.has(fullPath)) {
            const data = JSON.parse(JSON.stringify(this.db.store.get(fullPath)));
            return {
                exists: true,
                id: this.id,
                data: () => data
            };
        }
        return {
            exists: false,
            id: this.id,
            data: () => undefined
        };
    }

    async set(data, options = {}) {
        const fullPath = this.path;
        if (options.merge && this.db.store.has(fullPath)) {
            const existing = this.db.store.get(fullPath);
            this.db.store.set(fullPath, { ...existing, ...JSON.parse(JSON.stringify(data)) });
        } else {
            this.db.store.set(fullPath, JSON.parse(JSON.stringify(data)));
        }
    }

    async update(data) {
        const fullPath = this.path;
        if (!this.db.store.has(fullPath)) {
            const error = new Error(`Document ${fullPath} not found`);
            error.statusCode = 404;
            throw error;
        }
        const existing = this.db.store.get(fullPath);
        this.db.store.set(fullPath, { ...existing, ...JSON.parse(JSON.stringify(data)) });
    }
}

class MemoryCollection {
    constructor(db, pathSegments) {
        this.db = db;
        this.pathSegments = pathSegments;
    }

    doc(id) {
        return new MemoryDocRef(this.db, [...this.pathSegments, String(id)]);
    }
}

class MemoryFirestore {
    constructor() {
        this.store = new Map();
    }

    collection(name) {
        return new MemoryCollection(this, [name]);
    }

    async runTransaction(updateFunction) {
        const transaction = {
            get: async (docRef) => docRef.get(),
            set: async (docRef, data, options) => docRef.set(data, options),
            update: async (docRef, data) => docRef.update(data)
        };
        return await updateFunction(transaction);
    }
}

describe('Authoritative Verification Backend (CP3)', () => {
    let memoryDb;
    let server;
    let baseUrl;

    beforeEach(async () => {
        memoryDb = new MemoryFirestore();

        // Build Express test application with simulated authenticated test harness
        const app = express();
        app.use(express.json());
        app.locals.db = memoryDb;

        // Diagnostic route for auth middleware tests
        app.get('/test/auth-guard', verifyFirebaseAuth, (req, res) => {
            res.json({ ok: true, user: req.user });
        });

        // Test harness router that simulates authenticated req.user based on Bearer token value
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

        it('3. fails closed when Firebase Admin credentials are unconfigured', async () => {
            // In test environment without service account, middleware must fail closed
            const res = await fetch(`${baseUrl}/test/auth-guard`, {
                headers: { Authorization: 'Bearer some_mock_token' }
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

            // Verify stored in canonical Firestore path: users/{uid}/devices/{deviceId}
            const stored = await memoryDb
                .collection('users')
                .doc('user_alpha')
                .collection('devices')
                .doc('dev_pixel_9')
                .get();

            assert.strictEqual(stored.exists, true);
            assert.strictEqual(stored.data().fcmToken, 'fcm_token_alpha_123');
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
                .collection('users')
                .doc('user_beta')
                .collection('devices')
                .doc('dev_iphone_16')
                .get();

            assert.strictEqual(stored.data().fcmToken, 'token_v2');
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

            // Artificial delay to ensure timestamp progression
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
            // Establish disabled relationship under users/{victim}/trustedContacts/{guardian}
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
                    trustedUserId: 'user_victim', // Self-verification
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
            // Authorize guardian
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
            assert.strictEqual(session.expiresAt - session.createdAt, 60000); // 60s TTL
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
                    protectedUserId: 'spoofed_impersonated_uid', // Malicious attempt to spoof
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
            // Seed relationship
            await trustedContacts.setTrustedContact({
                protectedUid: 'victim_user',
                trustedUid: 'trusted_guardian',
                displayName: 'Son',
                enabled: true,
                db: memoryDb
            });

            // Seed registered device for trusted guardian
            await deviceRegistry.registerDevice({
                uid: 'trusted_guardian',
                deviceId: 'guardian_device_alpha',
                fcmToken: 'fcm_token_guardian',
                platform: 'android',
                db: memoryDb
            });

            // Create initial pending session
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
            // Register device for imposter
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

            // Verify persistence in Firestore
            const stored = await memoryDb.collection('verificationSessions').doc(activeSessionId).get();
            assert.strictEqual(stored.data().status, 'VERIFIED');
            assert.strictEqual(stored.data().responseDeviceId, 'guardian_device_alpha');
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
            // Create a session with already-expired TTL
            const pastTimestamp = Date.now() - 100000;
            const expiredDocRef = memoryDb.collection('verificationSessions').doc('sess_expired_1');
            await expiredDocRef.set({
                id: 'sess_expired_1',
                protectedUserId: 'victim_user',
                trustedUserId: 'trusted_guardian',
                claimedIdentity: 'Electricity Board',
                requestedAction: 'Pay bill immediately',
                requestSummary: 'Power disconnect scam',
                riskScoreAtCreation: 80,
                status: 'PENDING',
                createdAt: pastTimestamp - 60000,
                expiresAt: pastTimestamp, // expired in past
                respondedAt: null,
                responseDeviceId: null,
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
            assert.notStrictEqual(readBody.session.status, 'REJECTED'); // Timeout is never REJECTED
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
});
