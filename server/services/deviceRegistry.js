const { firestore, isMockMode } = require('./firebase');

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
 * Registers or updates an authenticated device in canonical Firestore:
 *   users/{uid}/devices/{deviceId}
 *
 * Rules:
 * - Ownership bound strictly to authenticated uid.
 * - Idempotently updates same document if deviceId already exists.
 * - Preserves original createdAt timestamp on re-registration.
 * - Updates updatedAt and lastSeenAt.
 * - Never returns raw FCM token in response.
 * - Never logs raw FCM token.
 */
async function registerDevice({ uid, deviceId, fcmToken, platform, db = firestore }) {
    const { cleanUid, cleanDeviceId, cleanFcmToken, normalizedPlatform } =
        validateRegistrationInput({ uid, deviceId, fcmToken, platform });

    if (!db) {
        const error = new Error('Database service unavailable: Firestore is not configured');
        error.statusCode = 503;
        throw error;
    }

    const maskedToken = cleanFcmToken.length > 8
        ? `${cleanFcmToken.substring(0, 4)}...${cleanFcmToken.substring(cleanFcmToken.length - 4)}`
        : '***';

    console.log(`[DeviceRegistry] Registering device "${cleanDeviceId}" (${normalizedPlatform}) for user "${cleanUid}", token: ${maskedToken}`);

    const deviceRef = db.collection('users').doc(cleanUid).collection('devices').doc(cleanDeviceId);
    const existingDoc = await deviceRef.get();
    const now = Date.now();

    let createdAt = now;
    if (existingDoc.exists) {
        const existingData = existingDoc.data();
        if (existingData && typeof existingData.createdAt === 'number') {
            createdAt = existingData.createdAt;
        }
    }

    const record = {
        deviceId: cleanDeviceId,
        fcmToken: cleanFcmToken,
        platform: normalizedPlatform,
        enabled: true,
        createdAt,
        updatedAt: now,
        lastSeenAt: now
    };

    await deviceRef.set(record, { merge: true });

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
 * Retrieves a registered device record under users/{uid}/devices/{deviceId}.
 */
async function getDevice({ uid, deviceId, db = firestore }) {
    if (!uid || !deviceId || !db) return null;
    const docRef = db.collection('users').doc(String(uid).trim()).collection('devices').doc(String(deviceId).trim());
    const doc = await docRef.get();
    if (!doc.exists) return null;
    return doc.data();
}

/**
 * Checks whether a device is registered and currently enabled.
 */
async function isDeviceEnabled({ uid, deviceId, db = firestore }) {
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
