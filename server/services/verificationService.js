const crypto = require('crypto');
const { getAdminClient } = require('./supabase');
const trustedContacts = require('./trustedContacts');
const deviceRegistry = require('./deviceRegistry');

const DEFAULT_TTL_MS = 60 * 1000; // 60 seconds V1 TTL
const ALLOWED_RESPONSES = new Set(['VERIFIED', 'REJECTED']);
const TERMINAL_STATUSES = new Set(['VERIFIED', 'REJECTED', 'EXPIRED', 'UNAVAILABLE']);
const CANONICAL_TABLE = 'verification_sessions';

/**
 * Transforms a Supabase PostgreSQL row into a domain VerificationSession object.
 */
function rowToSession(row) {
    if (!row) return null;
    return {
        id: row.id,
        protectedUserId: row.protected_user_id,
        trustedUserId: row.trusted_user_id,
        claimedIdentity: row.claimed_identity,
        requestedAction: row.requested_action,
        requestSummary: row.request_summary,
        riskScoreAtCreation: Number(row.risk_score_at_creation),
        status: row.status,
        createdAt: typeof row.created_at === 'string' ? Number(row.created_at) : row.created_at,
        expiresAt: typeof row.expires_at === 'string' ? Number(row.expires_at) : row.expires_at,
        respondedAt: row.responded_at != null ? (typeof row.responded_at === 'string' ? Number(row.responded_at) : row.responded_at) : null,
        responseDeviceId: row.response_device_id || null,
        version: Number(row.version)
    };
}

/**
 * Helper to fetch a session by ID.
 */
async function fetchSessionById(sessionId, db) {
    if (!db || typeof db.from !== 'function') return null;
    const { data, error } = await db
        .from(CANONICAL_TABLE)
        .select('*')
        .eq('id', sessionId)
        .maybeSingle();

    if (error || !data) return null;
    return rowToSession(data);
}

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
 * Creates an authoritative VerificationSession in Supabase PostgreSQL.
 *
 * Rules:
 * - protectedUserId is bound strictly to req.user.uid.
 * - trustedUserId must be an enabled trusted contact of protectedUserId (else 403).
 * - Canonical table: verification_sessions
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
    db = getAdminClient()
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
        const error = new Error('Database service unavailable: Supabase is not configured');
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

    const row = {
        id: sessionId,
        protected_user_id: cleanProtected,
        trusted_user_id: cleanTrusted,
        claimed_identity: cleanIdentity,
        requested_action: cleanAction,
        request_summary: cleanSummary,
        risk_score_at_creation: score,
        status: 'PENDING',
        created_at: now,
        expires_at: expiresAt,
        responded_at: null,
        response_device_id: null,
        version: 1
    };

    if (typeof db.from === 'function') {
        const { data, error } = await db.from(CANONICAL_TABLE).insert(row).select().single();
        if (error) {
            console.error('[VerificationService] Insert session failed:', error.message);
            const err = new Error(error.message);
            err.statusCode = 500;
            throw err;
        }
        return rowToSession(data || row);
    } else {
        const err = new Error('Unsupported database interface');
        err.statusCode = 503;
        throw err;
    }
}

/**
 * Submits an authenticated trusted contact response with compare-and-set atomic
 * first-terminal-state-wins semantics.
 *
 * Rules:
 * - Caller MUST be the session.trustedUserId (else 403).
 * - Response must be VERIFIED or REJECTED (else 400).
 * - Responding device MUST be registered and enabled under devices table for caller (else 403).
 * - If session is already terminal: first terminal state wins. Idempotent on identical replay.
 * - If session is PENDING but current time >= expiresAt: atomically transitions to EXPIRED, never REJECTED.
 * - If session is PENDING and not expired: atomically transitions to VERIFIED or REJECTED.
 * - Atomic compare-and-set ensures that if concurrent transitions race, exactly one wins and losing requests re-read canonical state.
 */
