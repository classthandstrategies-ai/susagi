# SuSagi Voice Authenticity Service

Private CP4A anti-spoof inference service. Android never receives the model-service credential.

## Model provenance

- Model: AASIST
- Source: NAVER/ClovaAI `clovaai/aasist`
- Pinned source commit: `a04c9863f63d44471dde8a6abcb3b082b07cd1d1`
- Checkpoint: `models/weights/AASIST.pth`
- Upstream Git blob SHA-1: `edbce171dbae13ae448f210e9fd62a0b861b5713`
- Training/evaluation family: ASVspoof 2019 Logical Access
- License: MIT
- Input: mono waveform, 16 kHz
- Window: 64,600 samples (~4.04 s)

The image verifies the pinned source commit and checkpoint Git blob hash. Runtime health reports checkpoint SHA-256.

## Preprocessing

PCM16 mono 16 kHz -> float [-1,1], no peak normalization. Short input is repeated to 64,600 samples using the upstream evaluation behavior; long input is cropped. Inference uses official AASIST in eval/inference mode.

The upstream model has spoof/bona-fide logits. SuSagi applies softmax and interprets class 1 as bona fide:

`synthetic_probability = 1 - bona_fide_softmax_score`

Reported `confidence` is margin from the 0.5 boundary, not a calibrated real-world probability.

## Security

Public path:

Android -> Supabase JWT -> SuSagi backend -> private service

Private `POST /analyze` requires `Authorization: Bearer <VOICE_AUTH_SERVICE_TOKEN>`.
No raw audio is logged or persisted. The service fails closed if the token or trained model is unavailable.

## Benchmark

`benchmark.py` consumes a JSON manifest referencing local WAV files. Do not commit audio samples.

Manifest metadata: `path`, `label` (human/synthetic), `speaker_id`, `language`, optional `generator`, `transport` (clean/agora/speaker_replay), and optional `pair_id`.

It reports TP/TN/FP/FN/UNCERTAIN, precision, recall, FPR, FNR, coverage, measured p50/p95 model inference latency, and paired clean-vs-transport probability shift. A small corpus must not be described as general real-world accuracy.
