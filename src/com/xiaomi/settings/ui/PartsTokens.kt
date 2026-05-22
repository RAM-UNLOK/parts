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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle

object PartsTokens {

    // ── Spacing scale (4 dp base unit) ───────────────────────────────────
    val sp4:  Dp = 4.dp
    val sp8:  Dp = 8.dp
    val sp12: Dp = 12.dp
    val sp16: Dp = 16.dp
    val sp20: Dp = 20.dp
    val sp24: Dp = 24.dp
    val sp28: Dp = 28.dp
    val sp32: Dp = 32.dp

    // ── Semantic spacing aliases ──────────────────────────────────────────
    val contentPaddingHorizontal: Dp get() = sp16
    val rowPaddingVertical:       Dp get() = sp12 + sp2   // 14 dp
    val rowElementSpacing:        Dp get() = sp16
    val cardBlockSpacing:         Dp get() = sp16
    val heroToCardSpacing:        Dp get() = sp16
    val premiumCardSpacing:       Dp get() = sp12
    val categoryTopPadding:       Dp get() = sp20
    val categoryBottomPadding:    Dp get() = sp8 - sp2    // 6 dp
    val listBottomPadding:        Dp get() = sp32
    val bannerTopSpacing:         Dp get() = sp20
    val bannerVerticalPadding:    Dp get() = sp12 + sp2   // 14 dp
    val sheetContentTopPadding:   Dp get() = sp8

    // sub-step helpers (not exported as public API)
    private val sp2: Dp = 2.dp

    // ── Icon / component sizes ────────────────────────────────────────────
    val leadingIconContainerSize: Dp = sp20 * 2      // 40 dp
    val leadingIconSize:          Dp = 22.dp
    val trailingIconSize:         Dp = sp20
    val closeIconSize:            Dp = 18.dp
    val appIconSize:              Dp = sp20 * 2      // 40 dp
    val colourModeLeadingSize:    Dp = sp20 - sp2 + sp20  // 36 dp  (20+20-4 = 36)
    val colourModeDotSize:        Dp = 14.dp
    val heroPreviewHeight:        Dp = 200.dp
    val premiumCardElevation:     Dp = sp4 - sp2     // 2 dp
    val dividerThickness:         Dp = sp2 - sp2 + 1.dp // 1 dp

    // ── Shape tokens (M3E bold radii) ─────────────────────────────────────
    val cardShape            = RoundedCornerShape(sp24)
    val bannerShape          = RoundedCornerShape(sp20)
    val leadingIconShape     = RoundedCornerShape(sp12)
    val dialogSelectionShape = RoundedCornerShape(sp16)
    val buttonShape          = RoundedCornerShape(50.dp)   // pill
    val bottomSheetTopShape  = RoundedCornerShape(topStart = sp28, topEnd = sp28)

    // ── Alpha tokens ─────────────────────────────────────────────────────
    const val colourModeIconContainerAlpha: Float = 0.18f
    const val colourModeIconDotAlpha:       Float = 0.85f
    const val selectedStateLayerAlpha:      Float = 0.12f
    const val selectedDotContainerAlpha:    Float = 0.32f

    // ── Canvas / shimmer constants (tokenised from inline file consts) ────
    const val shimmerStartPx:       Float = -600f
    const val shimmerEndPx:         Float = 1200f
    const val shimmerWidthPx:       Float = 300f
    const val shimmerAlpha:         Float = 0.05f
    const val blobRadiusFraction:   Float = 0.38f
    const val motionShimmerTweenMs: Int   = 2800
    const val motionBannerFadeMs:   Int   = 220
    const val motionCheckFadeInMs:  Int   = 150
    const val motionCheckFadeOutMs: Int   = 100
    const val frictionMultiplier:   Float = 2f
    const val toastDebounceMs:      Long  = 2_000L

    // ── Motion / spring tokens ────────────────────────────────────────────
    val MotionSpringEnter: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness    = Spring.StiffnessMediumLow,
    )
    val MotionSpringNoBouncy: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness    = Spring.StiffnessMedium,
    )
    val MotionSpringExpressive: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness    = Spring.StiffnessMedium,
    )
    val MotionSpringModal: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessMediumLow,
    )
    val MotionSpringColor: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness    = Spring.StiffnessMediumLow,
    )

    // ── Color tokens — 100 % MaterialTheme.colorScheme, zero hex ─────────
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

        // Default row icon container (diagnostics / fallback rows)
        val iconContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer

        val iconContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSecondaryContainer

        // Charging banner (tertiaryContainer = warm/amber in Monet)
        val chargingBannerContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiaryContainer

        val chargingBannerContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onTertiaryContainer

        // Info / descriptive banners (secondaryContainer)
        val infoBannerContainer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer

        val infoBannerContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSecondaryContainer

        val dialogSelectedText: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary

        val dialogSelectedLayer: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary

        // Per-app thermal premium card
        val premiumCardSurface: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer

        val premiumCardContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimaryContainer

        val premiumCardButton: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary

        val premiumCardButtonContent: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimary

        // Android Settings homepage entry card surface
        val settingsEntryCard: Color
            @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer

        // Feature-specific icon containers
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

        // Colour-mode hues — each maps to a distinct tonal role
        object Hues {
            // Vivid: highest chroma role
            val vivid: Color
                @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.error

            // Saturated: tertiary (complementary hue)
            val saturated: Color
                @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiary

            // Standard: primary brand hue
            val standard: Color
                @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary

            // Original: neutral-content hue
            val original: Color
                @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface

            // P3: secondary hue
            val p3: Color
                @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondary

            // sRGB: muted/neutral variant
            val srgb: Color
                @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    // ── Typography tokens — delegates to M3E expressiveTypography() ───────
    // Use these instead of calling MaterialTheme.typography.* inline so
    // every text style is controlled from one place.
    object Type {
        val screenTitle: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.titleLarge

        val categoryLabel: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.labelMedium

        val rowHeadline: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.bodyLarge

        val rowSupporting: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.bodyMedium

        val rowCaption: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.bodySmall

        val sheetTitle: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.titleMedium

        val cardTitle: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.titleSmall

        val buttonLabel: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.labelMedium

        val bannerLabel: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.labelLarge

        val infoBody: TextStyle
            @Composable @ReadOnlyComposable get() = MaterialTheme.typography.bodySmall
    }
}
