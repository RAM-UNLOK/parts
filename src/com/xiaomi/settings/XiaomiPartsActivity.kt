/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xiaomi.settings.display.DisplayColoursScreen
import com.xiaomi.settings.thermal.ThermalManagementScreen
import com.xiaomi.settings.touchsampling.TouchBoostScreen
import com.xiaomi.settings.ui.XiaomiPartsTheme

class XiaomiPartsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            XiaomiPartsTheme {
                val navController = rememberNavController()
                val navFade = tween<Float>(200)

                NavHost(
                    navController       = navController,
                    startDestination    = "home",
                    enterTransition     = { fadeIn(animationSpec  = navFade) },
                    exitTransition      = { fadeOut(animationSpec = navFade) },
                    popEnterTransition  = { fadeIn(animationSpec  = navFade) },
                    popExitTransition   = { fadeOut(animationSpec = navFade) },
                ) {
                    composable("home") {
                        XiaomiPartsHomeScreen(
                            onNavigateToDisplay = { navController.navigate("display") },
                            onNavigateToThermal = { navController.navigate("thermal") },
                            onNavigateToTouch   = { navController.navigate("touch") },
                        )
                    }
                    composable("display") {
                        DisplayColoursScreen(onBack = { navController.popBackStack() })
                    }
                    composable("thermal") {
                        ThermalManagementScreen(onBack = { navController.popBackStack() })
                    }
                    composable("touch") {
                        TouchBoostScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
