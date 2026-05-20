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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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

                // M3 Expressive screen transitions matching Pixel 10 Settings:
                //   Enter  — fade + gentle scale-up from 92 % (300 ms, EaseOut)
                //   Exit   — fade + gentle scale-down to  92 % (220 ms, EaseIn)
                //   Pop-Enter  — fade + scale-up from 92 % (300 ms)
                //   Pop-Exit   — fade + scale-down to 92 % (220 ms)
                // Asymmetric timing (enter slower than exit) matches M3 motion
                // spec: the incoming screen is the hero and gets more time;
                // the outgoing screen exits quickly so it doesn't compete.
                NavHost(
                    navController    = navController,
                    startDestination = "home",
                    enterTransition  = {
                        fadeIn(tween(300)) +
                        scaleIn(tween(300), initialScale = 0.92f)
                    },
                    exitTransition   = {
                        fadeOut(tween(220)) +
                        scaleOut(tween(220), targetScale = 0.92f)
                    },
                    popEnterTransition = {
                        fadeIn(tween(300)) +
                        scaleIn(tween(300), initialScale = 0.96f)
                    },
                    popExitTransition  = {
                        fadeOut(tween(200)) +
                        scaleOut(tween(200), targetScale = 1.04f)
                    },
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
