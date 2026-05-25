/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object PartsTokens {

    // Spacing scale — 4 px base unit
    val space1:  Dp = 1.dp
    val space4:  Dp = 4.dp
    val space8:  Dp = 8.dp
    val space12: Dp = 12.dp
    val space16: Dp = 16.dp
    val space20: Dp = 20.dp
    val space24: Dp = 24.dp
    val space28: Dp = 28.dp
    val space32: Dp = 32.dp
    val space40: Dp = 40.dp
    val space48: Dp = 48.dp
    val space56: Dp = 56.dp

    // Layout tokens
    val contentPaddingHorizontal: Dp get() = space16
    val rowPaddingVertical:       Dp get() = space16
    val rowElementSpacing:        Dp get() = space16
    val cardBlockSpacing:         Dp get() = space16
    val heroToCardSpacing:        Dp get() = space16
    val premiumCardSpacing:       Dp get() = space12
    val categoryTopPadding:       Dp get() = space24
    val categoryBottomPadding:    Dp get() = space8
    val listBottomPadding:        Dp get() = space32
    val bannerTopSpacing:         Dp get() = space20
    val bannerVerticalPadding:    Dp get() = space16
    val sheetContentTopPadding:   Dp get() = space8

    // Component size tokens
    val leadingIconContainerSize: Dp = space40
    val leadingIconSize:          Dp = space24
    val trailingIconSize:         Dp = space24
    val closeIconSize:            Dp = space20
    val appIconSize:              Dp = space40
    val colourModeDotSize:        Dp = space16
    val heroPreviewHeight:        Dp = 200.dp
    val premiumCardElevation:     Dp = space4
    val dividerThickness:         Dp = space1

    // Shape tokens
    val cardShape: Shape
        @Composable @ReadOnlyComposable get() = MaterialTheme.shapes.extraLarge
    val bannerShape: Shape
        @Composable @ReadOnlyComposable get() = MaterialTheme.shapes.extraLarge
    val leadingIconShape: Shape
        @Composable @ReadOnlyComposable get() = MaterialTheme.shapes.large
    val dialogSelectionShape: Shape
        @Composable @ReadOnlyComposable get() = MaterialTheme.shapes.medium
    val buttonShape: Shape
        @Composable @ReadOnlyComposable get() = MaterialTheme.shapes.extraLarge
    val bottomSheetTopShape: Shape
        @Composable @ReadOnlyComposable get() = MaterialTheme.shapes.extraLarge.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd   = CornerSize(0.dp),
        )

    // Alpha constants
    const val colourModeIconContainerAlpha: Float = 0.18f
    const val colourModeIconDotAlpha:       Float = 0.85f
    const val selectedStateLayerAlpha:      Float = 0.12f

    // Shimmer / blob constants
    const val shimmerAlpha:       Float = 0.05f
    const val blobRadiusFraction: Float = 0.38f
    const val toastDebounceMs:    Long  = 2_000L

    // ---------------------------------------------------------------------------
    // Motion — Stable M3 tokens only
    //
    // Duration reference (ms):
    //   Short1=50  Short2=100  Short3=150  Short4=200
    //   Medium1=250 Medium2=300 Medium3=350 Medium4=400
    //
    // Easing:
    //   Spatial enter  → EmphasizedDecelerate (0.05, 0.7, 0.1, 1.0)
    //   Spatial exit   → EmphasizedAccelerate (0.3, 0.0, 0.8, 0.15)
    //   Effects enter  → LinearOutSlowIn
    //   Effects exit   → FastOutLinearIn
    //   Standard       → FastOutSlowIn
    //
    // No @ExperimentalMaterial3ExpressiveApi. No MotionScheme.
    // ---------------------------------------------------------------------------

    private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    const val motionShimmerTweenMs: Int = 2800

    fun <T> navSpatialSpec(): FiniteAnimationSpec<T> =
        tween(durationMillis = 300, easing = EmphasizedDecelerate)

    fun <T> navEffectsSpec(): FiniteAnimationSpec<T> =
        tween(durationMillis = 150, easing = FastOutSlowInEasing)

    fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> =
        spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)

    fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> =
        tween(durationMillis = 150, easing = FastOutSlowInEasing)

    fun <T> checkFadeInSpec(): FiniteAnimationSpec<T> =
        tween(durationMillis = 150, easing = LinearOutSlowInEasing)

    fun <T> checkFadeOutSpec(): FiniteAnimationSpec<T> =
        tween(durationMillis = 100, easing = FastOutLinearInEasing)

    fun <T> bannerEnterSpec(): FiniteAnimationSpec<T> =
        tween(durationMillis = 250, easing = EmphasizedDecelerate)

    fun <T> shimmerSpec(): FiniteAnimationSpec<T> =
        tween(durationMillis = motionShimmerTweenMs, easing = LinearEasing)

    // ---------------------------------------------------------------------------
    // Color roles — resolved through MaterialTheme.colorScheme (stable M3)
    // ---------------------------------------------------------------------------
    object Colors {
        val page: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
        val topBarResting: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
        val topBarScrolled: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainer
        val cardSurface: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainer
        val textPrimary: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
        val textSecondary: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
        val outlineVariant: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant
        val iconContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer
        val iconContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSecondaryContainer
        val chargingBannerContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiaryContainer
        val chargingBannerContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onTertiaryContainer
        val infoBannerContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer
        val infoBannerContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSecondaryContainer
        val dialogSelectedText: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
        val selectionLayer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
        val premiumCardSurface: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
        val premiumCardContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimaryContainer
        val premiumCardButton: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
        val premiumCardButtonContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimary
        val settingsEntryCard: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer
        val displayIconContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiaryContainer
        val displayIconContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onTertiaryContainer
        val thermalIconContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.errorContainer
        val thermalIconContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onErrorContainer
        val touchIconContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
        val touchIconContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimaryContainer
        val touchIconContainerOff: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHigh
        val touchIconContentOff: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
        val diagIconContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer
        val diagIconContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSecondaryContainer
        val heroBg: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHigh
        val appIconFallback: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHighest
        val categoryLabelColor: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary

        object Hues {
            val vivid: Color
                @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.error
            val saturated: Color
                @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiary
            val standard: Color
                @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
            val original: Color
                @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
            val p3: Color
                @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondary
            val srgb: Color
                @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    // ---------------------------------------------------------------------------
    // Typography — resolved through MaterialTheme.typography (stable M3)
    // No hardcoded sp values or FontWeight anywhere.
    // ---------------------------------------------------------------------------
    object Type {
        val screenTitle: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.headlineMedium
        val categoryLabel: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.labelLarge
        val rowHeadline: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.titleMedium
        val rowSupporting: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.bodyMedium
        val rowCaption: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.bodySmall
        val sheetTitle: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.headlineSmall
        val cardTitle: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.titleLarge
        val buttonLabel: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.labelLarge
        val bannerLabel: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.titleSmall
        val infoBody: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.bodyMedium
    }
}
