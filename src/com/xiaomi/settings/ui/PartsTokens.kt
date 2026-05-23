/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.ui

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    // Bottom-sheet: round top corners only, square bottom corners.
    // Uses .copy() on extraLarge so the radius stays token-driven
    // and responds to any dynamic-colour / M3E theme override.
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
    const val frictionMultiplier: Float = 2f
    const val toastDebounceMs:    Long  = 2_000L

    // Motion duration constants
    const val motionNavFadeEnterMs: Int = 220
    const val motionNavFadeExitMs:  Int = 200
    const val motionNavSlideMs:     Int = 220
    const val motionShimmerTweenMs: Int = 2800
    const val motionBannerFadeMs:   Int = 220
    const val motionCheckFadeInMs:  Int = 150
    const val motionCheckFadeOutMs: Int = 100

    // Motion specs — M3 MotionScheme
    // Kotlin does not allow type parameters on extension properties without
    // a receiver; these are functions so the type can be inferred at each call-site.
    @Composable @ReadOnlyComposable
    fun <T> motionDefaultSpatial(): FiniteAnimationSpec<T> =
        MaterialTheme.motionScheme.defaultSpatialSpec()

    @Composable @ReadOnlyComposable
    fun <T> motionFastSpatial(): FiniteAnimationSpec<T> =
        MaterialTheme.motionScheme.fastSpatialSpec()

    @Composable @ReadOnlyComposable
    fun <T> motionSlowSpatial(): FiniteAnimationSpec<T> =
        MaterialTheme.motionScheme.slowSpatialSpec()

    @Composable @ReadOnlyComposable
    fun <T> motionDefaultEffects(): FiniteAnimationSpec<T> =
        MaterialTheme.motionScheme.defaultEffectsSpec()

    @Composable @ReadOnlyComposable
    fun <T> motionFastEffects(): FiniteAnimationSpec<T> =
        MaterialTheme.motionScheme.fastEffectsSpec()

    @Composable @ReadOnlyComposable
    fun <T> motionSlowEffects(): FiniteAnimationSpec<T> =
        MaterialTheme.motionScheme.slowEffectsSpec()

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
