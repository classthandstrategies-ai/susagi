const crypto = require('crypto');
const { firestore } = require('./firebase');
const trustedContacts = require('./trustedContacts');
const deviceRegistry = require('./deviceRegistry');

const DEFAULT_TTL_MS = 60 * 1000; // 60 seconds V1 TTL
const ALLOWED_RESPONSES = new Set(['VERIFIED', 'REJECTED']);
const TERMINAL_STATUSES = new Set(['VERIFIED', 'REJECTED', 'EXPIRED', 'UNAVAILABLE']);
const CANONICAL_COLLECTION = 'verificationSessions';

/**
 * Validates inputs for session creation.
 */
function validateCreateInput({
    protectedUserId,
    trustedUserId,
    claimedIdentity,
    requestedAction,
    requestSummary,
    riskScoreAtCreation
}) {
    if (!protectedUserId || typeof protectedUserId !== 'string' || protectedUserId.trim() === '') {
        const error = new Error('Authenticated protectedUserId is required');
        error.statusCode = 401;
        throw error;
    }

    if (!trustedUserId || typeof trustedUserId !== 'string' || trustedUserId.trim() === '') {
        const error = new Error('trustedUserId is required and must be a non-empty string');
        error.statusCode = 400;
        throw error;
    }

    const cleanProtected = protectedUserId.trim();
    const cleanTrusted = trustedUserId.trim();

    if (cleanProtected === cleanTrusted) {
        const error = new Error('Self-verification is forbidden: user cannot verify their own incident');
        error.statusCode = 400;
        throw error;
    }

    if (!claimedIdentity || typeof claimedIdentity !== 'string' || claimedIdentity.trim() === '') {
        const error = new Error('claimedIdentity is required and must be a non-empty string');
        error.statusCode = 400;
        throw error;
    }

    if (!requestedAction || typeof requestedAction !== 'string' || requestedAction.trim() === '') {
        const error = new Error('requestedAction is required and must be a non-empty string');
        error.statusCode = 400;
        throw error;
    }

    if (!requestSummary || typeof requestSummary !== 'string' || requestSummary.trim() === '') {
        const error = new Error('requestSummary is required and must be a non-empty string');
        error.statusCode = 400;
        throw error;
    }

    const numScore = Number(riskScoreAtCreation);
    if (isNaN(numScore) || numScore < 0 || numScore > 100) {
        const error = new Error('riskScoreAtCreation must be an integer between 0 and 100');
        error.statusCode = 400;
        throw error;
    }

    return {
        cleanProtected,
        cleanTrusted,
        cleanIdentity: claimedIdentity.trim(),
        cleanAction: requestedAction.trim(),
        cleanSummary: requestSummary.trim(),
        score: Math.round(numScore)
    };
}

/**
 * Creates an authoritative VerificationSession in Firestore.
 *
 * Rules:
 * - protectedUserId is bound strictly to req.user.uid.
 * - trustedUserId must be an enabled trusted contact of protectedUserId (else 403).
 * - Canonical collection: verificationSessions/{sessionId}
 * - Server controls: id, protectedUserId, status (PENDING), createdAt, expiresAt, version (1).
 */
