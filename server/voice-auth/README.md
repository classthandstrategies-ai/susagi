# SuSagi Voice Authenticity Inference Server

Private inference endpoint for voice authenticity analysis.

## Architecture

```
Android App → sends short REMOTE audio windows (base64 PCM16)
           → this server (AASIST-inspired spectral feature classifier)
           → returns authenticity assessment JSON
```

## Quickstart

```bash
# Install dependencies
pip install -r requirements.txt

# Run locally (development)
python server.py

# Run with Docker
docker build -t voice-auth .
docker run -p 8090:8090 -e VOICE_AUTH_API_KEY=your-key voice-auth
```

## API

### POST /analyze

**Request:**
```json
{
  "audio_b64": "<base64-encoded PCM16 little-endian bytes>",
  "sample_rate": 16000,
  "channels": 1,
  "format": "pcm16"
}
```

**Response:**
```json
{
  "synthetic_probability": 0.15,
  "confidence": 0.72,
  "model_version": "aasist-lite-v0.1-prototype",
  "inference_ms": 42
}
```

### GET /health

Returns server and model status.

## Security

- API key authentication via `Authorization: Bearer <key>`
- Set `VOICE_AUTH_API_KEY` environment variable
- Request size limits enforced
- No raw audio logged or persisted
- Rejects malformed/non-PCM payloads

## Model Status

**Current**: AASIST-lite prototype using spectral features (spectral centroid, flatness, ZCR, rolloff, energy variance, pitch stability). This is NOT a calibrated production model.

**Next**: Full AASIST/AASIST-L neural model with pre-trained weights → ONNX/TFLite on-device.

## Configuration

| Environment Variable | Default | Description |
|---|---|---|
| `PORT` | 8090 | Server port |
| `VOICE_AUTH_API_KEY` | (dev key) | API key for authentication |
