/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colour tokens for Settings screens.
 *
 * Every token maps to a canonical M3 dynamic colour role so the UI
 * inherits Monet wallpaper colours automatically on Android 12+.
 *
 * Light / dark switching is handled by MaterialTheme — callers never
 * need to check isSystemInDarkTheme() for surface or text colours.
 */
@Immutable
data class SettingsColorScheme(
    // ── Backgrounds ───────────────────────────────────────────────────────
    /** Page-level background (behind all cards). Maps to M3 surface. */
    val screenBackground: Color,
    /** Card / list-item container. Maps to M3 surfaceContainer. */
    val cardBackground: Color,
    /** Dialog / bottom-sheet container. Maps to M3 surfaceContainerHigh. */
    val dialogBackground: Color,

    // ── Text ──────────────────────────────────────────────────────────────
    /** Primary headline / title text. Maps to M3 onSurface. */
    val titleText: Color,
    /** Secondary / supporting body text. Maps to M3 onSurfaceVariant. */
    val summaryText: Color,
    /** Category header labels. Maps to M3 primary. */
    val categoryText: Color,

    // ── Icons ─────────────────────────────────────────────────────────────
    /** Accent icon tint (enabled state). Maps to M3 primary. */
    val primaryIcon: Color,
    /** Muted / secondary icon tint. Maps to M3 onSurfaceVariant. */
    val secondaryIcon: Color,
    /** Destructive action icon tint. Maps to M3 error. */
    val errorIcon: Color,

    // ── Chrome ────────────────────────────────────────────────────────────
    /** Divider / separator lines. Maps to M3 outlineVariant. */
    val divider: Color,
)

private val LocalSettingsColorScheme = staticCompositionLocalOf<SettingsColorScheme> {
    error("No SettingsColorScheme provided — wrap your content in XiaomiPartsTheme")
}

/**
 * Provides the [SettingsColorScheme] derived from the current [MaterialTheme]
 * colour scheme. Must be called inside a [XiaomiPartsTheme] / [MaterialTheme].
 */
@Composable
fun ProvideSettingsColorScheme(content: @Composable () -> Unit) {
    val m3 = MaterialTheme.colorScheme
    val scheme = SettingsColorScheme(
        screenBackground = m3.surface,
        cardBackground   = m3.surfaceContainer,
        dialogBackground = m3.surfaceContainerHigh,
        titleText        = m3.onSurface,
        summaryText      = m3.onSurfaceVariant,
        categoryText     = m3.primary,
        primaryIcon      = m3.primary,
        secondaryIcon    = m3.onSurfaceVariant,
        errorIcon        = m3.error,
        divider          = m3.outlineVariant,
    )
    CompositionLocalProvider(LocalSettingsColorScheme provides scheme, content = content)
}

/**
 * Access point for the current [SettingsColorScheme].
 *
 * Usage: `SettingsTheme.colorScheme.cardBackground`
 */
object SettingsTheme {
    val colorScheme: SettingsColorScheme
        @Composable get() = LocalSettingsColorScheme.current
}
