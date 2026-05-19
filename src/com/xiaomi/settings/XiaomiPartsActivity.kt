/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * XiaomiPartsActivity — single entry point injected into Android Settings.
 *
 * Theme: Material 3 Expressive
 *   • dynamicColorScheme() for Material You wallpaper-based colour on API 31+
 *   • expressiveShapes() for the squircle/rounded shape set from M3 Expressive
 *   • expressiveMotion() spring-based animation spec
 *   • Large‑rounded top‑level screen shapes matching Android 16 Settings look
 *
 * Navigation: single-activity Compose NavHost (no Fragments).
 */

package com.xiaomi.settings

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xiaomi.settings.display.DisplayColoursScreen
import com.xiaomi.settings.thermal.ThermalManagementScreen
import com.xiaomi.settings.touchsampling.TouchBoostScreen

/** Typed route constants used throughout the NavHost. */
object Routes {
    const val HOME            = "xiaomi_parts_home"
    const val DISPLAY_COLOURS = "display_colours"
    const val THERMAL         = "thermal_management"
    const val TOUCH_BOOST     = "touch_boost"
}

class XiaomiPartsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XiaomiPartsExpressiveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background,
                ) {
                    XiaomiPartsNavGraph()
                }
            }
        }
    }
}

/**
 * Material 3 Expressive theme wrapper.
 *
 * Uses [MaterialExpressiveTheme] which applies:
 *   - Expressive shape tokens (squircle corners, larger radii)
 *   - Expressive motion tokens (spring-based, physically natural)
 *   - Dynamic colour on API 31+ (Material You)
 *   - Baseline M3 palette fallback on older API
 */
@Composable
fun XiaomiPartsExpressiveTheme(content: @Composable () -> Unit) {
    val context   = LocalContext.current
    val darkTheme = isSystemInDarkTheme()

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme  -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme  -> darkColorScheme()
        else       -> lightColorScheme()
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        content     = content,
    )
}

@Composable
private fun XiaomiPartsNavGraph() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            XiaomiPartsHomeScreen(
                onNavigateToDisplay = { nav.navigate(Routes.DISPLAY_COLOURS) },
                onNavigateToThermal = { nav.navigate(Routes.THERMAL) },
                onNavigateToTouch   = { nav.navigate(Routes.TOUCH_BOOST) },
            )
        }

        composable(Routes.DISPLAY_COLOURS) {
            DisplayColoursScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.THERMAL) {
            ThermalManagementScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.TOUCH_BOOST) {
            TouchBoostScreen(onBack = { nav.popBackStack() })
        }
    }
}
