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
import androidx.compose.ui.unit.dp

/**
 * Central design-token object for the Xiaomi Parts UI.
 *
 * All spacing, shape, icon-size, motion, and colour-role constants
 * live here so every screen stays visually consistent without
 * hardcoded dp / ms / colorScheme values scattered across composables.
 *
 * ## Usage
 * ```kotlin
 * // Spacing / shape / icon size
 * Modifier.padding(horizontal = PartsTokens.contentPaddingHorizontal)
 * Surface(shape = PartsTokens.cardShape)
 * Icon(modifier = Modifier.size(PartsTokens.leadingIconSize))
 *
 * // Color roles (require Compose context)
 * Box(Modifier.background(PartsTokens.Colors.iconContainer))
 * Icon(tint = PartsTokens.Colors.iconContent)
 * Surface(color = PartsTokens.Colors.card)
 * ```
 */
object PartsTokens {

    // ── Spacing ─────────────────────────────────────────────
    val contentPaddingHorizontal  = 16.dp
    val rowPaddingVertical         = 16.dp
    val appRowPaddingVertical      = 10.dp
    val appRowIconSpacing          = 12.dp
    val rowElementSpacing          = 12.dp
    val categoryTopPadding         = 24.dp
    val categoryBottomPadding      = 8.dp
    val listBottomPadding          = 32.dp
    val loadingTopPadding          = 48.dp
    val disabledHintTopPadding     = 24.dp
    val cardBlockSpacing           = 8.dp
    val loadingSpinnerLabelSpacing = 12.dp
    val chargingHintIndent         = 32.dp

    // ── Shape ───────────────────────────────────────────────
    /** M3 Expressive large-component corner radius (all cards). */
    val cardShape = RoundedCornerShape(28.dp)
    /** Smaller shape for chips and inline surfaces. */
    val chipShape = RoundedCornerShape(50)

    // ── Icon sizes ──────────────────────────────────────────
    val leadingIconContainerSize = 48.dp
    val leadingIconSize          = 24.dp
    val trailingIconSize         = 24.dp
    /** Icon inside the per-app thermal profile chip. */
    val chipIconSize             = 14.dp

    // ── Chip (per-app thermal selector) ────────────────────
    val chipHeight = 36.dp
    val chipMinWidth = 96.dp
    val chipMaxWidth = 148.dp

    // ── Dropdown ───────────────────────────────────────────
    val dropdownItemVerticalPadding = 10.dp

    // ── Motion ─────────────────────────────────────────────
    const val MotionDampingRatio       = Spring.DampingRatioNoBouncy
    const val MotionStiffnessMediumLow = Spring.StiffnessMediumLow

    val MotionSpringEnter: SpringSpec<Float> = spring(
        dampingRatio = MotionDampingRatio,
        stiffness    = Spring.StiffnessMediumLow,
    )
    val MotionSpringExit: SpringSpec<Float> = spring(
        dampingRatio = MotionDampingRatio,
        stiffness    = Spring.StiffnessMedium,
    )

    // ── Motion durations (ms) ──────────────────────────────
    const val MotionDurationEnter  = 280
    const val MotionDurationSlide  = 320
    const val MotionDurationRoute  = 350
    const val MotionDurationExit   = 250
    const val MotionStaggerStep    = 60
    const val MotionSlideDistance  = 8

    // ── Colour roles ────────────────────────────────────────
    /**
     * Semantic colour roles for all Parts screens.
     *
     * Every property is @Composable + @ReadOnlyComposable so it can be
     * used inline anywhere in a Compose tree without wrapping in
     * remember {}. The values read directly from MaterialTheme.colorScheme
     * which is already backed by dynamicDark/LightColorScheme — the
     * single source of truth for Monet colors in this app.
     *
     * Screens must NOT reference MaterialTheme.colorScheme.* directly.
     * Use PartsTokens.Colors.* everywhere so role→token mapping is one
     * central decision.
     */
    object Colors {

        // Page
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

        // Cards
        /** Surface color for PartsCard (sits on [page]). */
        val card: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.surfaceContainer

        // Leading icon container (circle behind icon in PartsRow)
        val iconContainer: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.secondaryContainer

        val iconContent: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSecondaryContainer

        // Text
        val textPrimary: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSurface

        val textSecondary: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSurfaceVariant

        val textCategory: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.primary

        // Per-app thermal chip
        val chipContainer: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.secondaryContainer

        val chipContent: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSecondaryContainer

        // Dividers
        val divider: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.outlineVariant

        // Banners (charging / info)
        val bannerContainer: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.tertiaryContainer

        val bannerContent: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onTertiaryContainer

        // Trailing navigation arrow
        val trailing: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.onSurfaceVariant

        // Dialog surface (elevated above page)
        val dialogSurface: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.surfaceContainerHigh

        // Selected row in dialog
        val dialogSelectedBackground: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)

        val dialogSelectedText: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.primary

        val dialogRowBackground: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.secondaryContainer

        // Error / destructive
        val destructive: Color
            @Composable @ReadOnlyComposable get() =
                MaterialTheme.colorScheme.error
    }
}
