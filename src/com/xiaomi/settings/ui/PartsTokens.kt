/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Central design-token object for the Xiaomi Parts UI.
 *
 * All spacing, shape, icon-size, motion, alpha, and colour-role constants
 * live here so every screen stays visually consistent without hardcoded
 * dp / ms / alpha / colorScheme values scattered across composables.
 *
 * ## Usage
 * ```kotlin
 * Modifier.padding(horizontal = PartsTokens.contentPaddingHorizontal)
 * Surface(shape = PartsTokens.cardShape)
 * Icon(modifier = Modifier.size(PartsTokens.leadingIconSize))
 * Box(Modifier.background(PartsTokens.Colors.iconContainer))
 * ```
 */
object PartsTokens {

    // ── Spacing ────────────────────────────────────

    val contentPaddingHorizontal   = 16.dp
    val rowPaddingVertical          = 16.dp
    val appRowPaddingVertical       = 10.dp
    val appRowIconSpacing           = 12.dp
    val rowElementSpacing           = 12.dp
    val categoryTopPadding          = 24.dp
    val categoryBottomPadding       = 8.dp
    val listBottomPadding           = 32.dp
    val loadingTopPadding           = 48.dp
    val disabledHintTopPadding      = 24.dp
    val cardBlockSpacing            = 8.dp
    val loadingSpinnerLabelSpacing  = 12.dp
    val chargingHintIndent          = 32.dp

    // Section headers (DisplayColoursScreen and similar)
    /** Left indent for section-header labels — wider than content to align with M3 list spec. */
    val sectionHeaderStartPadding   = 24.dp
    /** Vertical breathing room above a section-header label. */
    val sectionHeaderTopPadding     = 20.dp
    // bottom padding reuses categoryBottomPadding (8.dp)

    // Dialog — outer shell
    /** Top/bottom padding of the root dialog Column. */
    val dialogOuterPaddingVertical  = 12.dp

    // Dialog — header row
    /** Top padding of the header Row — per M3 dialog header spec. */
    val dialogHeaderTopPadding      = 24.dp
    /** Bottom padding of the header Row. */
    val dialogHeaderBottomPadding   = 12.dp
    /** Spacing between app icon and text block in the dialog header. */
    val dialogHeaderIconSpacing     = 16.dp

    // Dialog — selection list
    /** Horizontal inset for selection rows so the pill doesn’t bleed to the dialog edge. */
    val dialogRowHorizontalInset    = 8.dp
    /** Inner vertical padding of each selection row (≈ 64dp total touch target). */
    val dialogRowPaddingVertical    = 12.dp
    /** Maximum height of the profile LazyColumn before it scrolls. */
    val dialogListMaxHeight: Dp     = 400.dp
    /** Top/bottom contentPadding of the profile LazyColumn. */
    val dialogListContentPadding    = 4.dp

    // Dialog — actions row
    /** Top padding above the Cancel / action TextButton row. */
    val dialogActionsTopPadding     = 4.dp

    // ── Shape ────────────────────────────────────

    /** M3 Extra-Large corner radius — all cards and dialogs. */
    val cardShape = RoundedCornerShape(28.dp)
    /**
     * Full-pill shape for chips and inline surfaces.
     * Uses [CircleShape] (= RoundedCornerShape(50%)) to match the M3
     * “full” shape token.
     */
    val chipShape: Shape = CircleShape
    /**
     * Rounded-rectangle highlight for a selected row inside a dialog picker.
     * Smaller than [cardShape] so it sits comfortably inside the dialog shell.
     */
    val dialogSelectionShape = RoundedCornerShape(12.dp)

    // ── Icon sizes ───────────────────────────────

    val leadingIconContainerSize    = 48.dp
    val leadingIconSize             = 24.dp
    val trailingIconSize            = 24.dp
    /** Icon inside the per-app thermal profile chip. */
    val chipIconSize                = 14.dp
    /** Icon inside charging / info banners. */
    val bannerIconSize              = 20.dp
    /** Dismiss “X” icon inside the home-screen charging banner. */
    val bannerDismissIconSize       = 16.dp
    /** Size of the dismiss IconButton in the charging banner. */
    val dismissButtonSize           = 32.dp

    // Dialog icon sizes
    /** App icon displayed in the ThermalProfileDialog header. */
    val dialogHeaderIconSize        = 40.dp
    /** Leading icon container size inside dialog selection rows. */
    val dialogRowIconSize           = 40.dp
    /** Inner icon (ImageVector) inside the dialog row leading container. */
    val dialogRowIconInnerSize      = 22.dp

    // Display Colours screen
    /** Height of the ColourPreviewHero card at the top of DisplayColoursScreen. */
    val heroPreviewHeight           = 180.dp
    /** Container circle size for the per-mode leading swatch in SelectionRow. */
    val colourModeLeadingSize       = 40.dp
    /** Inner filled dot size inside the leading colour swatch circle. */
    val colourModeDotSize           = 16.dp

