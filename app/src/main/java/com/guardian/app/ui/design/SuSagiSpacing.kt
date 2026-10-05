package com.guardian.app.ui.design

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * SuSagi Spacing Tokens (4dp grid)
 *
 * Guarantees consistent spacing and enforces accessible minimum tap targets.
 */
object SuSagiSpacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val section: Dp = 32.dp
    val hero: Dp = 40.dp

    // Accessibility standard touch target
    val minTouchTarget: Dp = 48.dp

    // Component standard paddings
    val cardPadding: Dp = 16.dp
    val screenPadding: Dp = 20.dp
    val buttonPaddingHorizontal: Dp = 20.dp
    val buttonHeight: Dp = 52.dp
    val topBarHeight: Dp = 56.dp
}
