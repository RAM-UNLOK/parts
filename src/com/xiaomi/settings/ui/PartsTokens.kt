/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.ui

import androidx.compose.animation.core.Spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Single source of truth for all spacing, sizing, shape, alpha and
 * **motion** tokens used across XiaomiParts screens.
 *
 * Tokens are derived from:
 *   - Clover/AOSP Settings themes_expressive.xml
 *   - Clover/AOSP Settings clover_dimens.xml
 *   - Material 3 Expressive spec (Android 16 / Pixel 10 baseline)
 *
 * Editing a value here propagates everywhere automatically.
 */
object PartsTokens {

    // ─ Screen-level layout ────────────────────────────────────────────
    /** Horizontal margin for all cards and banners — 16dp system gutter. */
    val contentPaddingHorizontal = 16.dp
    /** Top padding for the first item below the app bar. */
    val listTopPadding           = 8.dp
    /** Bottom padding so the last card clears the nav bar gesture zone. */
    val listBottomPadding        = 32.dp

    // ─ Category labels ──────────────────────────────────────────────
    /** Space above a section-category label ("Display", "Performance" …). */
    val categoryTopPadding    = 24.dp
    /** Space between a category label and the card that follows it. */
    val categoryBottomPadding = 8.dp

    // ─ Card spacing ────────────────────────────────────────────────
    /**
     * Vertical gap between two stacked PartsCards inside one category.
     * M3 Expressive spec: 16dp between list-group cards.
     */
    val cardBlockSpacing = 16.dp

    // ─ Preference rows (inside a card) ─────────────────────────────────
    /**
     * Vertical padding for a standard preference row.
     * Expressive row height ≈ 72dp = 16dp top + text content + 16dp bottom.
     */
    val rowPaddingVertical = 16.dp
    /** Horizontal gap between the leading icon container and text column. */
    val rowElementSpacing  = 16.dp

    // ─ App-list rows (denser than preference rows) ──────────────────────
    /**
     * Vertical padding for an app-list row (app icon + label + button).
     * Tighter than a preference row: 12dp top/bottom ≈ 64dp total height.
     */
    val appRowPaddingVertical = 12.dp
    /** Gap between the app icon and the label in an app-list row. */
    val appRowIconSpacing     = 12.dp

    // ─ Per-app thermal dropdown ─────────────────────────────────────────
    /** Minimum width for the thermal-state selector button. */
    val chipMinWidth = 160.dp
    /** Vertical padding inside each dropdown menu item. */
    val dropdownItemVerticalPadding = 14.dp

    // ─ Loading / empty-state helpers ─────────────────────────────────
    /** Top padding for a centred loading spinner or empty-state label. */
    val loadingTopPadding      = 80.dp
    /** Top padding for the "thermal disabled" hint text. */
    val disabledHintTopPadding = 48.dp

    // ─ Icon / container sizes ─────────────────────────────────────────
    /** Expressive leading icon circle — 48dp (increased from 40dp in M3). */
    val leadingIconContainerSize = 48.dp
    /** Icon size inside the leading icon circle. */
    val leadingIconSize          = 24.dp
    /** Trailing chevron / secondary icon size. */
    val trailingIconSize         = 20.dp
    /** App icon size in the per-app thermal list. */
    val appIconSize              = 40.dp

    // ─ Banners ──────────────────────────────────────────────────
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

    // ─ Elevation ──────────────────────────────────────────────────
    /**
     * Cards use surfaceContainerLow colour role for tonal elevation.
     * No physical shadow — matches Pixel Settings visual language.
     */
    val cardElevation = 0.dp

    // ─ Shapes ─────────────────────────────────────────────────────
    /**
     * Expressive card corner radius = 28dp.
     * Reference: Clover clover_dimens.xml
     *   about_phone_info_card_corner_radius  = 28dp
     *   about_phone_brand_card_corner_radius = 28dp
     */
    val cardShape   = RoundedCornerShape(28.dp)
    /**
     * Banner / info-pill corner radius = 28dp.
     */
    val bannerShape = RoundedCornerShape(28.dp)

    // ─ Motion ─────────────────────────────────────────────────────────
    //
    // Single source of truth for all animation durations, spring params
    // and stagger values. Every screen references these constants so
    // changing one value here propagates everywhere automatically.
    //
    // Curve: EaseOutCubic — M3 Expressive "Emphasized Decelerate" easing.
    // Used on ALL tween-based enter transitions (slide, fade, route nav).
    // Exit transitions use the same curve reversed (element accelerates out).
    //
    // Spring params map to M3 Expressive spec:
    //   DampingRatioNoBouncy + StiffnessMediumLow  → smooth settling, no overshoot
    //   DampingRatioNoBouncy + StiffnessMedium     → snappier exit / collapse
    //   DampingRatioNoBouncy + StiffnessHigh       → snap (scroll behaviour)

    /** Fade-in / fade-out duration for content elements entering the screen. */
    const val MotionDurationEnter: Int = 220

    /** Fade-out duration for content elements leaving the screen. */
    const val MotionDurationExit: Int = 160

    /**
     * Slide (translateY) duration for staggered section entrance.
     * Longer than the fade so the element is still moving as it becomes
     * fully opaque — produces a more physical, continuous feel.
     */
    const val MotionDurationSlide: Int = 400

    /**
     * Full route transition duration (screen → screen slide).
     * Asymmetric: enter=350ms (deliberate arrival), exit=250ms (quick departure).
     */
    const val MotionDurationRoute: Int = 350

    /**
     * Stagger step between section groups on the home screen.
     * Groups: Display (0 ms), Performance (1×step), Diagnostics (2×step).
     */
    const val MotionStaggerStep: Int = 60

    /**
     * Initial Y-offset divisor for slideInVertically.
     * initialOffsetY = { it / MotionSlideDistance } → 1/5 of item height.
     * Keeps the slide subtle (not a full-screen swoosh) while still giving
     * a clear directional cue.
     */
    const val MotionSlideDistance: Int = 5

    /**
     * Spring damping ratio used across all spring-based animations.
     * NoBouncy = critically damped — settles without oscillation.
     */
    val MotionDampingRatio: Float = Spring.DampingRatioNoBouncy

    /**
     * Spring stiffness for enter / expand animations.
     * MediumLow = leisurely settle, appropriate for elements arriving on screen.
     */
    val MotionStiffnessMediumLow: Float = Spring.StiffnessMediumLow

    /**
     * Spring stiffness for exit / collapse animations.
     * Medium = slightly snappier so the exit doesn't drag.
     */
    val MotionStiffnessMedium: Float = Spring.StiffnessMedium
}
