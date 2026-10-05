"""
SuSagi Voice Authenticity Inference Server

Receives short PCM16 audio windows (base64-encoded) and returns
voice authenticity assessments using an anti-spoof/deepfake detector.

Architecture:
  Android → sends short REMOTE audio windows (base64 PCM16)
  → this inference endpoint (AASIST-inspired model)
  → returns authenticity assessment JSON

Security:
  - API key authentication (Bearer token)
  - Request size limits
  - No raw audio logged or persisted
  - Rejects malformed/non-PCM payloads
"""

import base64
import io
import logging
import os
import struct
import time
from functools import wraps

import numpy as np
from flask import Flask, jsonify, request

# ── Configuration ──────────────────────────────────────────────
API_KEY = os.environ.get("VOICE_AUTH_API_KEY", "dev-key-do-not-use-in-production")
MAX_AUDIO_SECONDS = 6
MAX_SAMPLE_RATE = 48000
MAX_REQUEST_BYTES = 2 * MAX_SAMPLE_RATE * MAX_AUDIO_SECONDS * 2  # ~576KB
MODEL_VERSION = "aasist-lite-v0.1-prototype"

# ── Logging ─────────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("voice-auth")

# ── Flask App ───────────────────────────────────────────────────
app = Flask(__name__)
app.config["MAX_CONTENT_LENGTH"] = MAX_REQUEST_BYTES + 4096  # payload + JSON overhead


