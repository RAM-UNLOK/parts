/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.ui

import androidx.compose.ui.unit.dp

/**
 * Single source of truth for all spacing, sizing, and elevation tokens
 * used across XiaomiParts screens.
 *
 * By centralising these here:
 * - No magic dp literals scattered across composables.
 * - A single edit propagates everywhere.
 * - Easy to align with a future design-token system or Settings-style
 *   dimen resources.
 */
object PartsTokens {

    // ── Spacing ──────────────────────────────────────────────────────────
    /** Horizontal content margin matching Settings-style 16dp gutter. */
    val contentPaddingHorizontal = 16.dp
    /** Vertical padding inside a preference row. */
    val rowPaddingVertical       = 14.dp
    /** Gap between icon, text, and trailing elements in a row. */
    val rowElementSpacing        = 16.dp
    /** Vertical gap between card blocks. */
    val cardBlockSpacing         = 8.dp
    /** Bottom padding at end of scrollable list. */
    val listBottomPadding        = 24.dp
    /** Top padding before a category label. */
    val categoryTopPadding       = 20.dp
    /** Bottom padding after a category label. */
    val categoryBottomPadding    = 6.dp
    /** Horizontal padding of the charging banner inside the screen margin. */
    val bannerPaddingHorizontal  = 16.dp
    val bannerPaddingVertical    = 8.dp
    /** Internal padding of the charging banner card. */
    val bannerInnerPaddingH      = 20.dp
    val bannerInnerPaddingV      = 14.dp
    val bannerIconSpacing        = 12.dp

    // ── Icon sizes ───────────────────────────────────────────────────────
    /** Container circle for leading icons in a preference row. */
    val leadingIconContainerSize = 40.dp
    /** Icon itself inside the leading circle. */
    val leadingIconSize          = 22.dp
    /** Trailing chevron icon. */
    val trailingIconSize         = 20.dp
    /** Charging banner icon. */
    val bannerIconSize           = 24.dp
    /** App icon in per-app thermal rows. */
    val appIconSize              = 36.dp

    // ── Elevation ────────────────────────────────────────────────────────
    /** Cards are flat (tonal surface, no shadow). */
    val cardElevation            = 0.dp

    // ── M3 state-layer alpha ─────────────────────────────────────────────
    /**
     * Standard Material 3 selected-state layer alpha (0.24).
     * Used on primaryContainer for selected radio/option rows.
     * Reference: M3 spec "State layers" — selected state = 0.24 opacity.
     */
    const val selectedStateLayerAlpha = 0.24f

    /**
     * Subtle de-emphasis alpha for secondary text inside a coloured
     * container (e.g. banner subtitle).
     */
    const val containerSubtitleAlpha  = 0.75f
}
