const express = require('express');
const { verifySupabaseAuth } = require('../middleware/supabaseAuth');

const router = express.Router();
const MAX_BASE64_CHARS = 260000;

const authenticate = (req, res, next) => {
    const customAuth = req.app?.locals?.authMiddleware;
    if (typeof customAuth === 'function') {
        return customAuth(req, res, next);
    }
    return verifySupabaseAuth(req, res, next);
};

function validatePayload(body) {
    if (!body || typeof body !== 'object') return 'JSON body is required';
    if (typeof body.audio_b64 !== 'string' || body.audio_b64.length === 0) return 'audio_b64 is required';
    if (body.audio_b64.length > MAX_BASE64_CHARS) return 'audio_b64 exceeds maximum size';
    if (body.sample_rate !== 16000) return 'sample_rate must be exactly 16000';
    if (body.channels !== 1) return 'channels must be 1';
    if (body.format !== 'pcm16') return 'format must be pcm16';
    return null;
}

function serviceConfig(req) {
    return {
        url: req.app?.locals?.voiceAuthServiceUrl || process.env.VOICE_AUTH_SERVICE_URL || '',
        token: req.app?.locals?.voiceAuthServiceToken ?? process.env.VOICE_AUTH_SERVICE_TOKEN ?? '',
        fetchImpl: req.app?.locals?.voiceAuthFetch || global.fetch
    };
}

router.post('/analyze', authenticate, async (req, res) => {
    const uid = req.user?.uid;
    if (!uid) {
        return res.status(401).json({ error: 'Unauthorized', message: 'Authenticated user ID is missing' });
    }

    const validationError = validatePayload(req.body);
    if (validationError) {
        return res.status(validationError.includes('maximum size') ? 413 : 400).json({
            error: 'BadRequest',
            message: validationError
        });
    }

    const config = serviceConfig(req);
    if (!config.url || !config.token || typeof config.fetchImpl !== 'function') {
        return res.status(503).json({
            error: 'ServiceUnavailable',
            message: 'Voice authenticity service is not configured'
        });
    }

    if (
        process.env.NODE_ENV === 'production'
        && !config.url.startsWith('https://')
        && !config.url.startsWith('http://voice-auth:')
    ) {
        return res.status(503).json({
            error: 'ServiceUnavailable',
            message: 'Voice authenticity service must use HTTPS outside the private service network'
        });
    }

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 8000);

    try {
        const upstream = await config.fetchImpl(config.url, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${config.token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(req.body),
            signal: controller.signal
        });

        let data = {};
        try { data = await upstream.json(); } catch (_) { data = {}; }

        if (!upstream.ok) {
            return res.status(502).json({
                error: 'BadGateway',
                message: 'Voice authenticity inference service unavailable'
            });
        }

        const syntheticProbability = Number(data.synthetic_probability);
        const confidence = Number(data.confidence);
        if (
            !Number.isFinite(syntheticProbability) || syntheticProbability < 0 || syntheticProbability > 1
            || !Number.isFinite(confidence) || confidence < 0 || confidence > 1
        ) {
            return res.status(502).json({
                error: 'BadGateway',
                message: 'Malformed voice authenticity response'
            });
        }

        return res.status(200).json({
            synthetic_probability: syntheticProbability,
            confidence,
            model_version: String(data.model_version || 'unknown'),
            model_source_commit: String(data.model_source_commit || ''),
            checkpoint_filename: String(data.checkpoint_filename || ''),
            checkpoint_sha256: String(data.checkpoint_sha256 || ''),
            inference_started_at_ms: Number(data.inference_started_at_ms || 0),
            inference_finished_at_ms: Number(data.inference_finished_at_ms || 0),
            inference_ms: Number(data.inference_ms || 0)
        });
    } catch (err) {
        return res.status(502).json({
            error: 'BadGateway',
            message: err?.name === 'AbortError'
                ? 'Voice authenticity inference timed out'
                : 'Voice authenticity inference service unavailable'
        });
    } finally {
        clearTimeout(timeout);
    }
});

module.exports = router;
module.exports.validatePayload = validatePayload;
