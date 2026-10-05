"""
Thin production inference wrapper around the official NAVER/ClovaAI AASIST model.

The model implementation and checkpoint are not copied into SuSagi. The Docker
image pins the official upstream repository to a specific commit and verifies
the checkpoint Git blob hash before startup.
"""
from __future__ import annotations

import hashlib
import importlib
import os
import sys
import time
from pathlib import Path
from typing import Dict

import librosa
import numpy as np
import torch

AASIST_SOURCE_REPOSITORY = "https://github.com/clovaai/aasist"
AASIST_SOURCE_COMMIT = "a04c9863f63d44471dde8a6abcb3b082b07cd1d1"
AASIST_CHECKPOINT_FILENAME = "AASIST.pth"
AASIST_CHECKPOINT_GIT_BLOB_SHA1 = "edbce171dbae13ae448f210e9fd62a0b861b5713"
AASIST_TRAINING_DATASET = "ASVspoof 2019 Logical Access"
AASIST_LICENSE = "MIT"
TARGET_SAMPLE_RATE = 16000
MODEL_SAMPLES = 64600

MODEL_CONFIG: Dict[str, object] = {
    "architecture": "AASIST",
    "nb_samp": MODEL_SAMPLES,
    "first_conv": 128,
    "filts": [70, [1, 32], [32, 32], [32, 64], [64, 64]],
    "gat_dims": [64, 32],
    "pool_ratios": [0.5, 0.7, 0.5, 0.5],
    "temperatures": [2.0, 2.0, 100.0, 100.0],
}

def _repeat_or_crop(audio: np.ndarray, max_len: int = MODEL_SAMPLES) -> np.ndarray:
    """Match the deterministic upstream evaluation preprocessing."""
    if audio.size == 0:
        raise ValueError("audio must contain at least one sample")
    if audio.shape[0] >= max_len:
        return audio[:max_len]
    repeats = int(max_len / audio.shape[0]) + 1
    return np.tile(audio, repeats)[:max_len]

class AASISTDetector:
    def __init__(self, repo_dir: str | None = None, checkpoint_path: str | None = None, device: str | None = None) -> None:
        self.repo_dir = Path(repo_dir or os.environ.get("AASIST_REPO_DIR", "/opt/aasist")).resolve()
        self.checkpoint_path = Path(
            checkpoint_path or os.environ.get(
                "AASIST_CHECKPOINT_PATH",
                str(self.repo_dir / "models" / "weights" / AASIST_CHECKPOINT_FILENAME),
            )
        ).resolve()

        if not (self.repo_dir / "models" / "AASIST.py").is_file():
            raise RuntimeError(f"Official AASIST source not found under {self.repo_dir}")
        if not self.checkpoint_path.is_file():
            raise RuntimeError(f"AASIST checkpoint not found: {self.checkpoint_path}")

        requested_device = device or os.environ.get("AASIST_DEVICE", "auto")
        if requested_device == "auto":
            requested_device = "cuda" if torch.cuda.is_available() else "cpu"
        self.device = torch.device(requested_device)

        repo_str = str(self.repo_dir)
        if repo_str not in sys.path:
            sys.path.insert(0, repo_str)

        module = importlib.import_module("models.AASIST")
        model_cls = getattr(module, "Model")
        self.model = model_cls(MODEL_CONFIG).to(self.device)

        state = torch.load(self.checkpoint_path, map_location=self.device)
        self.model.load_state_dict(state, strict=True)
        self.model.eval()

        self.checkpoint_sha256 = self._sha256(self.checkpoint_path)
        self.model_version = f"clovaai-aasist-{AASIST_SOURCE_COMMIT[:8]}"
        self.loaded = True

    @staticmethod
    def _sha256(path: Path) -> str:
        digest = hashlib.sha256()
        with path.open("rb") as handle:
            for chunk in iter(lambda: handle.read(1024 * 1024), b""):
                digest.update(chunk)
        return digest.hexdigest()

    def analyze(self, audio: np.ndarray, sample_rate: int) -> dict:
        inference_started_at_ms = int(time.time() * 1000)
        start_perf = time.perf_counter()

        x = np.asarray(audio, dtype=np.float32).reshape(-1)
        if x.size == 0:
            raise ValueError("audio is empty")
        if not np.all(np.isfinite(x)):
            raise ValueError("audio contains non-finite samples")

        if sample_rate != TARGET_SAMPLE_RATE:
            x = librosa.resample(
                x, orig_sr=sample_rate, target_sr=TARGET_SAMPLE_RATE, res_type="soxr_hq"
            ).astype(np.float32, copy=False)

        x = np.clip(x, -1.0, 1.0)
        x = _repeat_or_crop(x)

        tensor = torch.from_numpy(x).float().unsqueeze(0).to(self.device)
        with torch.inference_mode():
            _, logits = self.model(tensor)
            posterior = torch.softmax(logits, dim=1)[0]
            bona_fide_probability = float(posterior[1].detach().cpu().item())

        synthetic_probability = float(np.clip(1.0 - bona_fide_probability, 0.0, 1.0))
        confidence = float(np.clip(abs(synthetic_probability - 0.5) * 2.0, 0.0, 1.0))

        inference_finished_at_ms = int(time.time() * 1000)
        inference_ms = int((time.perf_counter() - start_perf) * 1000)

        return {
            "synthetic_probability": synthetic_probability,
            "confidence": confidence,
            "model_version": self.model_version,
            "model_source_commit": AASIST_SOURCE_COMMIT,
            "checkpoint_filename": self.checkpoint_path.name,
            "checkpoint_sha256": self.checkpoint_sha256,
            "inference_started_at_ms": inference_started_at_ms,
            "inference_finished_at_ms": inference_finished_at_ms,
            "inference_ms": inference_ms,
        }
