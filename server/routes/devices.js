const express = require('express');
const router = express.Router();
const { verifySupabaseAuth } = require('../middleware/supabaseAuth');
const { registerDevice } = require('../services/deviceRegistry');

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
 * POST /api/v1/devices/register
 * Registers or updates a device for the authenticated user.
 *
 * Header:
 *   Authorization: Bearer <Supabase access token>
 *
 * Body:
 * {
 *   "deviceId": "device_123",
 *   "fcmToken": "fcm_token_xyz",
 *   "platform": "android"
 * }
 */
router.post('/register', authenticate, async (req, res) => {
    try {
        const uid = req.user?.uid;
        if (!uid) {
            return res.status(401).json({
                error: 'Unauthorized',
                message: 'Authenticated user ID is missing'
            });
        }

        const { deviceId, fcmToken, platform } = req.body || {};
        const db = req.app?.locals?.db;

        const device = await registerDevice({
            uid,
            deviceId,
            fcmToken,
            platform,
            db
        });

        return res.status(200).json({
            success: true,
            device
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
