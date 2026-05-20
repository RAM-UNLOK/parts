/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Central design-token object for the Xiaomi Parts UI.
 *
 * All spacing, shape, icon-size, and motion constants live here so
 * every screen stays consistent without hardcoded literals scattered
 * across composables.
 *
 * Motion philosophy
 * ─────────────────
 * We use Spring animations exclusively (no tween for UI motion).
 * Two shared SpringSpecs cover all cases:
 *
 *   MotionSpringEnter  — elements entering the screen or expanding.
 *                        DampingRatioNoBouncy + StiffnessMediumLow
 *                        gives a smooth, unhurried deceleration.
 *
 *   MotionSpringExit   — elements leaving or collapsing.
 *                        Same damping, StiffnessMedium so the exit
 *                        is slightly faster than the enter (feels snappy
 *                        without being abrupt).
 *
 * These specs are used for:
 *   - animateFloatAsState (chevron rotation, alpha)
 *   - animateColorAsState (selection highlight)
 *   - TopAppBar snapAnimationSpec
 *
 * IMPORTANT — what we do NOT animate:
 *   AnimatedVisibility entrance on first composition is intentionally
 *   avoided. A false→true MutableTransitionState fires during the first
 *   recomposition frame and can cause a one-frame white flash before the
 *   fade-in completes. Items are instead revealed via a LaunchedEffect
 *   alpha transition after the frame is committed (see HomeScreen /
 *   ThermalManagementScreen).
 */
object PartsTokens {

    // ── Spacing ──────────────────────────────────────────
    val contentPaddingHorizontal = 16.dp
    val rowPaddingVertical        = 16.dp
    val appRowPaddingVertical     = 10.dp
    val appRowIconSpacing         = 12.dp
    val rowElementSpacing         = 12.dp
    val categoryTopPadding        = 24.dp
    val listBottomPadding         = 32.dp
    val loadingTopPadding         = 48.dp
    val disabledHintTopPadding    = 24.dp

    // ── Shape ───────────────────────────────────────────
    /** M3 Expressive large-component corner radius. */
    val cardShape = RoundedCornerShape(28.dp)

    // ── Icon sizes ────────────────────────────────────────
    val leadingIconContainerSize = 48.dp
    val leadingIconSize          = 24.dp

    // ── Dropdown ────────────────────────────────────────
    val dropdownItemVerticalPadding = 10.dp

    // ── Motion ──────────────────────────────────────────

    /**
     * Damping ratio shared by all spring animations.
     * NoBouncy = critically damped — smooth deceleration, no overshoot.
     */
    const val MotionDampingRatio = Spring.DampingRatioNoBouncy

    /**
     * Spring spec for entering / expanding elements.
     * MediumLow stiffness → unhurried, smooth arrival.
     */
    val MotionSpringEnter: SpringSpec<Float> = spring(
        dampingRatio = MotionDampingRatio,
        stiffness    = Spring.StiffnessMediumLow,
    )

    /**
     * Spring spec for exiting / collapsing elements.
     * Medium stiffness → slightly faster than enter so exits feel crisp.
     */
    val MotionSpringExit: SpringSpec<Float> = spring(
        dampingRatio = MotionDampingRatio,
        stiffness    = Spring.StiffnessMedium,
    )

    /**
     * Stiffness value used anywhere a Float spring is needed inline
     * (e.g. animateFloatAsState for chevron rotation).
     */
    const val MotionStiffnessMediumLow = Spring.StiffnessMediumLow

    // Legacy aliases kept for any composables that reference them directly.
    // New code should use MotionSpringEnter / MotionSpringExit above.
    const val MotionDurationEnter  = 280
    const val MotionDurationSlide  = 320
    const val MotionStaggerStep    = 60
    const val MotionSlideDistance  = 8
}