async function createSession({
    protectedUserId,
    trustedUserId,
    claimedIdentity,
    requestedAction,
    requestSummary,
    riskScoreAtCreation,
    ttlMs = DEFAULT_TTL_MS,
    db = firestore
}) {
    const {
        cleanProtected,
        cleanTrusted,
        cleanIdentity,
        cleanAction,
        cleanSummary,
        score
    } = validateCreateInput({
        protectedUserId,
        trustedUserId,
        claimedIdentity,
        requestedAction,
        requestSummary,
        riskScoreAtCreation
    });

    if (!db) {
        const error = new Error('Database service unavailable: Firestore is not configured');
        error.statusCode = 503;
        throw error;
    }

    // Verify authorized relationship
    const isAuthorized = await trustedContacts.isTrustedContact({
        protectedUid: cleanProtected,
        trustedUid: cleanTrusted,
        db
    });

    if (!isAuthorized) {
        const error = new Error('Forbidden: target is not an authorized, enabled trusted contact');
        error.statusCode = 403;
        throw error;
    }

    const now = Date.now();
    const sessionId = `sess_${now}_${crypto.randomBytes(4).toString('hex')}`;
    const expiresAt = now + ttlMs;

    const session = {
        id: sessionId,
        protectedUserId: cleanProtected,
        trustedUserId: cleanTrusted,
        claimedIdentity: cleanIdentity,
        requestedAction: cleanAction,
        requestSummary: cleanSummary,
        riskScoreAtCreation: score,
        status: 'PENDING',
        createdAt: now,
        expiresAt,
        respondedAt: null,
        responseDeviceId: null,
        version: 1
    };

    const sessionRef = db.collection(CANONICAL_COLLECTION).doc(sessionId);
    await sessionRef.set(session);

    return session;
}

/**
 * Submits an authenticated trusted contact response with transactional first-terminal-state-wins semantics.
 *
 * Rules:
 * - Caller MUST be the session.trustedUserId (else 403).
 * - Response must be VERIFIED or REJECTED (else 400).
 * - Responding device MUST be registered and enabled under users/{callerUid}/devices/{deviceId} (else 403).
 * - If session is already terminal: first terminal state wins. Idempotent on identical replay.
 * - If session is PENDING but current time >= expiresAt: transitions to EXPIRED, never REJECTED.
 * - If session is PENDING and not expired: transitions to VERIFIED or REJECTED.
 */
async function respondToSession({
    sessionId,
    callerUid,
    response,
    deviceId,
    now = Date.now(),
    db = firestore
}) {
    if (!sessionId || typeof sessionId !== 'string' || sessionId.trim() === '') {
        const error = new Error('sessionId is required');
        error.statusCode = 400;
        throw error;
    }

    if (!callerUid || typeof callerUid !== 'string' || callerUid.trim() === '') {
        const error = new Error('Authenticated callerUid is required');
        error.statusCode = 401;
        throw error;
    }

    if (!response || typeof response !== 'string') {
        const error = new Error('response is required and must be either "VERIFIED" or "REJECTED"');
        error.statusCode = 400;
        throw error;
    }

    const normalizedResponse = response.trim().toUpperCase();
    if (!ALLOWED_RESPONSES.has(normalizedResponse)) {
        const error = new Error(`Invalid response "${response}". Only "VERIFIED" and "REJECTED" are allowed`);
        error.statusCode = 400;
        throw error;
    }

    if (!deviceId || typeof deviceId !== 'string' || deviceId.trim() === '') {
        const error = new Error('deviceId is required and must be a non-empty string');
        error.statusCode = 400;
        throw error;
    }

    if (!db) {
        const error = new Error('Database service unavailable: Firestore is not configured');
        error.statusCode = 503;
        throw error;
    }

    const cleanSessionId = sessionId.trim();
    const cleanCallerUid = callerUid.trim();
    const cleanDeviceId = deviceId.trim();

    return await db.runTransaction(async (transaction) => {
        const sessionRef = db.collection(CANONICAL_COLLECTION).doc(cleanSessionId);
        const sessionDoc = await transaction.get(sessionRef);

        if (!sessionDoc.exists) {
            const error = new Error(`Verification session "${cleanSessionId}" not found`);
            error.statusCode = 404;
            throw error;
        }

        const session = sessionDoc.data();

        // Check caller authority
        if (session.trustedUserId !== cleanCallerUid) {
            const error = new Error('Forbidden: only the designated trusted contact can respond to this session');
            error.statusCode = 403;
            throw error;
        }

        // Verify responding device belongs to caller and is enabled
        const deviceRef = db
            .collection('users')
            .doc(cleanCallerUid)
            .collection('devices')
            .doc(cleanDeviceId);

        const deviceDoc = await transaction.get(deviceRef);
        if (!deviceDoc.exists || !deviceDoc.data()?.enabled) {
            const error = new Error('Forbidden: responding device is not registered or enabled for authenticated user');
            error.statusCode = 403;
            throw error;
        }

        // Check if session is already in a terminal state (FIRST TERMINAL STATE WINS)
        if (TERMINAL_STATUSES.has(session.status)) {
            // Terminal state is immutable. Return existing state without modification.
            return session;
        }

        // If PENDING, check server-authoritative expiry
        if (now >= session.expiresAt) {
            // Timeout always becomes EXPIRED, never REJECTED
            const expiredSession = {
                ...session,
                status: 'EXPIRED',
                version: (session.version || 1) + 1
            };
            transaction.update(sessionRef, {
                status: 'EXPIRED',
                version: expiredSession.version
            });
            return expiredSession;
        }

        // Apply authorized terminal transition
        const updatedSession = {
            ...session,
            status: normalizedResponse,
            respondedAt: now,
            responseDeviceId: cleanDeviceId,
            version: (session.version || 1) + 1
        };

        transaction.update(sessionRef, {
            status: normalizedResponse,
            respondedAt: now,
            responseDeviceId: cleanDeviceId,
            version: updatedSession.version
        });

        return updatedSession;
    });
}

