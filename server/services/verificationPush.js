const { getAdminClient } = require('./supabase');
const { getMessagingClient } = require('./firebaseMessaging');

const PERMANENT_INVALID_ERROR_CODES = new Set([
    'messaging/invalid-registration-token',
    'messaging/registration-token-not-registered',
    'messaging/invalid-argument'
]);

/**
 * Truncates an FCM token for safe diagnostic logging without leaking secrets.
 */
function safeTokenPrefix(token) {
    if (!token || typeof token !== 'string') return 'invalid';
    return `${token.slice(0, 8)}...`;
}

/**
 * Dispatches high-priority Android FCM notification for an authoritative VerificationSession.
 *
 * Requirements:
 * - Queries Supabase devices table for session.trustedUserId
 * - Queries ONLY real schema columns: user_id, device_id, fcm_token, enabled, platform (NO generic id column)
 * - Only enabled devices with non-empty FCM tokens
 * - Deduplicates identical token values
 * - Dispatches high-priority Android message with canonical contract
 * - Invalid/unregistered tokens are marked disabled in Supabase devices table using (user_id, device_id)
 * - Transient errors leave device enabled
 * - Unconfigured Firebase credentials return structured unconfigured status, never fake success
 * - Never logs or returns raw FCM tokens
 * - Never mutates or deletes canonical VerificationSession on delivery failures
 *
 * @param {Object} options
 * @param {Object} options.session - The canonical VerificationSession
 * @param {Object} [options.db] - Supabase admin client
 * @param {Object} [options.messagingClient] - Firebase Messaging client or mock
 * @returns {Promise<Object>} Structured dispatch summary
 */
async function dispatchVerificationPush({
    session,
    db = getAdminClient(),
    messagingClient = getMessagingClient()
}) {
    if (!session || !session.id || !session.trustedUserId) {
        throw new Error('Valid session with id and trustedUserId is required for verification push');
    }

    if (!db) {
        console.warn('[VerificationPush] Supabase admin client unavailable');
        return {
            configured: false,
            attempted: 0,
            accepted: 0,
            invalidTokens: 0,
            transientFailures: 0,
            deliveryStatus: 'unavailable',
            details: 'Database service unavailable'
        };
    }

    // 1. Query enabled devices for the exact trusted user from the session
    // Real schema: (user_id, device_id, fcm_token, platform, enabled, created_at, updated_at, last_seen_at)
    // There is NO generic "id" column.
    const { data: devices, error: dbError } = await db
        .from('devices')
        .select('user_id, device_id, fcm_token, enabled, platform')
        .eq('user_id', session.trustedUserId)
        .eq('enabled', true);

    if (dbError) {
        console.error('[VerificationPush] Error querying devices for user:', dbError.message);
        return {
            configured: true,
            attempted: 0,
            accepted: 0,
            invalidTokens: 0,
            transientFailures: 1,
            deliveryStatus: 'failed',
            details: 'Failed to query user devices'
        };
    }

    if (!devices || devices.length === 0) {
        console.log(`[VerificationPush] No enabled devices found for trusted user: ${session.trustedUserId.slice(0, 8)}...`);
        return {
            configured: true,
            attempted: 0,
            accepted: 0,
            invalidTokens: 0,
            transientFailures: 0,
            deliveryStatus: 'no_devices',
            details: 'No enabled registered devices found for trusted user'
        };
    }

    // 2. Filter valid non-empty tokens and deduplicate
    const validDevices = devices.filter(d => typeof d.fcm_token === 'string' && d.fcm_token.trim().length > 0);
    const uniqueTokens = [...new Set(validDevices.map(d => d.fcm_token.trim()))];

    if (uniqueTokens.length === 0) {
        return {
            configured: true,
            attempted: 0,
            accepted: 0,
            invalidTokens: 0,
            transientFailures: 0,
            deliveryStatus: 'no_devices',
            details: 'No valid non-empty FCM tokens found'
        };
    }

    // 3. In real runtime, missing credentials must report unconfigured rather than fake success
    if (!messagingClient) {
        console.warn('[VerificationPush] Messaging transport unconfigured (running without live Firebase credentials)');
        return {
            configured: false,
            attempted: 0,
            accepted: 0,
            invalidTokens: 0,
            transientFailures: 0,
            deliveryStatus: 'unconfigured',
            details: 'FCM messaging transport not configured or credentials unavailable'
        };
    }

    // 4. Construct canonical high-priority data payload
    const dataPayload = {
        type: 'identity_verification',
        sessionId: String(session.id),
        claimedIdentity: String(session.claimedIdentity || ''),
        expiresAt: String(session.expiresAt || '')
    };
    if (session.requestedAction && String(session.requestedAction).trim()) {
        dataPayload.requestedAction = String(session.requestedAction).trim();
    }

    let accepted = 0;
    let invalidTokens = 0;
    let transientFailures = 0;

    // 5. Dispatch to each unique token
    for (const token of uniqueTokens) {
        const message = {
            token,
            data: dataPayload,
            android: {
                priority: 'high'
            }
        };

        try {
            await messagingClient.send(message);
            accepted++;
            console.log(`[VerificationPush] Sent high-priority push to token [${safeTokenPrefix(token)}]`);
        } catch (err) {
            const errorCode = err.code || '';
            const isPermanent = PERMANENT_INVALID_ERROR_CODES.has(errorCode) ||
                err.message?.includes('registration-token-not-registered') ||
                err.message?.includes('invalid-registration-token');

            if (isPermanent) {
                invalidTokens++;
                console.warn(`[VerificationPush] Invalid/unregistered token detected [${safeTokenPrefix(token)}]. Disabling device in Supabase.`);
                // Safely identify exact matching device row(s) using composite primary key (user_id, device_id)
                const matchingDevices = validDevices.filter(d => d.fcm_token === token);
                for (const dev of matchingDevices) {
                    try {
                        await db
                            .from('devices')
                            .update({
                                enabled: false,
                                updated_at: Date.now()
                            })
                            .eq('user_id', session.trustedUserId)
                            .eq('device_id', dev.device_id);
                    } catch (disableErr) {
                        console.error('[VerificationPush] Failed to disable invalid device row:', disableErr.message);
                    }
                }
            } else {
                transientFailures++;
                console.warn(`[VerificationPush] Transient error dispatching to token [${safeTokenPrefix(token)}]:`, err.message);
            }
        }
    }

    return {
        configured: true,
        attempted: uniqueTokens.length,
        accepted,
        invalidTokens,
        transientFailures,
        deliveryStatus: accepted > 0 ? 'delivered' : 'failed'
    };
}

module.exports = {
    dispatchVerificationPush
};
