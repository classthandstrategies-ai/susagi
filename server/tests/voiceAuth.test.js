const { describe, it, afterEach } = require('node:test');
const assert = require('node:assert');
const express = require('express');
const voiceAuthRouter = require('../routes/voiceAuth');

const servers = [];

function startApp({ token = 'internal-secret', fetchImpl } = {}) {
    const app = express();
    app.use(express.json({ limit: '320kb' }));
    app.locals.authMiddleware = (req, res, next) => {
        if (req.headers.authorization !== 'Bearer user-supabase-jwt') {
            return res.status(401).json({ error: 'Unauthorized' });
        }
        req.user = { uid: 'supabase-user-1' };
        next();
    };
    app.locals.voiceAuthServiceUrl = 'https://voice-auth.internal/analyze';
    app.locals.voiceAuthServiceToken = token;
    app.locals.voiceAuthFetch = fetchImpl;
    app.use('/api/v1/voice-auth', voiceAuthRouter);

    return new Promise((resolve) => {
        const server = app.listen(0, '127.0.0.1', () => {
            servers.push(server);
            resolve(server);
        });
    });
}

async function post(server, body, auth = 'Bearer user-supabase-jwt') {
    const { port } = server.address();
    return fetch(`http://127.0.0.1:${port}/api/v1/voice-auth/analyze`, {
        method: 'POST',
        headers: { 'Authorization': auth, 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
}

function payload() {
    return {
        audio_b64: Buffer.alloc(3200).toString('base64'),
        sample_rate: 16000,
        channels: 1,
        format: 'pcm16'
    };
}

afterEach(() => {
    while (servers.length) servers.pop().close();
});

describe('voice authenticity backend proxy', () => {
    it('requires a Supabase-authenticated request', async () => {
        const server = await startApp({ fetchImpl: async () => assert.fail('upstream must not be called') });
        const response = await post(server, payload(), '');
        assert.strictEqual(response.status, 401);
    });

    it('uses only the server-side model credential upstream', async () => {
        let upstreamAuth = null;
        const server = await startApp({
            fetchImpl: async (_url, options) => {
                upstreamAuth = options.headers.Authorization;
                return {
                    ok: true,
                    async json() {
                        return {
                            synthetic_probability: 0.2,
                            confidence: 0.7,
                            model_version: 'aasist-test',
                            inference_ms: 12
                        };
                    }
                };
            }
        });

        const response = await post(server, payload());
        assert.strictEqual(response.status, 200);
        assert.strictEqual(upstreamAuth, 'Bearer internal-secret');
        assert.notStrictEqual(upstreamAuth, 'Bearer user-supabase-jwt');
    });

    it('fails closed when the internal service credential is missing', async () => {
        const server = await startApp({
            token: '',
            fetchImpl: async () => assert.fail('upstream must not be called')
        });
        const response = await post(server, payload());
        assert.strictEqual(response.status, 503);
    });

    it('rejects malformed audio before proxying', async () => {
        const server = await startApp({ fetchImpl: async () => assert.fail('upstream must not be called') });
        const bad = payload();
        bad.sample_rate = 48000;
        const response = await post(server, bad);
        assert.strictEqual(response.status, 400);
    });

    it('never returns raw audio in the response', async () => {
        const server = await startApp({
            fetchImpl: async () => ({
                ok: true,
                async json() {
                    return {
                        synthetic_probability: 0.8,
                        confidence: 0.9,
                        model_version: 'aasist-test',
                        inference_ms: 5,
                        audio_b64: 'must-not-leak'
                    };
                }
            })
        });
        const response = await post(server, payload());
        const data = await response.json();
        assert.strictEqual(response.status, 200);
        assert.strictEqual(data.audio_b64, undefined);
    });
});
