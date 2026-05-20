/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material 3 Expressive typography scale.
 *
 * Matches the Android 16 / Pixel 10 Settings Expressive spec:
 *   - Display sizes use slightly larger line heights for dramatic headings.
 *   - Body sizes match the 16sp / 1.5 line-height reading comfort spec.
 *   - Label sizes use tracked uppercase for UI chrome (chips, tabs, buttons).
 *
 * On Android 16 the system provides a full Expressive type ramp via
 * `Theme.Material3.DynamicColors.DayNight`; we replicate the key tokens
 * here so the ramp is consistent across Android 12–16.
 *
 * Note: [FontFamily.Default] resolves to the device's system font
 * (Roboto Flex on Pixel / AOSP, or the OEM font on partner devices).
 * We intentionally do not hardcode Roboto here so the app respects
 * OEM font customisations — matching Settings behaviour.
 */
private val ExpressiveTypography = Typography(
    // ── Display ─────────────────────────────────────────
    displayLarge  = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall  = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    // ── Headline ─────────────────────────────────────────
    headlineLarge  = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall  = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    // ── Title ────────────────────────────────────────────
    titleLarge  = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall  = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // ── Body ─────────────────────────────────────────────
    bodyLarge  = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall  = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    // ── Label ────────────────────────────────────────────
    labelLarge  = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall  = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

/**
 * App-wide Material 3 theme wrapper.
 *
 * Colour scheme:
 *   On Android 12+ (S): dynamic colour — wallpaper-derived Monet / Material
 *   You tonal palette, identical to Pixel Settings behaviour.
 *   On older builds: M3 baseline scheme as fallback.
 *
 * Typography:
 *   [ExpressiveTypography] is passed explicitly so the Compose tree uses
 *   the M3 Expressive type ramp on ALL Android versions (12+). Without the
 *   explicit pass Compose falls back to the M3 default Typography object
 *   which lacks the Expressive line-height / letter-spacing adjustments.
 *
 * Note: The redundant Box(surfaceContainer) wrapper that previously lived
 * here has been removed. Scaffold already paints its own containerColor;
 * double-painting wastes a draw pass and causes a subtle flicker on first
 * composition before the Scaffold measures itself.
 */
@Composable
fun XiaomiPartsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content  : @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else           dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else      -> lightColorScheme()
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = ExpressiveTypography,
        content     = content,
    )
}
