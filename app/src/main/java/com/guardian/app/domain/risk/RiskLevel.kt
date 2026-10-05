package com.guardian.app.domain.risk

/**
 * Standardized risk classifications across the SuSagi V1 domain.
 *
 * Internally consistent thresholds:
 * - [LOW]: 0..39 — Normal conversation, routine inquiries, benign security warnings.
 * - [CAUTION]: 40..59 — Elevated suspicion, unverified financial/informational requests.
 * - [HIGH]: 60..79 — High-probability scam pattern, credential demands, coercive urgency.
 * - [CRITICAL]: 80..100 — Severe active threat (e.g., Digital Arrest, OTP coercion, remote access).
 */
enum class RiskLevel {
    LOW,
    CAUTION,
    HIGH,
    CRITICAL;

    companion object {
        const val LOW_MAX = 39
        const val CAUTION_MAX = 59
        const val HIGH_MAX = 79
        const val CRITICAL_MIN = 80

        fun fromScore(score: Int): RiskLevel = when {
            score >= CRITICAL_MIN -> CRITICAL
            score > CAUTION_MAX -> HIGH
            score > LOW_MAX -> CAUTION
            else -> LOW
        }
    }
}
