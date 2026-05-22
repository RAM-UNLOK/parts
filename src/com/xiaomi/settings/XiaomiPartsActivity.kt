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
                NavHost(
                    navController   = nav,
                    startDestination = "home",
                    enterTransition  = {
                        slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMediumLow,
                            ),
                            initialOffsetX = { it / 4 },
                        ) + fadeIn(tween(220))
                    },
                    exitTransition  = {
                        slideOutHorizontally(
                            animationSpec = tween(200),
                            targetOffsetX = { -it / 6 },
                        ) + fadeOut(tween(200))
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMediumLow,
                            ),
                            initialOffsetX = { -it / 4 },
                        ) + fadeIn(tween(220))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            animationSpec = tween(200),
                            targetOffsetX = { it / 6 },
                        ) + fadeOut(tween(200))
                    },
                ) {
                    composable("home")            { XiaomiPartsHomeScreen(nav) }
                    composable("displayColours")  { DisplayColoursScreen { nav.popBackStack() } }
                    composable("thermal")         { ThermalManagementScreen { nav.popBackStack() } }
                    composable("touchBoost")      { TouchBoostScreen { nav.popBackStack() } }
                }
            }
        }
    }
}
