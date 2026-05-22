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
import androidx.compose.animation.core.tween
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
import com.xiaomi.settings.ui.XiaomiPartsTheme

class XiaomiPartsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            XiaomiPartsTheme {
                val nav = rememberNavController()

                val pushEnter = slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness    = Spring.StiffnessMediumLow,
                    ),
                    initialOffsetX = { it },
                ) + fadeIn(tween(220))

                val pushExit = slideOutHorizontally(
                    animationSpec = tween(220),
                    targetOffsetX = { -it / 4 },
                ) + fadeOut(tween(200))

                val popEnter = slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness    = Spring.StiffnessMediumLow,
                    ),
                    initialOffsetX = { -it / 4 },
                ) + fadeIn(tween(220))

                val popExit = slideOutHorizontally(
                    animationSpec = tween(220),
                    targetOffsetX = { it },
                ) + fadeOut(tween(200))

                NavHost(
                    navController    = nav,
                    startDestination = "home",
                    enterTransition  = { pushEnter },
                    exitTransition   = { pushExit },
                    popEnterTransition = { popEnter },
                    popExitTransition  = { popExit },
                ) {
                    composable("home") {
                        XiaomiPartsHomeScreen(
                            onNavigateToDisplay = { nav.navigate("displayColours") },
                            onNavigateToThermal = { nav.navigate("thermal") },
                            onNavigateToTouch   = { nav.navigate("touchBoost") },
                        )
                    }
                    composable("displayColours") { DisplayColoursScreen { nav.popBackStack() } }
                    composable("thermal")         { ThermalManagementScreen { nav.popBackStack() } }
                    composable("touchBoost")      { TouchBoostScreen { nav.popBackStack() } }
                }
            }
        }
    }
}
