const { describe, it } = require('node:test');
const assert = require('node:assert/strict');
const { dispatchVerificationPush } = require('../services/verificationPush');

/**
 * Creates an in-memory mock Supabase DB client with strict schema validation
 * for the devices table:
 * - Real schema: (user_id, device_id, fcm_token, platform, enabled, created_at, updated_at, last_seen_at)
 * - NO generic 'id' column exists. Any query requesting 'id' will explicitly throw.
 */
function createMockDb(initialDevices = []) {
    const devices = initialDevices.map(d => ({ ...d }));
    const queries = [];
    const updates = [];

    return {
        _devices: devices,
        _queries: queries,
        _updates: updates,
        from(tableName) {
            if (tableName === 'devices') {
                return {
                    select(fields) {
                        const requestedCols = fields.split(',').map(s => s.trim());
                        if (requestedCols.includes('id')) {
                            throw new Error('Supabase schema error: column "id" does not exist in public.devices table');
                        }
                        return {
                            eq(f1, v1) {
                                return {
                                    eq(f2, v2) {
                                        queries.push({ [f1]: v1, [f2]: v2 });
                                        const filtered = devices.filter(d => d[f1] === v1 && d[f2] === v2);
                                        return Promise.resolve({ data: filtered, error: null });
                                    }
                                };
                            }
                        };
                    },
                    update(updatePayload) {
                        return {
                            eq(f1, v1) {
                                return {
                                    eq(f2, v2) {
                                        if (f1 === 'id' || f2 === 'id') {
                                            throw new Error('Supabase schema error: cannot predicate on nonexistent column "id"');
                                        }
                                        updates.push({ predicate: { [f1]: v1, [f2]: v2 }, payload: updatePayload });
                                        for (const dev of devices) {
                                            if (dev[f1] === v1 && dev[f2] === v2) {
                                                Object.assign(dev, updatePayload);
                                            }
                                        }
                                        return Promise.resolve({ data: devices, error: null });
                                    }
                                };
                            }
                        };
                    }
                };
            }
            if (tableName === 'verification_sessions') {
                return {
                    insert(row) {
                        return Promise.resolve({ data: [row], error: null });
                    },
                    select() {
                        return {
                            eq() {
                                return {
                                    maybeSingle() {
                                        return Promise.resolve({ data: null, error: null });
                                    }
                                };
                            }
                        };
                    }
                };
            }
            throw new Error(`Unexpected table ${tableName}`);
        }
    };
}