/**
 * Retrieves a VerificationSession with lazy authoritative expiry.
 *
 * Rules:
 * - Reader must be protectedUserId or trustedUserId (else 403).
 * - If PENDING and current time >= expiresAt: transitions to EXPIRED in Firestore.
 */
async function getSession({ sessionId, callerUid, now = Date.now(), db = firestore }) {
    if (!sessionId || typeof sessionId !== 'string') {
        const error = new Error('sessionId is required');
        error.statusCode = 400;
        throw error;
    }

    if (!callerUid || typeof callerUid !== 'string') {
        const error = new Error('Authenticated callerUid is required');
        error.statusCode = 401;
        throw error;
    }

    if (!db) {
        const error = new Error('Database service unavailable: Firestore is not configured');
        error.statusCode = 503;
        throw error;
    }

    const cleanSessionId = sessionId.trim();
    const cleanCallerUid = callerUid.trim();
    const sessionRef = db.collection(CANONICAL_COLLECTION).doc(cleanSessionId);

    const doc = await sessionRef.get();
    if (!doc.exists) {
        const error = new Error(`Verification session "${cleanSessionId}" not found`);
        error.statusCode = 404;
        throw error;
    }

    let session = doc.data();

    // Check authorization: reader must be protectedUserId or trustedUserId
    if (session.protectedUserId !== cleanCallerUid && session.trustedUserId !== cleanCallerUid) {
        const error = new Error('Forbidden: not authorized to view this verification session');
        error.statusCode = 403;
        throw error;
    }

    // Lazy authoritative expiry: If PENDING and expiresAt elapsed, transition to EXPIRED
    if (session.status === 'PENDING' && now >= session.expiresAt) {
        try {
            await db.runTransaction(async (transaction) => {
                const freshDoc = await transaction.get(sessionRef);
                if (!freshDoc.exists) return;
                const freshData = freshDoc.data();
                if (freshData.status === 'PENDING' && now >= freshData.expiresAt) {
                    const newVersion = (freshData.version || 1) + 1;
                    transaction.update(sessionRef, {
                        status: 'EXPIRED',
                        version: newVersion
                    });
                    session = {
                        ...freshData,
                        status: 'EXPIRED',
                        version: newVersion
                    };
                } else {
                    session = freshData;
                }
            });
        } catch (err) {
            console.error('[VerificationService] Lazy expiry transition error:', err.message);
        }
    }

    return session;
}

module.exports = {
    DEFAULT_TTL_MS,
    ALLOWED_RESPONSES,
    TERMINAL_STATUSES,
    CANONICAL_COLLECTION,
    validateCreateInput,
    createSession,
    respondToSession,
    getSession
};
