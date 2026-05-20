/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
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
 * App-wide Material 3 theme wrapper.
 *
 * On Android 12+ (S) dynamic colour is used so the app adopts the
 * wallpaper-derived tonal palette — identical to Pixel Settings behaviour.
 * On older builds the default M3 baseline scheme is used as fallback.
 *
 * Note: The redundant Box(surfaceContainer) wrapper that previously lived
 * here has been removed. Scaffold already paints its own containerColor;
 * double-painting wastes a draw pass and can cause a subtle flicker on
 * first composition before the Scaffold measures itself.
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
    MaterialTheme(colorScheme = colorScheme, content = content)
}
