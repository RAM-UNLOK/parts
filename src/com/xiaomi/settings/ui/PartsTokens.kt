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
 * All spacing and size values are aligned to the Material Design 3
 * specification (md.material.io, component tokens as of M3 1.3 / Compose
 * Material3 1.3.x).
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

    // ── Spacing ────────────────────────────────────────────────────────────
    //
    // All values derived from the M3 spacing scale (4 dp grid) and the
    // component-level inset / padding tokens defined at:
    // https://m3.material.io/components

    /** Horizontal content padding — aligns with M3 ListItem leading inset (16 dp). */
    val contentPaddingHorizontal    = 16.dp

    /**
     * Vertical padding for full-height setting rows (icon + two-line text).
     * M3 ListItem two-line height = 72 dp → 16 dp top + 16 dp bottom.
     */
    val rowPaddingVertical          = 16.dp

    /**
     * Vertical padding for compact app-list rows inside the thermal screen.
     * M3 ListItem one-line height = 56 dp → 10 dp top + 10 dp bottom
     * (with a 36 dp icon container the maths resolves to 56 dp total).
     */
    val appRowPaddingVertical       = 10.dp

    /**
     * Horizontal gap between sibling elements in a row
     * (icon → text column, text column → trailing).
     * M3 ListItem horizontal gap = 16 dp; 12 dp is used here because the
     * leading container already contributes 4 dp of visual spacing on each
     * side, keeping the effective gap at the spec value.
     */
    val rowElementSpacing           = 12.dp

    /**
     * Top padding above a category label.
     * M3 list section gap = 16 dp.
     */
    val categoryTopPadding          = 16.dp

    /**
     * Bottom padding below a category label before its first list item.
     * M3 subheader bottom inset = 4 dp so the label hugs its first item.
     */
    val categoryBottomPadding       = 4.dp

    /** Bottom padding after the last list item — M3 list bottom inset = 24 dp. */
    val listBottomPadding           = 24.dp

    /** Top padding for the full-screen loading state. Not in M3 spec — intentional breathing room. */
    val loadingTopPadding           = 48.dp

    /** Top padding for the "per-app thermal disabled" hint text. Intentional breathing room. */
    val disabledHintTopPadding      = 24.dp

    /** Vertical gap between two PartsCard blocks. M3 list section gap = 8 dp. */
    val cardBlockSpacing            = 8.dp

    /** Gap between the CircularProgressIndicator and its loading label below it. */
    val loadingSpinnerLabelSpacing  = 12.dp

    /** Leading indent for the "per-app thermal" charging-hint text below the switch. */
    val chargingHintIndent          = 32.dp

    // ── Section headers (DisplayColoursScreen and similar) ─────────────────

    /**
     * Leading inset for section-header labels.
     * M3 ListItem leading inset = 16 dp — matches contentPaddingHorizontal.
     */
    val sectionHeaderStartPadding   = 16.dp

    /**
     * Top padding above a section-header label.
     * M3 list section gap = 16 dp.
     */
    val sectionHeaderTopPadding     = 16.dp
    // Bottom padding reuses categoryBottomPadding (4 dp).

    // ── Dialog — outer shell ────────────────────────────────────────────────

    /**
     * Top and bottom padding of the root dialog Column.
     * M3 BasicAlertDialog content padding = 16 dp top/bottom.
     */
    val dialogOuterPaddingVertical  = 16.dp

    // ── Dialog — header row ─────────────────────────────────────────────────

    /**
     * Top padding of the header Row inside the dialog.
     * M3 AlertDialog header top = 24 dp.
     */
    val dialogHeaderTopPadding      = 24.dp

    /**
     * Bottom padding of the dialog header Row — gap between header and body.
     * M3 AlertDialog header-to-body gap = 16 dp.
     */
    val dialogHeaderBottomPadding   = 16.dp

    /**
     * Spacing between the app icon and the text block in the dialog header.
     * M3 ListItem gap between leading widget and text column = 16 dp.
     */
    val dialogHeaderIconSpacing     = 16.dp

    /**
     * Horizontal gap between elements inside dialog selection list rows
     * (leading icon container → label text).
     * Separate from dialogHeaderIconSpacing so changes to the header layout
     * do not affect the list rows and vice versa.
     * Value = rowElementSpacing (12 dp) — same reasoning as the main row.
     */
    val dialogRowElementSpacing     = 12.dp

    // ── Dialog — selection list ─────────────────────────────────────────────

    /**
     * Horizontal inset so the selection pill does not bleed to the dialog edge.
     * 8 dp gives the pill a visible margin within the dialog card.
     */
    val dialogRowHorizontalInset    = 8.dp

    /**
     * Inner vertical padding of each selection row.
     * M3 ListItem one-line height = 56 dp → 12 dp top + 12 dp bottom
     * (with a 32 dp icon container this resolves to exactly 56 dp).
     */
    val dialogRowPaddingVertical    = 12.dp

    /** Maximum height of the profile LazyColumn before it scrolls. */
    val dialogListMaxHeight: Dp     = 400.dp

    /**
     * Top / bottom contentPadding of the profile LazyColumn.
     * M3 list top/bottom inset = 8 dp.
     */
    val dialogListContentPadding    = 8.dp

    // ── Dialog — actions row ────────────────────────────────────────────────

    /**
     * Top padding above the Cancel / action TextButton row.
     * M3 AlertDialog actions top padding = 8 dp.
     */
    val dialogActionsTopPadding     = 8.dp

    // ── Shape ───────────────────────────────────────────────────────────────
    //
    // M3 shape scale reference:
    //   ExtraSmall =  4 dp   Small  =  8 dp   Medium = 12 dp
    //   Large      = 16 dp   ExtraLarge = 28 dp   Full = 50 %

    /**
     * M3 ExtraLarge corner radius (28 dp) — used for all full-width Cards.
     * M3 Card component specifies the ExtraLarge shape token for its default corner.
     */
    val cardShape: Shape = RoundedCornerShape(28.dp)

    /**
     * Rounded-rectangle highlight for a selected row inside a dialog picker.
     * M3 Medium shape token = 12 dp, used for List-Item state-layer containers
     * inside dialog bodies.
     */
    val dialogSelectionShape: Shape = RoundedCornerShape(12.dp)

    // ── Icon sizes ──────────────────────────────────────────────────────────
    //
    // M3 icon size tokens:
    //   Standard icon  = 24 dp
    //   Small icon     = 20 dp  (M3 1.3 chip leading icon = 18 dp)
    //   ListItem avatar / leading container = 40 dp

    /**
     * Circular leading icon container — M3 ListItem avatar size = 40 dp.
     * Note: 48 dp is the touch-target floor, not the visual container size.
     */
    val leadingIconContainerSize    = 40.dp

    /** Icon inside the leading container — M3 standard icon size = 24 dp. */
    val leadingIconSize             = 24.dp

    /** Trailing chevron / check icon — M3 standard icon size = 24 dp. */
    val trailingIconSize            = 24.dp

    /**
     * App icon in AppThermalRow list items and the ThermalProfileDialog header.
     * 40 dp matches M3 ListItem avatar / thumbnail size.
     * Kept as its own token so the app-icon size and the dialog-header icon
     * size can diverge independently in future.
     */
    val appIconSize                 = 40.dp

    /**
     * Icon inside charging / info banners.
     * M3 standard icon size = 24 dp; unified across all banners.
     */
    val bannerIconSize              = 24.dp

    /**
     * Dismiss "X" icon inside the home-screen charging banner.
     * M3 minimum icon size = 18 dp, chosen to visually subordinate the
     * close button relative to the content icon beside it.
     */
    val bannerDismissIconSize       = 18.dp

    /**
     * Size of the dismiss IconButton in the charging banner.
     * M3 IconButton has a built-in 48 dp touch target; the visual container
     * can be smaller — 40 dp is the M3 dense-context icon-button container size.
     */
    val dismissButtonSize           = 40.dp

    // Dialog icon sizes

    /** Leading icon container inside dialog selection rows — M3 avatar = 40 dp. */
    val dialogRowIconSize           = 40.dp

    /**
     * Inner icon (ImageVector) inside the dialog-row leading container.
     * 22 dp — slightly smaller than 24 dp standard so it breathes inside
     * the 40 dp container with 9 dp padding on each side.
     */
    val dialogRowIconInnerSize      = 22.dp

    // Display Colours screen

    /** Height of the ColourPreviewHero card at the top of DisplayColoursScreen. */
    val heroPreviewHeight           = 180.dp

    /** Container circle size for the per-mode leading swatch in SelectionRow. */
    val colourModeLeadingSize       = 40.dp

    /** Inner filled dot size inside the leading colour swatch circle. */
    val colourModeDotSize           = 16.dp

    // ── Alpha constants ─────────────────────────────────────────────────────
    //
    // M3 state-layer opacity tokens (md.material.io/foundations/interaction):
    //   Hovered   = 0.08    Focused  = 0.12
    //   Pressed   = 0.12    Selected = 0.12    Dragged = 0.16

    /** Background circle alpha for the colour-mode swatch (illustrative, not a state layer). */
    const val colourModeIconContainerAlpha = 0.20f

    /** Filled dot alpha for the colour-mode swatch. */
    const val colourModeIconDotAlpha       = 0.80f

    /**
     * M3 selected state-layer opacity = 0.12.
     *
     * Single source of truth for all selected-row highlights across the app
     * (DisplayColoursScreen selection rows, ThermalProfileDialog selection rows).
     * Previous value was 0.30 which caused over-saturated selection highlights
     * and is not compliant with the M3 state-layer specification.
     */
    const val selectedStateLayerAlpha      = 0.12f

    // ── Motion ──────────────────────────────────────────────────────────────

    /**
     * Standard enter spring: no bounce, medium-low stiffness.
     * Matches the M3 Emphasized Decelerate easing intent for entering elements.
     */
    val MotionSpringEnter: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness    = Spring.StiffnessMediumLow,
    )

    /** Fade-in duration (ms) for the trailing CheckCircle in selection rows. */
    const val motionCheckFadeInMs  = 180

    /** Fade-out duration (ms) for the trailing CheckCircle in selection rows. */
    const val motionCheckFadeOutMs = 120

    // ── Colour roles ────────────────────────────────────────────────────────

    /**
     * Semantic colour roles for all Parts screens.
     *
     * Every property is @Composable + @ReadOnlyComposable so it can be
     * used inline anywhere in a Compose tree without wrapping in
     * remember {}. The values read directly from MaterialTheme.colorScheme
     * which is already backed by dynamicDark/LightColorScheme — the
     * single source of truth for Monet colours in this app.
     *
     * Screens MUST NOT reference MaterialTheme.colorScheme.* directly.
     * Use PartsTokens.Colors.* everywhere so the role→token mapping is
     * one central decision.
     */
    object Colors {

        // ── Page ───────────────────────────────────────────────────────────

        /** Background of every Scaffold / page. M3: surface. */
        val page: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.surface

        /** TopAppBar background when not scrolled. M3: surface. */
        val topBarResting: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.surface

        /** TopAppBar background when scrolled (elevated). M3: surfaceContainer. */
        val topBarScrolled: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.surfaceContainer

        // ── Cards ──────────────────────────────────────────────────────────

        /** Surface colour for PartsCard. M3: surfaceContainer. */
        val card: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.surfaceContainer

        // ── Leading icon container ─────────────────────────────────────────

        /** Background circle behind the leading icon in PartsRow. M3: secondaryContainer. */
        val iconContainer: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.secondaryContainer

        /** Foreground icon tint inside the leading container. M3: onSecondaryContainer. */
        val iconContent: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSecondaryContainer

        // ── Text ───────────────────────────────────────────────────────────

        /** Primary body text. M3: onSurface. */
        val textPrimary: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSurface

        /** Secondary / supporting text. M3: onSurfaceVariant. */
        val textSecondary: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSurfaceVariant

        /**
         * Category label text (PartsCategory).
         * M3 list-subheader spec uses secondary for a mid-weight emphasis
         * that sits above body text but below primary actions.
         */
        val textCategory: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.secondary

        /**
         * Section-header label text (e.g. "Standard", "Expert" headings
         * in DisplayColoursScreen).
         * M3 list anatomy: section-header / supporting text = onSurfaceVariant.
         */
        val sectionHeader: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSurfaceVariant

        // ── Dividers ───────────────────────────────────────────────────────

        /** Row divider line. M3: outlineVariant. */
        val divider: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.outlineVariant

        // ── Banners (charging / info) ──────────────────────────────────────

        /** Banner card background. M3: tertiaryContainer. */
        val bannerContainer: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.tertiaryContainer

        /** Text and icon tint inside banners. M3: onTertiaryContainer. */
        val bannerContent: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onTertiaryContainer

        // ── Trailing navigation arrow ──────────────────────────────────────

        /** Trailing chevron icon tint. M3: onSurfaceVariant. */
        val trailing: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSurfaceVariant

        // ── Per-app thermal chip ───────────────────────────────────────────

        /** Chip / pill surface. M3: secondaryContainer. */
        val chipContainer: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.secondaryContainer

        /** Chip text and icon tint. M3: onSecondaryContainer. */
        val chipContent: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSecondaryContainer

        // ── Dialog ─────────────────────────────────────────────────────────

        /**
         * Elevated surface for the profile-picker dialog.
         * M3 dialog container = surfaceContainerHigh (+2 tonal elevation
         * above the page surface, matching the M3 dialog spec).
         */
        val dialogSurface: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.surfaceContainerHigh

        /**
         * Tinted highlight for the selected row inside a dialog.
         * M3 selected state-layer: primaryContainer @ [selectedStateLayerAlpha] (0.12).
         */
        val dialogSelectedBackground: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = selectedStateLayerAlpha,
                )

        /** Text tint for the selected-state label in a dialog row. M3: primary. */
        val dialogSelectedText: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.primary

        // ── Error / destructive ────────────────────────────────────────────

        /** Destructive action colour. M3: error. */
        val destructive: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.error

        // ── Display Colours hero ───────────────────────────────────────────

        /**
         * Near-black base for the ColourPreviewHero card.
         * Intentionally static (not Monet-derived) so the coloured blobs
         * always pop regardless of the system's dynamic colour seed.
         */
        val heroBase: Color = Color(0xFF0D0D0D)

        // ── Display Colours — per-mode characteristic hues ─────────────────
        //
        // Illustrative / artistic colours — NOT dynamic Monet colours.
        // They represent the visual identity of each colour profile and
        // are used in the hero preview blobs and the leading swatch circles.

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
