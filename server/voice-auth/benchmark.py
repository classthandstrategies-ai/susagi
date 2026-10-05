"""Reproducible CP4A real-audio benchmark runner. Audio is read locally and never written."""
from __future__ import annotations

import argparse
import json
from pathlib import Path

import librosa
import numpy as np
import soundfile as sf

from aasist_detector import AASISTDetector

def classify(probability: float, confidence: float) -> str:
    if confidence < 0.30:
        return "UNCERTAIN"
    if probability >= 0.70:
        return "SYNTHETIC_LIKELY"
    if probability <= 0.30:
        return "LIKELY_HUMAN"
    return "UNCERTAIN"

def safe_rate(num: int, den: int):
    return None if den == 0 else num / den

def percentile(values, q):
    if not values:
        return None
    ordered = sorted(values)
    index = round((len(ordered) - 1) * q)
    return ordered[index]

def load_audio(path: Path):
    audio, sample_rate = sf.read(path, dtype="float32", always_2d=False)
    if audio.ndim == 2:
        audio = np.mean(audio, axis=1)
    if sample_rate != 16000:
        audio = librosa.resample(
            audio, orig_sr=sample_rate, target_sr=16000, res_type="soxr_hq"
        ).astype(np.float32, copy=False)
        sample_rate = 16000
    return audio, sample_rate

def run(manifest_path: Path):
    manifest = json.loads(manifest_path.read_text())
    if not isinstance(manifest, list) or not manifest:
        raise ValueError("Manifest must be a non-empty JSON array")
    detector = AASISTDetector()
    rows = []
    tp = tn = fp = fn = uncertain = 0
    latencies = []

    for item in manifest:
        path = (manifest_path.parent / item["path"]).resolve()
        truth = item["label"]
        if truth not in {"human", "synthetic"}:
            raise ValueError(f"Unsupported label {truth!r} for {path}")
        audio, sample_rate = load_audio(path)
        result = detector.analyze(audio, sample_rate)
        prediction = classify(result["synthetic_probability"], result["confidence"])
        latencies.append(result["inference_ms"])

        if prediction == "UNCERTAIN":
            uncertain += 1
        elif truth == "synthetic" and prediction == "SYNTHETIC_LIKELY":
            tp += 1
        elif truth == "human" and prediction == "LIKELY_HUMAN":
            tn += 1
        elif truth == "human" and prediction == "SYNTHETIC_LIKELY":
            fp += 1
        elif truth == "synthetic" and prediction == "LIKELY_HUMAN":
            fn += 1

        rows.append({
            "path": item["path"],
            "label": truth,
            "speaker_id": item.get("speaker_id"),
            "language": item.get("language"),
            "generator": item.get("generator"),
            "transport": item.get("transport", "clean"),
            "pair_id": item.get("pair_id"),
            "prediction": prediction,
            "synthetic_probability": result["synthetic_probability"],
            "confidence": result["confidence"],
            "inference_ms": result["inference_ms"],
        })

    decided = tp + tn + fp + fn
    total = len(rows)
    summary = {
        "sample_count": total, "TP": tp, "TN": tn, "FP": fp, "FN": fn, "UNCERTAIN": uncertain,
        "precision": safe_rate(tp, tp + fp),
        "recall": safe_rate(tp, tp + fn),
        "false_positive_rate": safe_rate(fp, fp + tn),
        "false_negative_rate": safe_rate(fn, fn + tp),
        "coverage": safe_rate(decided, total),
        "p50_inference_ms": percentile(latencies, 0.50),
        "p95_inference_ms": percentile(latencies, 0.95),
        "checkpoint_sha256": detector.checkpoint_sha256,
        "model_version": detector.model_version,
        "metric_note": "Binary metrics exclude UNCERTAIN; coverage reports decided/total.",
    }

    paired = {}
    for row in rows:
        if row["pair_id"]:
            paired.setdefault(row["pair_id"], {})[row["transport"]] = row
    transport_pairs = []
    for pair_id, variants in sorted(paired.items()):
        if "clean" not in variants:
            continue
        clean = variants["clean"]
        for transport, variant in variants.items():
            if transport == "clean":
                continue
            transport_pairs.append({
                "pair_id": pair_id,
                "transport": transport,
                "clean_prediction": clean["prediction"],
                "transport_prediction": variant["prediction"],
                "clean_synthetic_probability": clean["synthetic_probability"],
                "transport_synthetic_probability": variant["synthetic_probability"],
                "absolute_probability_shift": abs(
                    clean["synthetic_probability"] - variant["synthetic_probability"]
                ),
            })
    return {"summary": summary, "transport_pairs": transport_pairs, "samples": rows}

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    report = run(args.manifest)
    rendered = json.dumps(report, indent=2)
    if args.output:
        args.output.write_text(rendered + "\n")
    print(rendered)

if __name__ == "__main__":
    main()
