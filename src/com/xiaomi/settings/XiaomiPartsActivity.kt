/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
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

                // Plain crossfade transitions for in-process NavHost navigation.
                //
                // WHY NOT scaleIn/scaleOut:
                //   Scale + fade simultaneously causes a brightness flash.
                //   The outgoing screen's fadeOut (fast) completes before the
                //   incoming screen is opaque, briefly exposing the raw window
                //   background. Scale transitions belong at the Activity window
                //   level (WindowManager / predictive-back), not inside a
                //   NavHost composable.
                //
                // WHY LinearEasing for fades:
                //   EaseIn or EaseOut on an opacity-only transition produces a
                //   perceptual brightness dip at the midpoint — the eye reads
                //   this as a flash. LinearEasing keeps brightness even
                //   throughout the crossfade, matching how Android's own
                //   ActivityOptions.makeSceneTransitionAnimation works.
                //
                // enter 200ms / exit 150ms: asymmetric timing so the outgoing
                // screen exits slightly faster than the incoming one arrives,
                // matching Material 3 motion guidance (exit is secondary).
                NavHost(
                    navController      = navController,
                    startDestination   = "home",
                    enterTransition    = { fadeIn(tween(200, easing = LinearEasing)) },
                    exitTransition     = { fadeOut(tween(150, easing = LinearEasing)) },
                    popEnterTransition = { fadeIn(tween(200, easing = LinearEasing)) },
                    popExitTransition  = { fadeOut(tween(150, easing = LinearEasing)) },
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
