/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object PartsTokens {

    val space4: Dp = 4.dp
    val space8: Dp = 8.dp
    val space12: Dp = 12.dp
    val space16: Dp = 16.dp
    val space20: Dp = 20.dp
    val space24: Dp = 24.dp
    val space28: Dp = 28.dp
    val space32: Dp = 32.dp
    val space40: Dp = 40.dp
    val space48: Dp = 48.dp
    val space56: Dp = 56.dp

    val contentPaddingHorizontal: Dp get() = space16
    val rowPaddingVertical: Dp get() = space16
    val rowElementSpacing: Dp get() = space16
    val cardBlockSpacing: Dp get() = space16
    val heroToCardSpacing: Dp get() = space16
    val premiumCardSpacing: Dp get() = space12
    val categoryTopPadding: Dp get() = space24
    val categoryBottomPadding: Dp get() = space8
    val listBottomPadding: Dp get() = space32
    val bannerTopSpacing: Dp get() = space20
    val bannerVerticalPadding: Dp get() = space16
    val sheetContentTopPadding: Dp get() = space8

    val leadingIconContainerSize: Dp = space40
    val leadingIconSize: Dp = space24
    val trailingIconSize: Dp = space24
    val closeIconSize: Dp = space20
    val appIconSize: Dp = space40
    val colourModeLeadingSize: Dp = space40
    val colourModeDotSize: Dp = space16
    val heroPreviewHeight: Dp = space56 * 3 + space32
    val premiumCardElevation: Dp = space4
    val dividerThickness: Dp = space4 / 4
    val dismissButtonSize: Dp = space32

    val cardShape = RoundedCornerShape(space28)
    val bannerShape = RoundedCornerShape(space24)
    val leadingIconShape = RoundedCornerShape(space16)
    val dialogSelectionShape = RoundedCornerShape(space20)
    val buttonShape = RoundedCornerShape(percent = 50)
    val bottomSheetTopShape = RoundedCornerShape(topStart = space28, topEnd = space28)

    const val colourModeIconContainerAlpha: Float = 0.18f
    const val colourModeIconDotAlpha: Float = 0.85f
    const val selectedStateLayerAlpha: Float = 0.12f
    const val selectedDotContainerAlpha: Float = 0.32f

    const val shimmerStartPx: Float = -600f
    const val shimmerEndPx: Float = 1200f
    const val shimmerWidthPx: Float = 300f
    const val shimmerAlpha: Float = 0.05f
    const val blobRadiusFraction: Float = 0.38f
    const val frictionMultiplier: Float = 2f
    const val toastDebounceMs: Long = 2_000L

    const val motionNavFadeEnterMs: Int = 220
    const val motionNavFadeExitMs: Int = 200
    const val motionNavSlideMs: Int = 220
    const val motionShimmerTweenMs: Int = 2800
    const val motionBannerFadeMs: Int = 220
    const val motionCheckFadeInMs: Int = 150
    const val motionCheckFadeOutMs: Int = 100

    val MotionSpringEnter: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
    val MotionSpringNoBouncy: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    val MotionSpringExpressive: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    val MotionSpringModal: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
    val MotionSpringColor: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    object Colors {
        val page: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
        val topBarResting: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
        val topBarScrolled: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainer
        val textPrimary: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
        val textSecondary: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
        val outlineVariant: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant
        val transparent: Color
            get() = Color.Transparent
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
        val dialogSelectedLayer: Color
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
