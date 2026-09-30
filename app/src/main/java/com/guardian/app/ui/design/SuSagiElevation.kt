package com.guardian.app.ui.design

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * SuSagi Elevation Tokens
 *
 * Replaces high-intensity cyber neon glow with restrained banking-grade depth.
 */
object SuSagiElevation {
    val level0: Dp = 0.dp
    val level1: Dp = 2.dp
    val level2: Dp = 4.dp
    val level3: Dp = 8.dp
    val level4: Dp = 16.dp
}

/**
 * Subtle banking-grade card shadow.
 */
fun Modifier.suSagiShadow(
    elevation: Dp = SuSagiElevation.level1,
    shape: Shape = SuSagiShape.card,
    clip: Boolean = false
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = Color(0x33000000),
    spotColor = Color(0x33000000),
    clip = clip
)
