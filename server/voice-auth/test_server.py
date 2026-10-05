"""
Tests for the voice authenticity inference server.

Run: pytest test_server.py -v
"""
import base64
import json
import struct
import numpy as np
import pytest
from server import app


@pytest.fixture
def client():
    """Create test client."""
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client


def make_pcm16_audio(duration_s: float = 1.0, sample_rate: int = 16000, freq: float = 200.0, amplitude: float = 5000.0) -> bytes:
    """Generate PCM16 audio bytes."""
    n_samples = int(sample_rate * duration_s)
    t = np.arange(n_samples) / sample_rate
    signal = (amplitude * np.sin(2 * np.pi * freq * t)).astype(np.int16)
    return signal.tobytes()


def encode_audio(pcm_bytes: bytes) -> str:
    """Base64-encode audio bytes."""
    return base64.b64encode(pcm_bytes).decode("utf-8")


class TestHealthEndpoint:
    def test_health_returns_ok(self, client):
        response = client.get("/health")
        assert response.status_code == 200
        data = response.get_json()
        assert data["status"] == "ok"
        assert "model_version" in data
        assert data["model_loaded"] is True


class TestAnalyzeEndpoint:
    def test_valid_audio_returns_assessment(self, client):
        audio = make_pcm16_audio(duration_s=2.0)
        payload = {
            "audio_b64": encode_audio(audio),
            "sample_rate": 16000,
            "channels": 1,
            "format": "pcm16"
        }
        response = client.post("/analyze", json=payload)
        assert response.status_code == 200
        data = response.get_json()
        assert "synthetic_probability" in data
        assert "confidence" in data
        assert "model_version" in data
        assert 0 <= data["synthetic_probability"] <= 1
        assert 0 <= data["confidence"] <= 1

    def test_short_audio_returns_low_confidence(self, client):
        audio = make_pcm16_audio(duration_s=0.1)
        payload = {
            "audio_b64": encode_audio(audio),
            "sample_rate": 16000,
            "channels": 1,
            "format": "pcm16"
        }
        response = client.post("/analyze", json=payload)
        assert response.status_code == 200
        data = response.get_json()
        assert data["confidence"] == 0.0  # Too short

    def test_missing_audio_returns_400(self, client):
        response = client.post("/analyze", json={"sample_rate": 16000})
        assert response.status_code == 400

    def test_wrong_format_returns_400(self, client):
        audio = make_pcm16_audio()
        payload = {
            "audio_b64": encode_audio(audio),
            "sample_rate": 16000,
            "channels": 1,
            "format": "wav"  # Wrong format
        }
        response = client.post("/analyze", json=payload)
        assert response.status_code == 400

    def test_stereo_rejected(self, client):
        audio = make_pcm16_audio()
        payload = {
            "audio_b64": encode_audio(audio),
            "sample_rate": 16000,
            "channels": 2,  # Stereo not supported
            "format": "pcm16"
        }
        response = client.post("/analyze", json=payload)
        assert response.status_code == 400

    def test_invalid_sample_rate_rejected(self, client):
        audio = make_pcm16_audio()
        payload = {
            "audio_b64": encode_audio(audio),
            "sample_rate": 4000,  # Too low
            "channels": 1,
            "format": "pcm16"
        }
        response = client.post("/analyze", json=payload)
        assert response.status_code == 400

    def test_invalid_base64_returns_400(self, client):
        payload = {
            "audio_b64": "not-valid-base64!!!",
            "sample_rate": 16000,
            "channels": 1,
            "format": "pcm16"
        }
        response = client.post("/analyze", json=payload)
        assert response.status_code == 400

    def test_non_json_returns_400(self, client):
        response = client.post("/analyze", data="raw bytes", content_type="text/plain")
        assert response.status_code == 400

    def test_too_short_audio_returns_400(self, client):
        # Very short audio (< 10ms at 16kHz = 320 bytes)
        payload = {
            "audio_b64": base64.b64encode(b"\x00" * 100).decode(),
            "sample_rate": 16000,
            "channels": 1,
            "format": "pcm16"
        }
        response = client.post("/analyze", json=payload)
        assert response.status_code == 400

    def test_4_second_window_returns_good_confidence(self, client):
        """Target: ~3-4 sec first meaningful window."""
        audio = make_pcm16_audio(duration_s=4.0)
        payload = {
            "audio_b64": encode_audio(audio),
            "sample_rate": 16000,
            "channels": 1,
            "format": "pcm16"
        }
        response = client.post("/analyze", json=payload)
        assert response.status_code == 200
        data = response.get_json()
        assert data["confidence"] > 0.0  # Should have meaningful confidence


class TestSecurityConstraints:
    def test_no_audio_in_response(self, client):
        """Ensure raw audio is never returned in responses."""
        audio = make_pcm16_audio(duration_s=2.0)
        payload = {
            "audio_b64": encode_audio(audio),
            "sample_rate": 16000,
            "channels": 1,
            "format": "pcm16"
        }
        response = client.post("/analyze", json=payload)
        data = response.get_json()
        assert "audio" not in data
        assert "audio_b64" not in data
        assert "pcm" not in data
