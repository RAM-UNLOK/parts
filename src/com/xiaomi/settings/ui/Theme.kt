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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// ──────────────────────────────────────────────────────────────────────────
// Semantic colour tokens
// ──────────────────────────────────────────────────────────────────────────

@Immutable
data class SettingsColorScheme(
    val screenBackground: Color, // surfaceContainerLow
    val cardBackground:   Color, // surfaceContainerHigh
    val dialogBackground: Color, // surfaceContainerHighest
    val titleText:        Color,
    val summaryText:      Color,
    val categoryText:     Color,
    val primaryIcon:      Color,
    val secondaryIcon:    Color,
    val errorIcon:        Color,
    val divider:          Color,
)

private val LocalSettingsColorScheme = staticCompositionLocalOf<SettingsColorScheme> {
    error("Wrap content in XiaomiPartsTheme")
}

object SettingsTheme {
    val colorScheme: SettingsColorScheme
        @Composable get() = LocalSettingsColorScheme.current
}

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
        dynamicColor && darkTheme  -> dynamicDarkColorScheme(context)
        dynamicColor && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme                  -> DarkColorScheme
        else                       -> LightColorScheme
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
    ) {
        val m3 = MaterialTheme.colorScheme
        val scheme = SettingsColorScheme(
            screenBackground = m3.surfaceContainerLow,
            cardBackground   = m3.surfaceContainerHigh,
            dialogBackground = m3.surfaceContainerHighest,
            titleText        = m3.onSurface,
            summaryText      = m3.onSurfaceVariant,
            categoryText     = m3.primary,
            primaryIcon      = m3.primary,
            secondaryIcon    = m3.onSurfaceVariant,
            errorIcon        = m3.error,
            divider          = m3.outlineVariant,
        )
        CompositionLocalProvider(LocalSettingsColorScheme provides scheme, content = content)
    }
}

// ──────────────────────────────────────────────────────────────────────────
// TopAppBar colour helper — fully specifies all 5 slots
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun settingsTopAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor             = SettingsTheme.colorScheme.screenBackground,
    scrolledContainerColor     = SettingsTheme.colorScheme.cardBackground,
    titleContentColor          = SettingsTheme.colorScheme.titleText,
    navigationIconContentColor = SettingsTheme.colorScheme.secondaryIcon,
    actionIconContentColor     = SettingsTheme.colorScheme.secondaryIcon,
)

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
