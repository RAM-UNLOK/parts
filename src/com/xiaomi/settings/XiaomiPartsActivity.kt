/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * XiaomiPartsActivity — single-Activity entry point injected into Settings.
 *
 * Theme    : Material Design 3 with dynamic colour (Material You on API 31+)
 * Nav      : Compose NavHost — no Fragments
 * Edge-to-edge: enabled via enableEdgeToEdge()
 */

package com.xiaomi.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xiaomi.settings.display.DisplayColoursScreen
import com.xiaomi.settings.thermal.ThermalManagementScreen
import com.xiaomi.settings.touchsampling.TouchBoostScreen
import com.xiaomi.settings.ui.XiaomiPartsTheme

/** Top-level nav route constants. */
object Routes {
    const val HOME            = "home"
    const val DISPLAY_COLOURS = "display_colours"
    const val THERMAL         = "thermal"
    const val TOUCH_BOOST     = "touch_boost"
}

class XiaomiPartsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XiaomiPartsTheme {
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
