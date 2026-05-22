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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object PartsTokens {

    val contentPaddingHorizontal: Dp = 16.dp
    val rowPaddingVertical: Dp       = 14.dp
    val rowElementSpacing: Dp        = 16.dp
    val cardBlockSpacing: Dp         = 16.dp
    val appRowSpacing: Dp            = 2.dp
    val heroToCardSpacing: Dp        = 16.dp
    val categoryTopPadding: Dp       = 20.dp
    val categoryBottomPadding: Dp    = 6.dp
    val listBottomPadding: Dp        = 32.dp
    val bannerTopSpacing: Dp         = 20.dp
    val bannerIconTopOffset: Dp      = 2.dp
    val sheetContentTopPadding: Dp   = 8.dp

    val leadingIconContainerSize: Dp = 40.dp
    val leadingIconSize: Dp          = 22.dp
    val trailingIconSize: Dp         = 20.dp
    val colourModeLeadingSize: Dp    = 36.dp
    val colourModeDotSize: Dp        = 14.dp

    val heroPreviewHeight: Dp = 200.dp

    val cardShape            = RoundedCornerShape(24.dp)
    val bannerShape          = RoundedCornerShape(20.dp)
    val iconContainerShape   = RoundedCornerShape(12.dp)
    val dialogSelectionShape = RoundedCornerShape(16.dp)
    val buttonShape          = RoundedCornerShape(50.dp)
    val bottomSheetTopShape  = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    val colourModeIconContainerAlpha: Float = 0.18f
    val colourModeIconDotAlpha: Float       = 0.85f
    val selectedStateLayerAlpha: Float      = 0.12f

    val premiumCardElevation: Dp = 2.dp

    val MotionSpringEnter: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness    = Spring.StiffnessMediumLow,
    )
    val MotionSpringExpressive: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness    = Spring.StiffnessMedium,
    )
    val MotionSpringModal: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessMediumLow,
    )
    const val motionCheckFadeInMs: Int  = 150
    const val motionCheckFadeOutMs: Int = 100

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

        val iconContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer

        val iconContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSecondaryContainer

        val bannerContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHigh

        val bannerContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

        val dialogSelectedText: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary

        val premiumCardSurface: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer

        val premiumCardContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimaryContainer

        val settingsEntryCard: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer

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
                @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant
        }
    }
}
