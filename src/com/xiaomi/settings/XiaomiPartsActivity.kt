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

// ─────────────────────────────────────────────────────────────────────────────
// M3 Expressive navigation motion constants
//
// Spec: https://m3.material.io/styles/motion/transitions/transition-patterns
//
// Key rules:
//   • Enter/PopEnter: short spatial offset (±NAV_SLIDE_DP) + fade-in.
//     The offset gives spatial context (the screen arrives FROM somewhere)
//     without a slow full-width theatrical sweep.
//   • Exit/PopExit: fade-out ONLY. No slide on the leaving screen.
//     Sliding out while a new screen slides in creates a visual collision
//     that looks cluttered and slow.
//   • Spring stiffness: Medium (800) for nav — snappy settle, no overshoot.
//     MediumLow (400) is correct for scroll snap / in-screen elements but
//     too slow for a full-screen route change.
//   • DampingRatioNoBouncy throughout: navigation must never bounce.
// ─────────────────────────────────────────────────────────────────────────────

/** Pixel offset for enter/pop-enter slide. 30dp in density-independent terms. */
private const val NAV_SLIDE_DP = 30

/**
 * Fade duration (ms) for exit/pop-exit transitions.
 * Kept shorter than enter so the leaving screen clears quickly and the
 * arriving screen has full visual focus.
 */
private const val NAV_FADE_EXIT_MS = 150

class XiaomiPartsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        setContent {
            XiaomiPartsTheme {
                val navController = rememberNavController()
                val density = resources.displayMetrics.density
                val slideOffsetPx = (NAV_SLIDE_DP * density).toInt()

                NavHost(
                    navController    = navController,
                    startDestination = "home",

                    // ── ENTER (navigate forward) ──────────────────────────
                    // New screen slides in from the right (+slideOffsetPx),
                    // short offset so it's spatial not theatrical.
                    enterTransition = {
                        slideInHorizontally(
                            animationSpec  = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMedium,
                            ),
                            initialOffsetX = { slideOffsetPx },
                        ) + fadeIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMedium,
                            ),
                        )
                    },

                    // ── EXIT (navigate forward — old screen leaves) ───────
                    // Old screen fades out only. No slide.
                    // Sliding out while a new screen slides in = collision.
                    exitTransition = {
                        fadeOut(animationSpec = tween(NAV_FADE_EXIT_MS))
                    },

                    // ── POP ENTER (back — returning screen re-enters) ─────
                    // Returning screen slides in from the LEFT (negative offset),
                    // confirming the user is going back spatially.
                    popEnterTransition = {
                        slideInHorizontally(
                            animationSpec  = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMedium,
                            ),
                            initialOffsetX = { -slideOffsetPx },
                        ) + fadeIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMedium,
                            ),
                        )
                    },

                    // ── POP EXIT (back — current screen leaves) ───────────
                    // Current screen fades out only. No slide.
                    popExitTransition = {
                        fadeOut(animationSpec = tween(NAV_FADE_EXIT_MS))
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
