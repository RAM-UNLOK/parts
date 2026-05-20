/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Single source of truth for all spacing, sizing, shape and alpha tokens
 * used across XiaomiParts screens.
 *
 * Tokens are derived from:
 *   - Clover/AOSP Settings themes_expressive.xml
 *   - Clover/AOSP Settings clover_dimens.xml
 *   - Material 3 Expressive spec (Android 16 / Pixel 10 baseline)
 *
 * Editing a value here propagates everywhere automatically.
 */
object PartsTokens {

    // ─ Screen-level layout ───────────────────────────────────────
    /** Horizontal margin for all cards and banners — 16dp system gutter. */
    val contentPaddingHorizontal = 16.dp
    /** Top padding for the first item below the app bar. */
    val listTopPadding           = 8.dp
    /** Bottom padding so the last card clears the nav bar gesture zone. */
    val listBottomPadding        = 32.dp

    // ─ Category labels ───────────────────────────────────────
    /** Space above a section-category label ("Display", "Performance" …). */
    val categoryTopPadding    = 24.dp
    /** Space between a category label and the card that follows it. */
    val categoryBottomPadding = 8.dp

    // ─ Card spacing ──────────────────────────────────────────
    /**
     * Vertical gap between two stacked PartsCards inside one category.
     * M3 Expressive spec: 16dp between list-group cards.
     */
    val cardBlockSpacing = 16.dp

    // ─ Preference rows (inside a card) ──────────────────────────
    /**
     * Vertical padding for a standard preference row.
     * Expressive row height ≈ 72dp = 16dp top + text content + 16dp bottom.
     */
    val rowPaddingVertical = 16.dp
    /** Horizontal gap between the leading icon container and text column. */
    val rowElementSpacing  = 16.dp

    // ─ App-list rows (denser than preference rows) ────────────────────
    /**
     * Vertical padding for an app-list row (app icon + label + chip).
     * Tighter than a preference row: 12dp top/bottom ≈ 64dp total height.
     */
    val appRowPaddingVertical = 12.dp
    /** Gap between the app icon and the label in an app-list row. */
    val appRowIconSpacing     = 12.dp

    // ─ Loading / empty-state helpers ─────────────────────────────
    /** Top padding for a centred loading spinner or empty-state label. */
    val loadingTopPadding      = 80.dp
    /** Top padding for the "thermal disabled" hint text. */
    val disabledHintTopPadding = 48.dp

    // ─ Icon / container sizes ────────────────────────────────────
    /** Expressive leading icon circle — 48dp (increased from 40dp in M3). */
    val leadingIconContainerSize = 48.dp
    /** Icon size inside the leading icon circle. */
    val leadingIconSize          = 24.dp
    /** Trailing chevron / secondary icon size. */
    val trailingIconSize         = 20.dp
    /** App icon size in the per-app thermal list. */
    val appIconSize              = 40.dp

    // ─ Banners ─────────────────────────────────────────────
    /** Outer horizontal margin for a full-width banner chip. */
    val bannerPaddingHorizontal = 16.dp
    /** Outer vertical margin for a full-width banner chip. */
    val bannerPaddingVertical   = 8.dp
    /** Inner horizontal padding inside a banner chip. */
    val bannerInnerPaddingH     = 20.dp
    /** Inner vertical padding inside a banner chip. */
    val bannerInnerPaddingV     = 16.dp
    /** Gap between the banner icon and the text column. */
    val bannerIconSpacing       = 14.dp
    /** Banner icon size. */
    val bannerIconSize          = 24.dp

    // ─ Elevation ─────────────────────────────────────────────
    /**
     * Cards use surfaceContainerLow colour role for tonal elevation.
     * No physical shadow — matches Pixel Settings visual language.
     * Passed as tonalElevation = 0.dp to Surface; the colour role itself
     * encodes the correct tonal value without an overlay.
     */
    val cardElevation = 0.dp

    // ─ Shapes ───────────────────────────────────────────────
    /**
     * Expressive card corner radius = 28dp.
     * Reference: Clover clover_dimens.xml
     *   about_phone_info_card_corner_radius  = 28dp
     *   about_phone_brand_card_corner_radius = 28dp
     */
    val cardShape   = RoundedCornerShape(28.dp)
    /**
     * Banner / info-pill corner radius = 28dp (full pill for short banners,
     * large-radius squircle for multi-line info cards).
     */
    val bannerShape = RoundedCornerShape(28.dp)
}
