const { auth, isMockMode } = require('../services/firebase');

/**
 * Express middleware to verify Firebase ID tokens.
 *
 * Expected Header:
 *   Authorization: Bearer <Firebase ID token>
 *
 * Behavior:
 * - Missing or malformed header -> HTTP 401 Unauthorized
 * - Empty token -> HTTP 401 Unauthorized
 * - Valid token -> Attaches decoded token payload to req.user (e.g. req.user.uid)
 * - Expired/Invalid token -> HTTP 401 Unauthorized
 * - Never trusts client-supplied user IDs from request body when authenticated UID is present
 */
async function verifyFirebaseAuth(req, res, next) {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({
            error: 'Unauthorized',
            message: 'Missing or malformed Authorization header. Expected Bearer <token>'
        });
    }

    const idToken = authHeader.split('Bearer ')[1]?.trim();

    if (!idToken) {
        return res.status(401).json({
            error: 'Unauthorized',
            message: 'Empty Bearer token provided'
        });
    }

    // Fail closed: If Firebase Admin is not initialized with live credentials, protected routes reject access
    if (isMockMode || !auth) {
        return res.status(401).json({
            error: 'Unauthorized',
            message: 'Authentication service unavailable: Firebase Admin credentials not configured.'
        });
    }

    try {
        const decodedToken = await auth.verifyIdToken(idToken);
        req.user = decodedToken;
        next();
    } catch (err) {
        console.error('[Auth Middleware] Token verification failed:', err.message);
        return res.status(401).json({
            error: 'Unauthorized',
            message: 'Invalid or expired Firebase ID token',
            code: err.code || 'auth/invalid-token'
        });
    }
}

module.exports = {
    verifyFirebaseAuth
};