async function respondToSession({
    sessionId,
    callerUid,
    response,
    deviceId,
    now = Date.now(),
    db = getAdminClient()
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
        const error = new Error('Database service unavailable: Supabase is not configured');
        error.statusCode = 503;
        throw error;
    }

    const cleanSessionId = sessionId.trim();
    const cleanCallerUid = callerUid.trim();
    const cleanDeviceId = deviceId.trim();

    // 1. Fetch current canonical state
    const session = await fetchSessionById(cleanSessionId, db);
    if (!session) {
        const error = new Error(`Verification session "${cleanSessionId}" not found`);
        error.statusCode = 404;
        throw error;
    }

    // 2. Check caller authority
    if (session.trustedUserId !== cleanCallerUid) {
        const error = new Error('Forbidden: only the designated trusted contact can respond to this session');
        error.statusCode = 403;
        throw error;
    }

    // 3. Verify responding device belongs to caller and is enabled
    const isDeviceEnabled = await deviceRegistry.isDeviceEnabled({
        uid: cleanCallerUid,
        deviceId: cleanDeviceId,
        db
    });

    if (!isDeviceEnabled) {
        const error = new Error('Forbidden: responding device is not registered or enabled for authenticated user');
        error.statusCode = 403;
        throw error;
    }

    // 4. Check if session is already in a terminal state (FIRST TERMINAL STATE WINS)
    if (TERMINAL_STATUSES.has(session.status)) {
        // Terminal state is immutable. Return existing state without modification.
        return session;
    }

    // 5. If PENDING, check server-authoritative expiry
    if (now >= session.expiresAt) {
        // Atomic compare-and-set: PENDING -> EXPIRED
        const { data, error } = await db
            .from(CANONICAL_TABLE)
            .update({
                status: 'EXPIRED',
                version: (session.version || 1) + 1
            })
            .eq('id', cleanSessionId)
            .eq('status', 'PENDING')
            .select();

        if (error) {
            console.error('[VerificationService] Atomic expiry CAS error:', error.message);
            const err = new Error(error.message);
            err.statusCode = 500;
            throw err;
        }

        if (data && data.length > 0) {
            return rowToSession(data[0]);
        }

        // Another request won concurrently; re-read and return canonical state
        const fresh = await fetchSessionById(cleanSessionId, db);
        return fresh || session;
    }

    // 6. Apply authorized terminal transition with atomic compare-and-set
    const { data, error } = await db
        .from(CANONICAL_TABLE)
        .update({
            status: normalizedResponse,
            responded_at: now,
            response_device_id: cleanDeviceId,
            version: (session.version || 1) + 1
        })
        .eq('id', cleanSessionId)
        .eq('status', 'PENDING')
        .select();

    if (error) {
        console.error('[VerificationService] Atomic response CAS error:', error.message);
        const err = new Error(error.message);
        err.statusCode = 500;
        throw err;
    }

    if (data && data.length > 0) {
        return rowToSession(data[0]);
    }

    // Another request won concurrently (e.g., competing response or expiry)
    // Re-read canonical terminal state and return it
    const canonical = await fetchSessionById(cleanSessionId, db);
    return canonical || session;
}

/**
 * Retrieves a VerificationSession with lazy authoritative expiry.
 *
 * Rules:
 * - Reader must be protectedUserId or trustedUserId (else 403).
 * - If PENDING and current time >= expiresAt: atomically transitions to EXPIRED in database.
 */
async function getSession({ sessionId, callerUid, now = Date.now(), db = getAdminClient() }) {
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
        const error = new Error('Database service unavailable: Supabase is not configured');
        error.statusCode = 503;
        throw error;
    }

    const cleanSessionId = sessionId.trim();
    const cleanCallerUid = callerUid.trim();

    const session = await fetchSessionById(cleanSessionId, db);
    if (!session) {
        const error = new Error(`Verification session "${cleanSessionId}" not found`);
        error.statusCode = 404;
        throw error;
    }

    // Check authorization: reader must be protectedUserId or trustedUserId
    if (session.protectedUserId !== cleanCallerUid && session.trustedUserId !== cleanCallerUid) {
        const error = new Error('Forbidden: not authorized to view this verification session');
        error.statusCode = 403;
        throw error;
    }

    // Lazy authoritative expiry: If PENDING and expiresAt elapsed, transition to EXPIRED atomically
    if (session.status === 'PENDING' && now >= session.expiresAt) {
        try {
            const { data, error } = await db
                .from(CANONICAL_TABLE)
                .update({
                    status: 'EXPIRED',
                    version: (session.version || 1) + 1
                })
                .eq('id', cleanSessionId)
                .eq('status', 'PENDING')
                .select();

            if (!error && data && data.length > 0) {
                return rowToSession(data[0]);
            }

            // If concurrent transition occurred, return fresh canonical session
            const fresh = await fetchSessionById(cleanSessionId, db);
            return fresh || session;
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
    CANONICAL_TABLE,
    rowToSession,
    validateCreateInput,
    createSession,
    respondToSession,
    getSession
};
