const express = require('express');
const router = express.Router();
const { verifySupabaseAuth } = require('../middleware/supabaseAuth');
const verificationService = require('../services/verificationService');

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
 * POST /api/v1/verifications
 * Authoritatively creates a new VerificationSession.
 *
 * Header:
 *   Authorization: Bearer <Supabase access token>
 *
 * Body:
 * {
 *   "trustedUserId": "uid_trusted",
 *   "claimedIdentity": "Bank Manager",
 *   "requestedAction": "Transfer funds",
 *   "requestSummary": "High pressure demand for immediate fund transfer",
 *   "riskScoreAtCreation": 82
 * }
 */
router.post('/', authenticate, async (req, res) => {
    try {
        const protectedUserId = req.user?.uid;
        if (!protectedUserId) {
            return res.status(401).json({
                error: 'Unauthorized',
                message: 'Authenticated user ID is missing'
            });
        }

        const {
            trustedUserId,
            claimedIdentity,
            requestedAction,
            requestSummary,
            riskScoreAtCreation
        } = req.body || {};

        const db = req.app?.locals?.db;

        const session = await verificationService.createSession({
            protectedUserId, // Always bound strictly to req.user.uid; body protectedUserId ignored
            trustedUserId,
            claimedIdentity,
            requestedAction,
            requestSummary,
            riskScoreAtCreation,
            db
        });

        return res.status(201).json({
            success: true,
            session
        });
    } catch (err) {
        const statusCode = err.statusCode || 500;
        return res.status(statusCode).json({
            error: statusCode === 403 ? 'Forbidden' : statusCode >= 500 ? 'InternalServerError' : 'BadRequest',
            message: err.message
        });
    }
});

/**
 * POST /api/v1/verifications/:sessionId/respond
 * Authenticated trusted contact response with compare-and-set atomic first-terminal-state-wins semantics.
 *
 * Header:
 *   Authorization: Bearer <Supabase access token>
 *
 * Body:
 * {
 *   "response": "VERIFIED" | "REJECTED",
 *   "deviceId": "device_123"
 * }
 */
router.post('/:sessionId/respond', authenticate, async (req, res) => {
    try {
        const callerUid = req.user?.uid;
        if (!callerUid) {
            return res.status(401).json({
                error: 'Unauthorized',
                message: 'Authenticated user ID is missing'
            });
        }

        const { sessionId } = req.params;
        const { response, deviceId } = req.body || {};
        const db = req.app?.locals?.db;

        const session = await verificationService.respondToSession({
            sessionId,
            callerUid,
            response,
            deviceId,
            db
        });

        return res.status(200).json({
            success: true,
            session
        });
    } catch (err) {
        const statusCode = err.statusCode || 500;
        return res.status(statusCode).json({
            error: statusCode === 404 ? 'NotFound' : statusCode === 403 ? 'Forbidden' : statusCode >= 500 ? 'InternalServerError' : 'BadRequest',
            message: err.message
        });
    }
});

/**
 * GET /api/v1/verifications/:sessionId
 * Retrieves a VerificationSession with lazy authoritative expiry.
 *
 * Header:
 *   Authorization: Bearer <Supabase access token>
 */
router.get('/:sessionId', authenticate, async (req, res) => {
    try {
        const callerUid = req.user?.uid;
        if (!callerUid) {
            return res.status(401).json({
                error: 'Unauthorized',
                message: 'Authenticated user ID is missing'
            });
        }

        const { sessionId } = req.params;
        const db = req.app?.locals?.db;

        const session = await verificationService.getSession({
            sessionId,
            callerUid,
            db
        });

        return res.status(200).json({
            success: true,
            session
        });
    } catch (err) {
        const statusCode = err.statusCode || 500;
        return res.status(statusCode).json({
            error: statusCode === 404 ? 'NotFound' : statusCode === 403 ? 'Forbidden' : statusCode >= 500 ? 'InternalServerError' : 'BadRequest',
            message: err.message
        });
    }
});

module.exports = router;
