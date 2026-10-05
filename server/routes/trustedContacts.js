const express = require('express');
const router = express.Router();
const { verifySupabaseAuth } = require('../middleware/supabaseAuth');
const { listTrustedContacts } = require('../services/trustedContacts');

/**
 * Middleware resolver supporting custom injection for tests while defaulting
 * strictly to verifySupabaseAuth in production.
 */
const authenticate = (req, res, next) => {
    const customAuth = req.app?.locals?.authMiddleware;
    if (typeof customAuth === 'function') {
        return customAuth(req, res, next);
    }
    return verifySupabaseAuth(req, res, next);
};

/**
 * GET /api/v1/trusted-contacts
 * Authenticated read-only endpoint returning canonical trusted contacts for the caller.
 *
 * Header:
 *   Authorization: Bearer <Supabase access token>
 *
 * Security:
 * - Protected-user identity is derived exclusively from req.user.uid.
 * - Client-supplied protectedUserId in query/body/path is ignored and never trusted.
 * - Returns only authorized, enabled relationships.
 */
router.get('/', authenticate, async (req, res) => {
    try {
        const protectedUid = req.user?.uid;
        if (!protectedUid) {
            return res.status(401).json({
                error: 'Unauthorized',
                message: 'Authenticated user ID is missing'
            });
        }

        const db = req.app?.locals?.db;

        const contacts = await listTrustedContacts({
            protectedUid,
            enabledOnly: true,
            db
        });

        return res.status(200).json({
            success: true,
            contacts
        });
    } catch (err) {
        const statusCode = err.statusCode || 500;
        return res.status(statusCode).json({
            error: statusCode >= 500 ? 'InternalServerError' : 'BadRequest',
            message: err.message
        });
    }
});

module.exports = router;
