/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * Material Design 3 theme for Xiaomi Parts.
 *
 * Colour  — dynamicColorScheme() on API 31+ (Material You / wallpaper-derived)
 *           Baseline M3 teal/purple palette on older API
 * Typography — full M3 type scale (Display → Label) via default M3 fonts
 * Shape     — standard M3 shape scale (None → ExtraLarge)
 * Motion    — spring physics via androidx.compose.animation.core.spring()
 *
 * All stable M3 APIs — zero @OptIn / experimental surface.
 */

package com.xiaomi.settings.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Root M3 theme wrapper for all Xiaomi Parts screens.
 *
 * Features:
 *  - Dynamic colour (Material You) on Android 12+ via wallpaper-extracted palette
 *  - Baseline M3 dark/light schemes as fallback
 *  - Full M3 typography scale (unchanged from M3 defaults — clean sans-serif)
 *  - Full M3 shape scale (rounded corners from Extra Small → Extra Large)
 */
@Composable
fun XiaomiPartsTheme(content: @Composable () -> Unit) {
    val context   = LocalContext.current
    val darkTheme = isSystemInDarkTheme()

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme  -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme  -> darkColorScheme()
        else       -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        // Typography & Shapes: M3 defaults are correct — no custom override needed.
        // The full scale (Display/Headline/Title/Body/Label × Large/Medium/Small)
        // and shape tokens (ExtraSmall → ExtraLarge) are already defined by M3.
        content = content,
    )
}
