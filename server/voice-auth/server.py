"""
SuSagi private voice-authenticity inference service.

Android never calls this service directly. Public flow:
Android -> Supabase-authenticated SuSagi backend -> private AASIST service.
No raw call audio is logged or persisted.
"""
from __future__ import annotations

import base64
import binascii
import logging
import os
import struct
from typing import Optional

import numpy as np
from flask import Flask, jsonify, request

from aasist_detector import (
    AASISTDetector,
    AASIST_CHECKPOINT_FILENAME,
    AASIST_LICENSE,
    AASIST_SOURCE_COMMIT,
    AASIST_SOURCE_REPOSITORY,
    AASIST_TRAINING_DATASET,
    TARGET_SAMPLE_RATE,
)

MAX_AUDIO_SECONDS = 6
MAX_RAW_AUDIO_BYTES = TARGET_SAMPLE_RATE * MAX_AUDIO_SECONDS * 2
MAX_JSON_BYTES = 300 * 1024

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
logger = logging.getLogger("voice-auth")
_detector: Optional[AASISTDetector] = None

def get_detector() -> AASISTDetector:
    global _detector
    if _detector is None:
        _detector = AASISTDetector()
        logger.info(
            "Loaded official AASIST source_commit=%s checkpoint=%s sha256=%s device=%s",
            AASIST_SOURCE_COMMIT, AASIST_CHECKPOINT_FILENAME,
            _detector.checkpoint_sha256, _detector.device,
        )
    return _detector

def create_app(detector: Optional[AASISTDetector] = None, service_token: Optional[str] = None) -> Flask:
    app = Flask(__name__)
    app.config["MAX_CONTENT_LENGTH"] = MAX_JSON_BYTES
    app.config["VOICE_AUTH_SERVICE_TOKEN"] = (
        service_token if service_token is not None else os.environ.get("VOICE_AUTH_SERVICE_TOKEN", "")
    )
    if detector is not None:
        global _detector
        _detector = detector

    def require_internal_auth():
        configured = app.config.get("VOICE_AUTH_SERVICE_TOKEN", "")
        if not configured:
            logger.error("VOICE_AUTH_SERVICE_TOKEN is not configured; failing closed")
            return jsonify({"error": "Service authentication is not configured"}), 503
        auth_header = request.headers.get("Authorization", "")
        if not auth_header.startswith("Bearer "):
            return jsonify({"error": "Unauthorized"}), 401
        supplied = auth_header[7:].strip()
        if not supplied or supplied != configured:
            return jsonify({"error": "Unauthorized"}), 401
        return None

    @app.get("/health")
    def health():
        try:
            model = get_detector()
            return jsonify({
                "status": "ok",
                "model_loaded": model.loaded,
                "model_version": model.model_version,
                "model_source": AASIST_SOURCE_REPOSITORY,
                "model_source_commit": AASIST_SOURCE_COMMIT,
                "checkpoint_filename": AASIST_CHECKPOINT_FILENAME,
                "checkpoint_sha256": model.checkpoint_sha256,
                "training_dataset": AASIST_TRAINING_DATASET,
                "license": AASIST_LICENSE,
            })
        except Exception as exc:
            logger.error("Model health check failed: %s", exc)
            return jsonify({"status": "unavailable", "model_loaded": False}), 503

    @app.post("/analyze")
    def analyze():
        auth_error = require_internal_auth()
        if auth_error is not None:
            return auth_error
        if not request.is_json:
            return jsonify({"error": "Content-Type must be application/json"}), 400
        data = request.get_json(silent=True)
        if not isinstance(data, dict):
            return jsonify({"error": "Invalid JSON payload"}), 400

        audio_b64 = data.get("audio_b64")
        sample_rate = data.get("sample_rate")
        channels = data.get("channels")
        fmt = data.get("format")

        if not isinstance(audio_b64, str) or not audio_b64:
            return jsonify({"error": "Missing audio_b64 field"}), 400
        if fmt != "pcm16":
            return jsonify({"error": "Only pcm16 format is supported"}), 400
        if channels != 1:
            return jsonify({"error": "Only mono audio (channels=1) is supported"}), 400
        if sample_rate != TARGET_SAMPLE_RATE:
            return jsonify({"error": f"sample_rate must be exactly {TARGET_SAMPLE_RATE}"}), 400

        try:
            audio_bytes = base64.b64decode(audio_b64, validate=True)
        except (binascii.Error, ValueError):
            return jsonify({"error": "Invalid base64 encoding in audio_b64"}), 400

        if len(audio_bytes) < 320:
            return jsonify({"error": "Audio too short"}), 400
        if len(audio_bytes) > MAX_RAW_AUDIO_BYTES:
            return jsonify({"error": "Audio exceeds maximum size"}), 413
        if len(audio_bytes) % 2 != 0:
            return jsonify({"error": "PCM16 payload must contain an even byte count"}), 400

        try:
            n_samples = len(audio_bytes) // 2
            audio = np.array(struct.unpack(f"<{n_samples}h", audio_bytes), dtype=np.float32)
            audio /= 32768.0
        except Exception:
            return jsonify({"error": "Failed to decode PCM16 audio"}), 400

        try:
            result = get_detector().analyze(audio, sample_rate)
        except Exception as exc:
            logger.error("AASIST inference failed: %s", exc)
            return jsonify({"error": "Inference unavailable"}), 503

        logger.info(
            "AASIST inference complete synthetic_probability=%.4f confidence=%.4f inference_ms=%s",
            result["synthetic_probability"], result["confidence"], result["inference_ms"],
        )
        return jsonify(result)

    return app

app = create_app()

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8090))
    get_detector()
    app.run(host="0.0.0.0", port=port, debug=False)
