"""Focused HTTP/security tests for the private voice-auth service.

Synthetic bytes here test transport validation only. They are not evidence of
human-vs-synthetic model accuracy.
"""
import base64

import numpy as np
import pytest

from server import create_app

class FakeDetector:
    loaded = True
    model_version = "fake-aasist-test"
    checkpoint_sha256 = "00" * 32
    device = "cpu"

    def analyze(self, audio, sample_rate):
        assert sample_rate == 16000
        assert isinstance(audio, np.ndarray)
        return {
            "synthetic_probability": 0.8,
            "confidence": 0.6,
            "model_version": self.model_version,
            "model_source_commit": "test",
            "checkpoint_filename": "AASIST.pth",
            "checkpoint_sha256": self.checkpoint_sha256,
            "inference_started_at_ms": 1000,
            "inference_finished_at_ms": 1010,
            "inference_ms": 10,
        }

@pytest.fixture
def client():
    app = create_app(detector=FakeDetector(), service_token="server-secret")
    app.config["TESTING"] = True
    with app.test_client() as test_client:
        yield test_client

def audio_b64(samples=16000):
    pcm = np.zeros(samples, dtype=np.int16).tobytes()
    return base64.b64encode(pcm).decode("ascii")

def auth_headers():
    return {"Authorization": "Bearer server-secret"}

def valid_payload():
    return {
        "audio_b64": audio_b64(),
        "sample_rate": 16000,
        "channels": 1,
        "format": "pcm16",
    }

def test_requires_internal_service_token(client):
    assert client.post("/analyze", json=valid_payload()).status_code == 401

def test_rejects_wrong_internal_service_token(client):
    response = client.post(
        "/analyze",
        headers={"Authorization": "Bearer wrong"},
        json=valid_payload(),
    )
    assert response.status_code == 401

def test_valid_request_returns_model_result_without_audio(client):
    response = client.post("/analyze", headers=auth_headers(), json=valid_payload())
    assert response.status_code == 200
    data = response.get_json()
    assert data["synthetic_probability"] == pytest.approx(0.8)
    assert data["model_version"] == "fake-aasist-test"
    assert "audio_b64" not in data
    assert "pcm" not in data

@pytest.mark.parametrize(
    "field,value",
    [("sample_rate", 48000), ("channels", 2), ("format", "wav")],
)
def test_rejects_wrong_audio_contract(client, field, value):
    payload = valid_payload()
    payload[field] = value
    response = client.post("/analyze", headers=auth_headers(), json=payload)
    assert response.status_code == 400

def test_rejects_invalid_base64(client):
    payload = valid_payload()
    payload["audio_b64"] = "not-valid-base64!!"
    response = client.post("/analyze", headers=auth_headers(), json=payload)
    assert response.status_code == 400

def test_rejects_oversized_audio(client):
    payload = valid_payload()
    payload["audio_b64"] = audio_b64(samples=16000 * 7)
    response = client.post("/analyze", headers=auth_headers(), json=payload)
    assert response.status_code in (413, 400)

def test_health_exposes_model_provenance_not_audio(client):
    response = client.get("/health")
    assert response.status_code == 200
    data = response.get_json()
    assert data["model_loaded"] is True
    assert data["checkpoint_filename"] == "AASIST.pth"
    assert "audio" not in data
