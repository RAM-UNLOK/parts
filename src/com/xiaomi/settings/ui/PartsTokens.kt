/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.ui

import androidx.compose.animation.core.FiniteAnimationSpec
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
        @Composable @ReadOnlyComposable get() = MaterialTheme.shapes.largeIncreased
    val buttonShape: Shape
        @Composable @ReadOnlyComposable get() = MaterialTheme.shapes.full
    val bottomSheetTopShape: Shape
        @Composable @ReadOnlyComposable get() = MaterialTheme.shapes.extraLargeTop

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
    val <T> MotionDefaultSpatial: FiniteAnimationSpec<T>
        @Composable @ReadOnlyComposable get() = MaterialTheme.motionScheme.defaultSpatialSpec()
    val <T> MotionFastSpatial: FiniteAnimationSpec<T>
        @Composable @ReadOnlyComposable get() = MaterialTheme.motionScheme.fastSpatialSpec()
    val <T> MotionSlowSpatial: FiniteAnimationSpec<T>
        @Composable @ReadOnlyComposable get() = MaterialTheme.motionScheme.slowSpatialSpec()
    val <T> MotionDefaultEffects: FiniteAnimationSpec<T>
        @Composable @ReadOnlyComposable get() = MaterialTheme.motionScheme.defaultEffectsSpec()
    val <T> MotionFastEffects: FiniteAnimationSpec<T>
        @Composable @ReadOnlyComposable get() = MaterialTheme.motionScheme.fastEffectsSpec()
    val <T> MotionSlowEffects: FiniteAnimationSpec<T>
        @Composable @ReadOnlyComposable get() = MaterialTheme.motionScheme.slowEffectsSpec()

    object Colors {
        // Page / scaffold
        val page: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
        val topBarResting: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
        val topBarScrolled: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainer

        // Card surface
        val cardSurface: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainer

        // Text
        val textPrimary: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
        val textSecondary: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

        // Divider
        val outlineVariant: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant

        // Generic row icon containers
        val iconContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer
        val iconContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSecondaryContainer

        // Banners
        val chargingBannerContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiaryContainer
        val chargingBannerContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onTertiaryContainer
        val infoBannerContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer
        val infoBannerContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSecondaryContainer

        // Selection state
        val dialogSelectedText: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
        val selectionLayer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary

        // Per-app premium card
        val premiumCardSurface: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
        val premiumCardContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimaryContainer
        val premiumCardButton: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
        val premiumCardButtonContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimary

        // Settings homepage entry card
        val settingsEntryCard: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer

        // Module-specific icon containers — Display
        val displayIconContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiaryContainer
        val displayIconContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onTertiaryContainer

        // Module-specific icon containers — Thermal
        val thermalIconContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.errorContainer
        val thermalIconContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onErrorContainer

        // Module-specific icon containers — Touch
        val touchIconContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
        val touchIconContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimaryContainer
        val touchIconContainerOff: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHigh
        val touchIconContentOff: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

        // Module-specific icon containers — Diagnostics (Fingerprint + Speaker)
        val diagIconContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer
        val diagIconContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSecondaryContainer

        // Hero preview background
        val heroBg: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHigh

        // App icon placeholder
        val appIconFallback: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHighest

        // Category label
        val categoryLabelColor: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary

        // Colour mode hues (Display Colours screen blob palette)
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