# ── Model Loading ───────────────────────────────────────────────
class AASISTLiteDetector:
    """
    Prototype anti-spoof detector inspired by AASIST/AASIST-L.

    For the first milestone this uses spectral feature analysis
    (spectral centroid, spectral flatness, harmonic-to-noise ratio)
    as a lightweight classifier. The full AASIST neural model
    will be integrated once PyTorch weights are validated.

    This is NOT production-grade and will produce uncertain results.
    Thresholds are configurable and NOT calibrated.
    """

    def __init__(self):
        self.loaded = True
        logger.info(f"AASISTLiteDetector initialized (version={MODEL_VERSION})")

    def analyze(self, audio: np.ndarray, sample_rate: int) -> dict:
        """
        Analyze audio for synthetic voice indicators.

        Returns:
            dict with keys:
                synthetic_probability: float [0,1]
                confidence: float [0,1]
                features: dict of extracted spectral features
        """
        start_time = time.time()

        if len(audio) < sample_rate * 0.5:
            return {
                "synthetic_probability": 0.5,
                "confidence": 0.0,
                "model_version": MODEL_VERSION,
                "features": {},
                "inference_ms": 0
            }

        # Resample to 16kHz if needed
        if sample_rate != 16000:
            try:
                import librosa
                audio = librosa.resample(audio, orig_sr=sample_rate, target_sr=16000)
                sample_rate = 16000
            except ImportError:
                # Fall back to simple decimation
                ratio = sample_rate // 16000
                if ratio > 1:
                    audio = audio[::ratio]
                    sample_rate = 16000

        # Normalize
        max_val = np.max(np.abs(audio))
        if max_val > 0:
            audio = audio / max_val

        # Extract spectral features
        features = self._extract_features(audio, sample_rate)

        # Simple heuristic classifier (placeholder for real AASIST model)
        syn_prob = self._classify(features)

        # Confidence is based on audio quality and duration
        duration_s = len(audio) / sample_rate
        confidence = min(0.85, duration_s / 4.0) * min(1.0, max_val * 10)

        inference_ms = int((time.time() - start_time) * 1000)

        return {
            "synthetic_probability": float(np.clip(syn_prob, 0, 1)),
            "confidence": float(np.clip(confidence, 0, 1)),
            "model_version": MODEL_VERSION,
            "features": {k: float(v) for k, v in features.items()},
            "inference_ms": inference_ms
        }

    def _extract_features(self, audio: np.ndarray, sr: int) -> dict:
        """Extract spectral features for classification."""
        features = {}

        # 1. Spectral centroid - synthetic speech often has different centroid patterns
        fft = np.fft.rfft(audio)
        magnitude = np.abs(fft)
        freqs = np.fft.rfftfreq(len(audio), 1.0 / sr)

        total_mag = np.sum(magnitude)
        if total_mag > 0:
            centroid = np.sum(freqs * magnitude) / total_mag
            features["spectral_centroid"] = centroid
        else:
            features["spectral_centroid"] = 0

        # 2. Spectral flatness - synthetic speech tends to be less "flat"
        geometric_mean = np.exp(np.mean(np.log(magnitude + 1e-10)))
        arithmetic_mean = np.mean(magnitude) + 1e-10
        features["spectral_flatness"] = geometric_mean / arithmetic_mean

        # 3. Zero crossing rate
        zcr = np.sum(np.abs(np.diff(np.sign(audio)))) / (2 * len(audio))
        features["zero_crossing_rate"] = zcr

        # 4. Spectral rolloff
        cumsum = np.cumsum(magnitude)
        if cumsum[-1] > 0:
            rolloff_idx = np.searchsorted(cumsum, 0.85 * cumsum[-1])
            features["spectral_rolloff"] = freqs[min(rolloff_idx, len(freqs) - 1)]
        else:
            features["spectral_rolloff"] = 0

        # 5. Short-term energy variance
        frame_size = sr // 20  # 50ms frames
        n_frames = max(1, len(audio) // frame_size)
        frame_energies = []
        for i in range(n_frames):
            frame = audio[i * frame_size : (i + 1) * frame_size]
            frame_energies.append(np.mean(frame ** 2))
        features["energy_variance"] = np.var(frame_energies) if frame_energies else 0

        # 6. Pitch stability (via autocorrelation)
        if len(audio) >= sr // 4:
            chunk = audio[:sr // 4]
            autocorr = np.correlate(chunk, chunk, mode="full")
            autocorr = autocorr[len(autocorr) // 2:]
            if len(autocorr) > sr // 500:
                # Find first major peak after initial decay
                start = sr // 500
                end = min(len(autocorr), sr // 50)
                if end > start and np.max(autocorr[start:end]) > 0:
                    peak_ratio = np.max(autocorr[start:end]) / (autocorr[0] + 1e-10)
                    features["pitch_stability"] = peak_ratio
                else:
                    features["pitch_stability"] = 0
            else:
                features["pitch_stability"] = 0
        else:
            features["pitch_stability"] = 0

        return features

    def _classify(self, features: dict) -> float:
        """
        Simple heuristic classifier.

        Synthetic speech indicators:
        - Very high spectral flatness (unnaturally uniform spectrum)
        - Very stable pitch (no natural micro-variations)
        - Low energy variance (unnaturally consistent)
        - Unusual spectral centroid

        This is a PROTOTYPE classifier. Not calibrated.
        """
        score = 0.5  # Start neutral

        # Spectral flatness: very flat = possibly synthetic
        flatness = features.get("spectral_flatness", 0.5)
        if flatness > 0.8:
            score += 0.15
        elif flatness < 0.1:
            score -= 0.05

        # Pitch stability: very stable = possibly synthetic
        pitch_stab = features.get("pitch_stability", 0.5)
        if pitch_stab > 0.85:
            score += 0.1
        elif pitch_stab < 0.3:
            score -= 0.1

        # Energy variance: very low = possibly synthetic
        energy_var = features.get("energy_variance", 0.01)
        if energy_var < 0.001:
            score += 0.1
        elif energy_var > 0.05:
            score -= 0.1

        # ZCR: unusual ranges
        zcr = features.get("zero_crossing_rate", 0.1)
        if zcr > 0.4 or zcr < 0.01:
            score += 0.05

        return np.clip(score, 0, 1)


# ── Initialize Model ───────────────────────────────────────────
detector = AASISTLiteDetector()


# ── Auth Decorator ──────────────────────────────────────────────
def require_auth(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        auth_header = request.headers.get("Authorization", "")
        if API_KEY and API_KEY != "dev-key-do-not-use-in-production":
            if not auth_header.startswith("Bearer ") or auth_header[7:] != API_KEY:
                return jsonify({"error": "Unauthorized"}), 401
        return f(*args, **kwargs)
    return decorated


# ── Routes ──────────────────────────────────────────────────────
@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "ok",
        "model_version": MODEL_VERSION,
        "model_loaded": detector.loaded
    })


@app.route("/analyze", methods=["POST"])
@require_auth
def analyze():
    """
    Analyze audio for voice authenticity.

    Request JSON:
        audio_b64: base64-encoded PCM16 little-endian audio bytes
        sample_rate: int (e.g., 16000)
        channels: int (must be 1)
        format: str (must be "pcm16")

    Response JSON:
        synthetic_probability: float [0,1]
        confidence: float [0,1]
        model_version: str
        inference_ms: int
    """
    if not request.is_json:
        return jsonify({"error": "Content-Type must be application/json"}), 400

    data = request.get_json(silent=True)
    if data is None:
        return jsonify({"error": "Invalid JSON payload"}), 400

    # Validate required fields
    audio_b64 = data.get("audio_b64")
    sample_rate = data.get("sample_rate", 16000)
    channels = data.get("channels", 1)
    fmt = data.get("format", "pcm16")

    if not audio_b64:
        return jsonify({"error": "Missing audio_b64 field"}), 400

    if fmt != "pcm16":
        return jsonify({"error": "Only pcm16 format is supported"}), 400

    if channels != 1:
        return jsonify({"error": "Only mono audio (channels=1) is supported"}), 400

    if not isinstance(sample_rate, int) or sample_rate < 8000 or sample_rate > MAX_SAMPLE_RATE:
        return jsonify({"error": f"sample_rate must be integer in [8000, {MAX_SAMPLE_RATE}]"}), 400

    # Decode audio
    try:
        audio_bytes = base64.b64decode(audio_b64)
    except Exception:
        return jsonify({"error": "Invalid base64 encoding in audio_b64"}), 400

    if len(audio_bytes) < 320:  # Minimum ~10ms at 16kHz
        return jsonify({"error": "Audio too short"}), 400

    if len(audio_bytes) > MAX_REQUEST_BYTES:
        return jsonify({"error": f"Audio exceeds maximum size ({MAX_REQUEST_BYTES} bytes)"}), 400

    # Convert PCM16 bytes to numpy array
    try:
        n_samples = len(audio_bytes) // 2
        audio = np.array(
            struct.unpack(f"<{n_samples}h", audio_bytes[:n_samples * 2]),
            dtype=np.float32
        )
        audio = audio / 32768.0  # Normalize to [-1, 1]
    except Exception as e:
        return jsonify({"error": f"Failed to decode PCM16 audio: {str(e)}"}), 400

    # Run inference
    try:
        result = detector.analyze(audio, sample_rate)
    except Exception as e:
        logger.error(f"Inference failed: {e}")
        return jsonify({
            "synthetic_probability": 0.5,
            "confidence": 0.0,
            "model_version": MODEL_VERSION,
            "error": "Inference failed"
        }), 200  # Return 200 with uncertain result

    # Do NOT log audio content
    logger.info(
        f"Analysis complete: syn_prob={result['synthetic_probability']:.3f}, "
        f"conf={result['confidence']:.3f}, "
        f"inference_ms={result.get('inference_ms', 0)}"
    )

    return jsonify(result)


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8090))
    logger.info(f"Starting Voice Authenticity Server on port {port}")
    app.run(host="0.0.0.0", port=port, debug=False)
