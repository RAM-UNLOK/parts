/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
        // WindowCompat.setDecorFitsSystemWindows(false). Called before
        // super.onCreate so window flags apply before first measure/layout.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Clear the XML windowBackground after Compose has taken ownership
        // so only the Scaffold surface colour paints. The ?colorBackground
        // in styles.xml handles the launch frame before this executes.
        window.setBackgroundDrawableResource(android.R.color.transparent)

        setContent {
            XiaomiPartsTheme {
                val navController = rememberNavController()

                NavHost(
                    navController    = navController,
                    startDestination = "home",

                    // M3 Expressive motion: spring physics on all route
                    // transitions. Spring produces physically correct
                    // deceleration without a fixed duration, which is the
                    // M3 Expressive motion spec. Do NOT use tween/EaseOutCubic
                    // for navigational transitions.

                    enterTransition = {
                        slideInHorizontally(
                            animationSpec  = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMediumLow,
                            ),
                            initialOffsetX = { it },
                        ) + fadeIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMedium,
                            ),
                        )
                    },

                    exitTransition = {
                        slideOutHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMedium,
                            ),
                            targetOffsetX = { -it / 3 },
                        ) + fadeOut(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMedium,
                            ),
                        )
                    },

                    popEnterTransition = {
                        slideInHorizontally(
                            animationSpec  = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMediumLow,
                            ),
                            initialOffsetX = { -it / 3 },
                        ) + fadeIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMedium,
                            ),
                        )
                    },

                    popExitTransition = {
                        slideOutHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMedium,
                            ),
                            targetOffsetX = { it },
                        ) + fadeOut(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMedium,
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
