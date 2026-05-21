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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            XiaomiPartsTheme {
                val navController = rememberNavController()

                // ── M3 Expressive route transitions ───────────────────────────
                //
                // Pattern: slideInHorizontally + fadeIn (enter/popEnter)
                //          slideOutHorizontally + fadeOut (exit/popExit)
                //
                // Direction:
                //   Forward  (enter)  — slide in from +X (right edge → centre)
                //   Forward  (exit)   — slide out to   -X (centre → left edge)
                //   Back     (pop enter) — slide in from -X (left edge → centre)
                //   Back     (pop exit)  — slide out to  +X (centre → right edge)
                //
                // EaseOutCubic: decelerating curve — element moves fast at the
                // start and slows to rest, matching M3 "Emphasized Decelerate"
                // easing for entering elements.
                //
                // Duration: PartsTokens.MotionDurationRoute (350 ms enter,
                // 250 ms exit) — asymmetric so the outgoing screen exits fast
                // and the incoming screen arrives deliberately.
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
                        DisplayColoursScreen()
                    }
                    composable("thermal") {
                        ThermalManagementScreen()
                    }
                    composable("touch") {
                        TouchBoostScreen()
                    }
                }
            }
        }
    }
}
