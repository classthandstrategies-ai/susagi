const { getAuthClient, isConfigured } = require('../services/supabase');

/**
 * Express middleware to verify Supabase JWT access tokens.
 *
 * Expected Header:
 *   Authorization: Bearer <Supabase access token>
 *
 * Behavior:
 * - Missing or malformed header -> HTTP 401 Unauthorized
 * - Empty token -> HTTP 401 Unauthorized
 * - Unconfigured Supabase -> HTTP 401 Fail Closed
 * - Valid token -> Attaches resolved user to req.user = { uid: user.id }
 * - Expired/Invalid token -> HTTP 401 Unauthorized
 * - Never trusts client-supplied user IDs from request body when authenticated UID is present
 */
async function verifySupabaseAuth(req, res, next) {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({
            error: 'Unauthorized',
            message: 'Missing or malformed Authorization header. Expected Bearer <token>'
        });
    }

    const token = authHeader.split('Bearer ')[1]?.trim();

    if (!token) {
        return res.status(401).json({
            error: 'Unauthorized',
            message: 'Empty Bearer token provided'
        });
    }

    // Fail closed if Supabase service credentials are not configured
    if (!isConfigured()) {
        return res.status(401).json({
            error: 'Unauthorized',
            message: 'Authentication service unavailable: Supabase credentials not configured.'
        });
    }

    const authClient = getAuthClient();
    if (!authClient) {
        return res.status(401).json({
            error: 'Unauthorized',
            message: 'Authentication service unavailable: Supabase auth client not initialized.'
        });
    }

    try {
        const { data, error } = await authClient.auth.getUser(token);

        if (error || !data || !data.user) {
            return res.status(401).json({
                error: 'Unauthorized',
                message: error?.message || 'Invalid or expired Supabase access token'
            });
        }

        // Attach canonical user identity
        req.user = {
            uid: data.user.id
        };

        next();
    } catch (err) {
        console.error('[Supabase Auth] Token verification error:', err.message);
        return res.status(401).json({
            error: 'Unauthorized',
            message: 'Invalid or expired Supabase access token'
        });
    }
}

module.exports = {
    verifySupabaseAuth
};
