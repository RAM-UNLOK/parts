/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Central design-token object for the Xiaomi Parts UI.
 *
 * Motion values mirror the M3 Expressive spring presets:
 *   - Enter  → DampingRatioNoBouncy + StiffnessMediumLow  (smooth arrival)
 *   - Exit   → DampingRatioNoBouncy + StiffnessMedium     (crisp departure)
 *
 * Spring constants are referenced directly from [Spring] at call sites;
 * the two [SpringSpec] helpers here exist only so composables can pass a
 * pre-built spec without constructing one inline.
 */
object PartsTokens {

    // ── Spacing ──────────────────────────────────────────
    val contentPaddingHorizontal = 16.dp
    val rowPaddingVertical        = 16.dp
    val appRowPaddingVertical     = 10.dp
    val appRowIconSpacing         = 12.dp
    val rowElementSpacing         = 12.dp
    val categoryTopPadding        = 24.dp
    val categoryBottomPadding     = 8.dp
    val listBottomPadding         = 32.dp
    val loadingTopPadding         = 48.dp
    val disabledHintTopPadding    = 24.dp
    /** Vertical gap between category blocks on the HomeScreen. */
    val cardBlockSpacing          = 8.dp

    // ── Shape ───────────────────────────────────────────
    /** M3 Expressive large-component corner radius. */
    val cardShape = RoundedCornerShape(28.dp)

    // ── Chip (per-app thermal selector) ─────────────────
    val thermalChipWidth  = 120.dp
    val thermalChipHeight = 36.dp

    // ── Icon sizes ────────────────────────────────────────
    val leadingIconContainerSize = 48.dp
    val leadingIconSize          = 24.dp
    val trailingIconSize         = 24.dp

    // ── Motion ──────────────────────────────────────────
    // Use Spring.* constants directly at call sites for the raw dampingRatio
    // and stiffness values — no indirection needed.
    //
    // These two pre-built specs are provided for composables that accept a
    // SpringSpec<Float> parameter (e.g. scroll behavior snap specs).

    /** M3 Expressive enter spring — smooth, no bounce. */
    val MotionSpringEnter: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness    = Spring.StiffnessMediumLow,
    )

    /** M3 Expressive exit spring — slightly crisper departure. */
    val MotionSpringExit: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness    = Spring.StiffnessMedium,
    )

    // ── Motion durations (ms) ────────────────────────────
    /** Enter duration: fade-in portion of enter transitions. */
    const val MotionDurationEnter  = 280
    /** Slide duration for list stagger animations. */
    const val MotionDurationSlide  = 320
    /** Full route enter transition duration (slide + fade). */
    const val MotionDurationRoute  = 350
    /** Exit / pop-exit transition duration — faster than enter. */
    const val MotionDurationExit   = 250
    /** Stagger delay step between animated list items. */
    const val MotionStaggerStep    = 60
    const val MotionSlideDistance  = 8
}
