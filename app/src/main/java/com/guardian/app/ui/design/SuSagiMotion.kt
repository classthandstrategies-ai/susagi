package com.guardian.app.ui.design

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween

/**
 * SuSagi Motion Tokens
 *
 * Guiding Principle:
 * Calm, predictable, reassuring transitions.
 * Shaking UI and aggressive continuous pulsing danger animations are strictly prohibited
 * to protect stressed users under active scam duress.
 */
object SuSagiMotion {
    const val DurationShort = 150
    const val DurationNormal = 250
    const val DurationEmphasized = 350

    val EasingStandard: Easing = FastOutSlowInEasing
    val EasingDecelerate: Easing = LinearOutSlowInEasing

    fun <T> standardTween(durationMillis: Int = DurationNormal) =
        tween<T>(durationMillis = durationMillis, easing = EasingStandard)

    fun <T> decelerateTween(durationMillis: Int = DurationNormal) =
        tween<T>(durationMillis = durationMillis, easing = EasingDecelerate)
}
