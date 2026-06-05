/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.ui

import android.os.Build
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// ──────────────────────────────────────────────────────────────────────────
// Theme entry point
// ──────────────────────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme()
private val DarkColorScheme  = darkColorScheme()

@Composable
fun XiaomiPartsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content:   @Composable () -> Unit,
) {
    val context      = LocalContext.current
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        dynamicColor -> dynamicLightColorScheme(context)
        else         -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography(),
        shapes = Shapes(
            extraSmall = RoundedCornerShape(4.dp),
            small      = RoundedCornerShape(8.dp),
            medium     = RoundedCornerShape(12.dp),
            large      = RoundedCornerShape(16.dp),
            extraLarge = RoundedCornerShape(28.dp),
        ),
        content = content,
    )
}

// ──────────────────────────────────────────────────────────────────────────
// Motion engine — M3 Expressive spring tokens
// ──────────────────────────────────────────────────────────────────────────

object Motion {
    private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    fun <T> navSpatialSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness    = Spring.StiffnessMediumLow,
    )

    fun <T> navEffectsSpec(): FiniteAnimationSpec<T> =
        tween(durationMillis = 150, easing = EmphasizedDecelerate)

    fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessMedium,
    )

    fun <T> shimmerSpec(): DurationBasedAnimationSpec<T> =
        tween(durationMillis = 2800, easing = LinearEasing)

    fun checkFadeInSpec(): FiniteAnimationSpec<Float> =
        tween(durationMillis = 150, easing = EmphasizedDecelerate)

    fun checkFadeOutSpec(): FiniteAnimationSpec<Float> =
        tween(durationMillis = 100, easing = EmphasizedAccelerate)
}

// ──────────────────────────────────────────────────────────────────────────
// Shared shapes
// ──────────────────────────────────────────────────────────────────────────

val bottomSheetTopShape: Shape
    @Composable @ReadOnlyComposable get() = MaterialTheme.shapes.extraLarge.copy(
        bottomStart = CornerSize(0.dp),
        bottomEnd   = CornerSize(0.dp),
    )

// ──────────────────────────────────────────────────────────────────────────
// Constants
// ──────────────────────────────────────────────────────────────────────────

const val toastDebounceMs:    Long  = 2_000L
const val shimmerAlpha:       Float = 0.05f
const val blobRadiusFraction: Float = 0.38f