describe('FCM Verification Push Service', () => {

    it('1. enabled trusted device -> send attempted with high-priority contract', async () => {
        const mockDb = createMockDb([
            { user_id: 'trusted_1', device_id: 'dev_1', fcm_token: 'valid_token_123', enabled: true, platform: 'android' }
        ]);

        const sentMessages = [];
        const mockMessaging = {
            send(msg) {
                sentMessages.push(msg);
                return Promise.resolve({ messageId: 'msg_1' });
            }
        };

        const session = {
            id: 'sess_101',
            protectedUserId: 'protected_1',
            trustedUserId: 'trusted_1',
            claimedIdentity: 'Bank Fraud Department',
            requestedAction: 'Freeze Account',
            expiresAt: 1790000000000
        };

        const result = await dispatchVerificationPush({
            session,
            db: mockDb,
            messagingClient: mockMessaging
        });

        assert.equal(result.configured, true);
        assert.equal(result.attempted, 1);
        assert.equal(result.accepted, 1);
        assert.equal(result.invalidTokens, 0);
        assert.equal(result.transientFailures, 0);
        assert.equal(result.deliveryStatus, 'delivered');

        assert.equal(sentMessages.length, 1);
        assert.equal(sentMessages[0].token, 'valid_token_123');
        assert.equal(sentMessages[0].data.type, 'identity_verification');
        assert.equal(sentMessages[0].data.sessionId, 'sess_101');
        assert.equal(sentMessages[0].data.claimedIdentity, 'Bank Fraud Department');
        assert.equal(sentMessages[0].data.requestedAction, 'Freeze Account');
        assert.equal(sentMessages[0].android.priority, 'high');
    });

    it('2. disabled device -> skipped', async () => {
        const mockDb = createMockDb([
            { user_id: 'trusted_1', device_id: 'dev_disabled', fcm_token: 'token_disabled', enabled: false, platform: 'android' }
        ]);

        let sendCalled = false;
        const mockMessaging = {
            send() {
                sendCalled = true;
                return Promise.resolve();
            }
        };

        const session = {
            id: 'sess_102',
            protectedUserId: 'protected_1',
            trustedUserId: 'trusted_1',
            claimedIdentity: 'Police Officer',
            expiresAt: 1790000000000
        };

        const result = await dispatchVerificationPush({
            session,
            db: mockDb,
            messagingClient: mockMessaging
        });

        assert.equal(result.attempted, 0);
        assert.equal(result.accepted, 0);
        assert.equal(result.deliveryStatus, 'no_devices');
        assert.equal(sendCalled, false);
    });

    it('3. multiple enabled devices -> dispatch all unique tokens', async () => {
        const mockDb = createMockDb([
            { user_id: 'trusted_1', device_id: 'dev_phone', fcm_token: 'token_phone_abc', enabled: true, platform: 'android' },
            { user_id: 'trusted_1', device_id: 'dev_tablet', fcm_token: 'token_tablet_xyz', enabled: true, platform: 'android' }
        ]);

        const sentTokens = [];
        const mockMessaging = {
            send(msg) {
                sentTokens.push(msg.token);
                return Promise.resolve();
            }
        };

        const session = {
            id: 'sess_103',
            protectedUserId: 'protected_1',
            trustedUserId: 'trusted_1',
            claimedIdentity: 'Courier Agent',
            expiresAt: 1790000000000
        };

        const result = await dispatchVerificationPush({
            session,
            db: mockDb,
            messagingClient: mockMessaging
        });

        assert.equal(result.attempted, 2);
        assert.equal(result.accepted, 2);
        assert.deepEqual(sentTokens, ['token_phone_abc', 'token_tablet_xyz']);
    });

    it('4. duplicate tokens -> de-duplicated into single dispatch', async () => {
        const mockDb = createMockDb([
            { user_id: 'trusted_1', device_id: 'dev_1', fcm_token: 'token_same', enabled: true, platform: 'android' },
            { user_id: 'trusted_1', device_id: 'dev_2', fcm_token: 'token_same', enabled: true, platform: 'android' }
        ]);

        const sentTokens = [];
        const mockMessaging = {
            send(msg) {
                sentTokens.push(msg.token);
                return Promise.resolve();
            }
        };

        const session = {
            id: 'sess_104',
            protectedUserId: 'protected_1',
            trustedUserId: 'trusted_1',
            claimedIdentity: 'Insurance Agent',
            expiresAt: 1790000000000
        };

        const result = await dispatchVerificationPush({
            session,
            db: mockDb,
            messagingClient: mockMessaging
        });

        assert.equal(result.attempted, 1);
        assert.equal(result.accepted, 1);
        assert.equal(sentTokens.length, 1);
    });

    it('5. no devices -> structured no_devices result without throwing', async () => {
        const mockDb = createMockDb([]);

        const session = {
            id: 'sess_105',
            protectedUserId: 'protected_1',
            trustedUserId: 'trusted_user_with_no_devices',
            claimedIdentity: 'Tax Inspector',
            expiresAt: 1790000000000
        };

        const result = await dispatchVerificationPush({
            session,
            db: mockDb,
            messagingClient: { send() {} }
        });

        assert.equal(result.attempted, 0);
        assert.equal(result.accepted, 0);
        assert.equal(result.deliveryStatus, 'no_devices');
        assert.ok(result.details.includes('No enabled registered devices'));
    });

    it('6. invalid/unregistered token -> disables ONLY exact matching device row, preserving others', async () => {
        // User has two enabled devices: one with an invalid token, one with a valid token
        const mockDb = createMockDb([
            { user_id: 'trusted_1', device_id: 'dev_bad', fcm_token: 'bad_token_404', enabled: true, platform: 'android' },
            { user_id: 'trusted_1', device_id: 'dev_good', fcm_token: 'good_token_200', enabled: true, platform: 'android' }
        ]);

        const mockMessaging = {
            send(msg) {
                if (msg.token === 'bad_token_404') {
                    const err = new Error('The registration token is not registered');
                    err.code = 'messaging/registration-token-not-registered';
                    return Promise.reject(err);
                }
                return Promise.resolve({ messageId: 'msg_good' });
            }
        };

        const session = {
            id: 'sess_106',
            protectedUserId: 'protected_1',
            trustedUserId: 'trusted_1',
            claimedIdentity: 'Utility Officer',
            expiresAt: 1790000000000
        };

        const result = await dispatchVerificationPush({
            session,
            db: mockDb,
            messagingClient: mockMessaging
        });

        assert.equal(result.attempted, 2);
        assert.equal(result.accepted, 1);
        assert.equal(result.invalidTokens, 1);
        assert.equal(result.transientFailures, 0);
        assert.equal(result.deliveryStatus, 'delivered');

        // Confirm: exact matching device row dev_bad was disabled
        const badDev = mockDb._devices.find(d => d.device_id === 'dev_bad');
        assert.equal(badDev.enabled, false);

        // Confirm: the other device dev_good remains enabled!
        const goodDev = mockDb._devices.find(d => d.device_id === 'dev_good');
        assert.equal(goodDev.enabled, true);

        // Confirm update was predicated on composite key (user_id, device_id), not nonexistent id
        assert.equal(mockDb._updates.length, 1);
        assert.deepEqual(mockDb._updates[0].predicate, { user_id: 'trusted_1', device_id: 'dev_bad' });
    });

    it('7. transient FCM error -> device remains enabled', async () => {
        const mockDb = createMockDb([
            { user_id: 'trusted_1', device_id: 'dev_good', fcm_token: 'good_token', enabled: true, platform: 'android' }
        ]);

        const mockMessaging = {
            send() {
                const err = new Error('Server unavailable');
                err.code = 'messaging/server-unavailable';
                return Promise.reject(err);
            }
        };

        const session = {
            id: 'sess_107',
            protectedUserId: 'protected_1',
            trustedUserId: 'trusted_1',
            claimedIdentity: 'Customs Officer',
            expiresAt: 1790000000000
        };

        const result = await dispatchVerificationPush({
            session,
            db: mockDb,
            messagingClient: mockMessaging
        });

        assert.equal(result.attempted, 1);
        assert.equal(result.accepted, 0);
        assert.equal(result.invalidTokens, 0);
        assert.equal(result.transientFailures, 1);
        assert.equal(result.deliveryStatus, 'failed');

        // Device remains enabled
        const devInDb = mockDb._devices.find(d => d.device_id === 'dev_good');
        assert.equal(devInDb.enabled, true);
        assert.equal(mockDb._updates.length, 0);
    });

    it('8. raw FCM token is never returned in dispatch summary', async () => {
        const rawToken = 'SECRET_REAL_FCM_DEVICE_TOKEN_999999999999999';
        const mockDb = createMockDb([
            { user_id: 'trusted_1', device_id: 'dev_sec', fcm_token: rawToken, enabled: true, platform: 'android' }
        ]);

        const mockMessaging = {
            send() {
                return Promise.resolve();
            }
        };

        const session = {
            id: 'sess_108',
            protectedUserId: 'protected_1',
            trustedUserId: 'trusted_1',
            claimedIdentity: 'Bank Rep',
            expiresAt: 1790000000000
        };

        const result = await dispatchVerificationPush({
            session,
            db: mockDb,
            messagingClient: mockMessaging
        });

        const resultString = JSON.stringify(result);
        assert.equal(resultString.includes(rawToken), false);
        assert.equal(result.rawTokens, undefined);
        assert.equal(result.tokens, undefined);
    });

    it('9. missing Firebase credentials != fake accepted push (reports unconfigured)', async () => {
        const mockDb = createMockDb([
            { user_id: 'trusted_1', device_id: 'dev_1', fcm_token: 'tok_1', enabled: true, platform: 'android' }
        ]);

        const session = {
            id: 'sess_109',
            protectedUserId: 'protected_1',
            trustedUserId: 'trusted_1',
            claimedIdentity: 'Credit Card Security',
            expiresAt: 1790000000000
        };

        // When messaging client is null (runtime without credentials)
        const result = await dispatchVerificationPush({
            session,
            db: mockDb,
            messagingClient: null
        });

        assert.equal(result.configured, false);
        assert.equal(result.attempted, 0);
        assert.equal(result.accepted, 0);
        assert.equal(result.deliveryStatus, 'unconfigured');
        assert.ok(result.details.includes('FCM messaging transport not configured'));
    });

    it('10. exact trustedUserId from VerificationSession is used for device lookup', async () => {
        const mockDb = createMockDb([
            { user_id: 'designated_trusted_uid_999', device_id: 'dev_des', fcm_token: 'tok_target', enabled: true, platform: 'android' },
            { user_id: 'other_user_456', device_id: 'dev_other', fcm_token: 'tok_other', enabled: true, platform: 'android' }
        ]);

        const sentTokens = [];
        const mockMessaging = {
            send(msg) {
                sentTokens.push(msg.token);
                return Promise.resolve();
            }
        };

        const session = {
            id: 'sess_exact_uid',
            protectedUserId: 'protected_user_123',
            trustedUserId: 'designated_trusted_uid_999', // Canonical field
            claimedIdentity: 'Support Agent',
            expiresAt: 1790000000000
        };

        const result = await dispatchVerificationPush({
            session,
            db: mockDb,
            messagingClient: mockMessaging
        });

        assert.equal(result.accepted, 1);
        assert.deepEqual(sentTokens, ['tok_target']);
        assert.deepEqual(mockDb._queries[0], { user_id: 'designated_trusted_uid_999', enabled: true });
    });

    it('11. session creation persists even when FCM dispatch fails', async () => {
        const mockDb = createMockDb([
            { user_id: 'trusted_1', device_id: 'dev_err', fcm_token: 'token_err', enabled: true, platform: 'android' }
        ]);

        const mockMessaging = {
            send() {
                return Promise.reject(new Error('Fatal network error in FCM transport'));
            }
        };

        const session = {
            id: 'sess_111',
            protectedUserId: 'protected_1',
            trustedUserId: 'trusted_1',
            claimedIdentity: 'Credit Card Security',
            requestedAction: 'Block Card',
            requestSummary: 'Immediate authorization request',
            riskScoreAtCreation: 85,
            status: 'PENDING',
            createdAt: Date.now(),
            expiresAt: Date.now() + 60000,
            version: 1
        };

        const pushResult = await dispatchVerificationPush({
            session,
            db: mockDb,
            messagingClient: mockMessaging
        });

        assert.equal(pushResult.deliveryStatus, 'failed');
        assert.equal(pushResult.transientFailures, 1);

        // Canonical session in memory / caller remains untouched
        assert.equal(session.status, 'PENDING');
        assert.equal(session.version, 1);
        assert.equal(session.id, 'sess_111');
    });

    it('12. Supabase canonical state is unaffected by push failure', async () => {
        const session = {
            id: 'sess_112',
            protectedUserId: 'protected_1',
            trustedUserId: 'trusted_1',
            claimedIdentity: 'Tax Agency',
            status: 'PENDING',
            expiresAt: 1790000000000,
            version: 1
        };

        const pushResult = await dispatchVerificationPush({
            session,
            db: createMockDb([{ user_id: 'trusted_1', device_id: 'dev_1', fcm_token: 'tok_1', enabled: true, platform: 'android' }]),
            messagingClient: null
        });

        assert.equal(pushResult.deliveryStatus, 'unconfigured');
        assert.equal(session.status, 'PENDING');
        assert.equal(session.version, 1);
    });
});
