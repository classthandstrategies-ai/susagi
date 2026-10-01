const { getAdminClient } = require('./supabase');

const ALLOWED_PLATFORMS = new Set(['android', 'ios', 'web']);

/**
 * Validates device registration parameters.
 */
function validateRegistrationInput({ uid, deviceId, fcmToken, platform }) {
    if (!uid || typeof uid !== 'string' || uid.trim() === '') {
        const error = new Error('Authenticated user ID (uid) is required');
        error.statusCode = 401;
        throw error;
    }

    if (!deviceId || typeof deviceId !== 'string' || deviceId.trim() === '') {
        const error = new Error('deviceId is required and must be a non-empty string');
        error.statusCode = 400;
        throw error;
    }

    if (!fcmToken || typeof fcmToken !== 'string' || fcmToken.trim() === '') {
        const error = new Error('fcmToken is required and must be a non-empty string');
        error.statusCode = 400;
        throw error;
    }

    const normalizedPlatform = typeof platform === 'string' ? platform.trim().toLowerCase() : '';
    if (!ALLOWED_PLATFORMS.has(normalizedPlatform)) {
        const error = new Error(`Invalid platform "${platform}". Allowed platforms: ${Array.from(ALLOWED_PLATFORMS).join(', ')}`);
        error.statusCode = 400;
        throw error;
    }

    return {
        cleanUid: uid.trim(),
        cleanDeviceId: deviceId.trim(),
        cleanFcmToken: fcmToken.trim(),
        normalizedPlatform
    };
}

/**
 * Registers or updates an authenticated device in Supabase PostgreSQL:
 *   Table: devices (user_id, device_id, fcm_token, platform, enabled, created_at, updated_at, last_seen_at)
 *
 * Rules:
 * - Ownership bound strictly to authenticated uid.
 * - Idempotently updates same row if (user_id, device_id) already exists.
 * - Preserves original created_at timestamp on re-registration.
 * - Updates updated_at and last_seen_at.
 * - Never returns raw FCM token in response.
 * - Never logs raw FCM token.
 */
async function registerDevice({ uid, deviceId, fcmToken, platform, db = getAdminClient() }) {
    const { cleanUid, cleanDeviceId, cleanFcmToken, normalizedPlatform } =
        validateRegistrationInput({ uid, deviceId, fcmToken, platform });

    if (!db) {
        const error = new Error('Database service unavailable: Supabase is not configured');
        error.statusCode = 503;
        throw error;
    }

    const maskedToken = cleanFcmToken.length > 8
        ? `${cleanFcmToken.substring(0, 4)}...${cleanFcmToken.substring(cleanFcmToken.length - 4)}`
        : '***';

    console.log(`[DeviceRegistry] Registering device "${cleanDeviceId}" (${normalizedPlatform}) for user "${cleanUid}", token: ${maskedToken}`);

    // Check existing device to preserve original created_at
    const existing = await getDevice({ uid: cleanUid, deviceId: cleanDeviceId, db });
    const now = Date.now();
    const createdAt = existing && typeof existing.createdAt === 'number' ? existing.createdAt : now;

    const record = {
        user_id: cleanUid,
        device_id: cleanDeviceId,
        fcm_token: cleanFcmToken,
        platform: normalizedPlatform,
        enabled: true,
        created_at: createdAt,
        updated_at: now,
        last_seen_at: now
    };

    if (typeof db.from === 'function') {
        const { error } = await db.from('devices').upsert(record, { onConflict: 'user_id,device_id' });
        if (error) {
            console.error('[DeviceRegistry] Upsert failed:', error.message);
            const err = new Error(error.message);
            err.statusCode = 500;
            throw err;
        }
    } else {
        // Fallback for custom db injection
        const err = new Error('Unsupported database interface');
        err.statusCode = 500;
        throw err;
    }

    // Return safe presentation payload omitting raw FCM token
    return {
        deviceId: cleanDeviceId,
        platform: normalizedPlatform,
        enabled: true,
        createdAt,
        updatedAt: now,
        lastSeenAt: now
    };
}

/**
 * Retrieves a registered device record under devices table.
 */
async function getDevice({ uid, deviceId, db = getAdminClient() }) {
    if (!uid || !deviceId || !db) return null;
    const cleanUid = String(uid).trim();
    const cleanDeviceId = String(deviceId).trim();

    if (typeof db.from === 'function') {
        const { data, error } = await db
            .from('devices')
            .select('*')
            .eq('user_id', cleanUid)
            .eq('device_id', cleanDeviceId)
            .maybeSingle();

        if (error || !data) return null;

        return {
            userId: data.user_id,
            deviceId: data.device_id,
            fcmToken: data.fcm_token,
            platform: data.platform,
            enabled: data.enabled,
            createdAt: typeof data.created_at === 'string' ? Number(data.created_at) : data.created_at,
            updatedAt: typeof data.updated_at === 'string' ? Number(data.updated_at) : data.updated_at,
            lastSeenAt: typeof data.last_seen_at === 'string' ? Number(data.last_seen_at) : data.last_seen_at
        };
    }

    return null;
}

/**
 * Checks whether a device is registered and currently enabled.
 */
async function isDeviceEnabled({ uid, deviceId, db = getAdminClient() }) {
    const device = await getDevice({ uid, deviceId, db });
    return Boolean(device && device.enabled === true);
}

module.exports = {
    ALLOWED_PLATFORMS,
    validateRegistrationInput,
    registerDevice,
    getDevice,
    isDeviceEnabled
};