    // ── Chip (per-app thermal selector) ────────────────

    val chipHeight   = 36.dp
    val chipMinWidth = 96.dp
    val chipMaxWidth = 148.dp

    // ── Alpha constants ────────────────────────────

    /** Background circle alpha for the colour-mode swatch in DisplayColoursScreen. */
    const val colourModeIconContainerAlpha = 0.20f
    /** Filled dot alpha for the colour-mode swatch in DisplayColoursScreen. */
    const val colourModeIconDotAlpha       = 0.80f
    /** Row background alpha when a colour-mode row is selected. */
    const val colourModeSelectedAlpha      = 0.30f

    // ── Motion ────────────────────────────────────

    /**
     * Standard enter spring: no bounce, medium-low stiffness.
     * Use for elements entering the screen (slides, fades, scale-ins).
     */
    val MotionSpringEnter: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness    = Spring.StiffnessMediumLow,
    )

    /** Fade-in duration (ms) for the trailing CheckCircle in selection rows. */
    const val motionCheckFadeInMs  = 180
    /** Fade-out duration (ms) for the trailing CheckCircle in selection rows. */
    const val motionCheckFadeOutMs = 120

    // ── Colour roles ──────────────────────────────

    /**
     * Semantic colour roles for all Parts screens.
     *
     * Every property is @Composable + @ReadOnlyComposable so it can be
     * used inline anywhere in a Compose tree without wrapping in
     * remember {}. The values read directly from MaterialTheme.colorScheme
     * which is already backed by dynamicDark/LightColorScheme — the
     * single source of truth for Monet colours in this app.
     *
     * Screens must NOT reference MaterialTheme.colorScheme.* directly.
     * Use PartsTokens.Colors.* everywhere so role→token mapping is one
     * central decision.
     */
    object Colors {

        // ── Page ────────────────────────────────────

        /** Background of every Scaffold / page. */
        val page: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.surface

        /** TopAppBar background when not scrolled. */
        val topBarResting: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.surface

        /** TopAppBar background when scrolled (elevated). */
        val topBarScrolled: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.surfaceContainer

        // ── Cards ────────────────────────────────────

        /** Surface colour for PartsCard (sits on [page]). */
        val card: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.surfaceContainer

        // ── Leading icon container ───────────────────────

        /** Background circle behind the leading icon in PartsRow. */
        val iconContainer: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.secondaryContainer

        val iconContent: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSecondaryContainer

        // ── Text ───────────────────────────────────────

        val textPrimary: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSurface

        val textSecondary: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSurfaceVariant

        val textCategory: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.primary

        // ── Per-app thermal chip ─────────────────────────

        val chipContainer: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.secondaryContainer

        val chipContent: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSecondaryContainer

        // ── Dividers ──────────────────────────────────

        val divider: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.outlineVariant

        // ── Banners (charging / info) ─────────────────────

        val bannerContainer: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.tertiaryContainer

        val bannerContent: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onTertiaryContainer

        // ── Trailing navigation arrow ─────────────────────

        val trailing: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSurfaceVariant

        // ── Dialog ────────────────────────────────────

        /** Elevated surface for the profile-picker dialog. */
        val dialogSurface: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.surfaceContainerHigh

        /**
         * Tinted background for the selected row inside a dialog.
         *
         * Uses M3 pressed/selected state-layer opacity (12 %) over
         * primaryContainer — consistent with M3 selection highlight spec.
         */
        val dialogSelectedBackground: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)

        val dialogSelectedText: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.primary

        // ── Error / destructive ─────────────────────────

        val destructive: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.error

        // ── Display Colours hero ────────────────────────

        /**
         * Near-black base colour for the ColourPreviewHero card.
         * Kept as a static constant (not Monet-derived) because the hero
         * is intentionally dark so the coloured blobs maximally pop.
         */
        val heroBase: Color = Color(0xFF0D0D0D)

        // ── Display Colours — per-mode characteristic hues ───
        //
        // These are illustrative / artistic colours — NOT dynamic Monet
        // colours — because they represent the visual identity of each
        // colour profile (e.g. Vivid = warm amber, P3 = wide-gamut violet).
        // They are used for the hero preview blobs and the leading swatch
        // circles in the selection rows.

        object Hues {
            /** Warm amber-orange: Vivid profile. */
            val vivid:     Color = Color(0xFFFF7043)
            /** Fresh lime: Saturated profile. */
            val saturated: Color = Color(0xFF8BC34A)
            /** Cool slate: Standard profile. */
            val standard:  Color = Color(0xFF90A4AE)
            /** Warm gold: Original profile. */
            val original:  Color = Color(0xFFFFCA28)
            /** Wide-gamut violet: DCI-P3 profile. */
            val p3:        Color = Color(0xFF9C27B0)
            /** Calibrated teal: sRGB profile. */
            val srgb:      Color = Color(0xFF26C6DA)
        }
    }
}
