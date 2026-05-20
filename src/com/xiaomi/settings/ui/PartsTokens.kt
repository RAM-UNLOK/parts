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
 *   - Material 3 Expressive spec
 *
 * Editing a value here propagates everywhere automatically.
 */
object PartsTokens {

    // ─ Spacing ───────────────────────────────────────────────────────────
    val contentPaddingHorizontal = 16.dp
    /** Expressive row height ~72dp = 16dp top + text + 16dp bottom */
    val rowPaddingVertical       = 16.dp
    val rowElementSpacing        = 16.dp
    /** Clover card spacing = 12dp */
    val cardBlockSpacing         = 12.dp
    val listBottomPadding        = 32.dp
    val categoryTopPadding       = 24.dp
    val categoryBottomPadding    = 8.dp
    val bannerPaddingHorizontal  = 16.dp
    val bannerPaddingVertical    = 8.dp
    val bannerInnerPaddingH      = 20.dp
    val bannerInnerPaddingV      = 16.dp
    val bannerIconSpacing        = 14.dp

    // ─ Icon / container sizes ────────────────────────────────────────
    /** Expressive leading icon circle = 48dp (up from 40dp) */
    val leadingIconContainerSize = 48.dp
    val leadingIconSize          = 24.dp
    val trailingIconSize         = 20.dp
    val bannerIconSize           = 24.dp
    val appIconSize              = 40.dp

    // ─ Elevation ──────────────────────────────────────────────────────
    val cardElevation = 0.dp

    // ─ Shapes ──────────────────────────────────────────────────────────
    /**
     * Expressive card corner radius = 28dp.
     * Reference: Clover clover_dimens.xml
     *   about_phone_info_card_corner_radius = 28dp
     *   about_phone_brand_card_corner_radius = 28dp
     */
    val cardShape      = RoundedCornerShape(28.dp)
    /** Banner / info pill = 28dp full pill */
    val bannerShape    = RoundedCornerShape(28.dp)

    // ─ M3 state-layer alphas ─────────────────────────────────────────
    /** M3 selected state layer alpha = 0.24 */
    const val selectedStateLayerAlpha = 0.24f
    /** Secondary text inside a coloured container */
    const val containerSubtitleAlpha  = 0.75f
}
