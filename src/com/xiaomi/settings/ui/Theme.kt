/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

@Composable
fun XiaomiPartsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content  : @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else      -> lightColorScheme()
    }

    // Material 3 Expressive typography with enhanced weights
    // for better visual hierarchy and Pixel 10 Pro alignment
    val typography = Typography().run {
        copy(
            headlineLarge = headlineLarge.copy(fontWeight = FontWeight.Medium),
            bodyLarge     = bodyLarge.copy(fontWeight = FontWeight.Normal),
            bodyMedium    = bodyMedium.copy(fontWeight = FontWeight.Normal),
            labelLarge    = labelLarge.copy(fontWeight = FontWeight.Medium),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = typography,
    ) {
        // Mirror Theme.Settings.Home.Expressive:
        // colorBackground = materialColorSurfaceContainer.
        // Using surfaceContainer here so the scaffold background
        // matches Clover Settings' expressive home page background
        // (warm tonal, not harsh white/dark).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            content()
        }
    }
}
