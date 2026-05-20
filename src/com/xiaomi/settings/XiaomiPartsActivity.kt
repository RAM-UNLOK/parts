/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutCubic
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xiaomi.settings.display.DisplayColoursScreen
import com.xiaomi.settings.thermal.ThermalManagementScreen
import com.xiaomi.settings.touchsampling.TouchBoostScreen
import com.xiaomi.settings.ui.PartsTokens
import com.xiaomi.settings.ui.XiaomiPartsTheme

class XiaomiPartsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // enableEdgeToEdge() sets the status/nav bars transparent via
        // WindowCompat.setDecorFitsSystemWindows(false).  We call it before
        // super.onCreate so the window flags are applied before the first
        // measure/layout pass.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Clear the XML windowBackground (which is ?colorBackground — correct
        // for the launch frame) so that Compose's Scaffold surface colour is
        // the ONLY painter once the Compose tree is ready.  Without this the
        // XML background and the Scaffold colour both paint in sequence,
        // occasionally causing a single-frame colour mismatch on slow devices.
        //
        // android.R.color.transparent is correct here: we are clearing the
        // *window drawable* after Compose has taken ownership, not setting
        // the initial launch colour (that is handled by ?colorBackground in
        // styles.xml which runs before this line executes).
        window.setBackgroundDrawableResource(android.R.color.transparent)

        setContent {
            XiaomiPartsTheme {
                val navController = rememberNavController()

                NavHost(
                    navController    = navController,
                    startDestination = "home",
                    enterTransition  = {
                        slideInHorizontally(
                            animationSpec  = tween(
                                durationMillis = PartsTokens.MotionDurationRoute,
                                easing         = EaseOutCubic,
                            ),
                            initialOffsetX = { it },
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = PartsTokens.MotionDurationEnter,
                                easing         = EaseOutCubic,
                            ),
                        )
                    },
                    exitTransition   = {
                        slideOutHorizontally(
                            animationSpec = tween(
                                durationMillis = PartsTokens.MotionDurationExit,
                                easing         = EaseOutCubic,
                            ),
                            targetOffsetX = { -it / 3 },
                        ) + fadeOut(
                            animationSpec = tween(
                                durationMillis = PartsTokens.MotionDurationExit,
                                easing         = EaseOutCubic,
                            ),
                        )
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            animationSpec  = tween(
                                durationMillis = PartsTokens.MotionDurationRoute,
                                easing         = EaseOutCubic,
                            ),
                            initialOffsetX = { -it / 3 },
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = PartsTokens.MotionDurationEnter,
                                easing         = EaseOutCubic,
                            ),
                        )
                    },
                    popExitTransition  = {
                        slideOutHorizontally(
                            animationSpec = tween(
                                durationMillis = PartsTokens.MotionDurationExit,
                                easing         = EaseOutCubic,
                            ),
                            targetOffsetX = { it },
                        ) + fadeOut(
                            animationSpec = tween(
                                durationMillis = PartsTokens.MotionDurationExit,
                                easing         = EaseOutCubic,
                            ),
                        )
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
