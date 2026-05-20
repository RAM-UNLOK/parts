/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.thermal.ThermalUtils
import com.xiaomi.settings.ui.PartsTokens
import com.xiaomi.settings.utils.CitLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XiaomiPartsHomeScreen(
    onNavigateToDisplay: () -> Unit,
    onNavigateToThermal: () -> Unit,
    onNavigateToTouch:   () -> Unit,
) {
    val context      = LocalContext.current
    val thermalUtils = remember { ThermalUtils.getInstance(context) }

    // ── Charging state ────────────────────────────────────────────────────────
    // Seed from sticky ACTION_BATTERY_CHANGED so the UI is correct before any
    // broadcast arrives. Only CONNECTED / DISCONNECTED edge events registered
    // to prevent duplicate toasts from repeated BATTERY_CHANGED fires.
    var isCharging by remember {
        val sticky  = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = sticky?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        mutableStateOf(plugged != 0)
    }
    var lastChargingEventMs by remember { mutableLongStateOf(0L) }
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val now = SystemClock.elapsedRealtime()
                if (now - lastChargingEventMs < 2_000L) return
                lastChargingEventMs = now
                when (intent.action) {
                    Intent.ACTION_POWER_CONNECTED    -> isCharging = true
                    Intent.ACTION_POWER_DISCONNECTED -> isCharging = false
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        @Suppress("UnspecifiedRegisterReceiverFlag")
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    // ── Thermal-enabled state ─────────────────────────────────────────────────
    var thermalEnabled by remember { mutableStateOf(thermalUtils.enabled) }
    DisposableEffect(Unit) {
        val prefListener = android.content.SharedPreferences
            .OnSharedPreferenceChangeListener { _, key ->
                if (key == "thermal_enabled") thermalEnabled = thermalUtils.enabled
            }
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        prefs.registerOnSharedPreferenceChangeListener(prefListener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(prefListener) }
    }

    // ── Scroll behaviour ──────────────────────────────────────────────────────
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec  = spring(
            dampingRatio = PartsTokens.MotionDampingRatio,
            stiffness    = Spring.StiffnessHigh,
        ),
        flingAnimationSpec = exponentialDecay(frictionMultiplier = 2f),
    )

    // ── Section entrance animation states ─────────────────────────────────────
    // Each section gets its own MutableTransitionState so they can be
    // staggered independently. Delay is baked into the tween animationSpec
    // on BOTH the slide and the fade — previously only the fade was delayed,
    // causing a visible pop-then-fade artefact.
    val visDisplay     = remember { MutableTransitionState(false).apply { targetState = true } }
    val visPerformance = remember { MutableTransitionState(false).apply { targetState = true } }
    val visDiagnostics = remember { MutableTransitionState(false).apply { targetState = true } }

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LargeTopAppBar(
                title  = { Text(text = stringResource(R.string.xiaomi_parts_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Charging banner ───────────────────────────────────────────────
            // spring enter/exit so the banner slides in with a physical feel
            // rather than a hard linear pop.
            AnimatedVisibility(
                visible = isCharging && thermalEnabled,
                enter   = expandVertically(
                    animationSpec = spring(
                        dampingRatio = PartsTokens.MotionDampingRatio,
                        stiffness    = PartsTokens.MotionStiffnessMediumLow,
                    ),
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = PartsTokens.MotionDurationEnter,
                        easing         = EaseOutCubic,
                    ),
                ),
                exit    = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = PartsTokens.MotionDampingRatio,
                        stiffness    = PartsTokens.MotionStiffnessMedium,
                    ),
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = PartsTokens.MotionDurationExit,
                        easing         = EaseOutCubic,
                    ),
                ),
            ) {
                ChargingBanner(
                    Modifier.padding(
                        horizontal = PartsTokens.contentPaddingHorizontal,
                        vertical   = PartsTokens.bannerPaddingVertical,
                    )
                )
            }

            // ── Display section  (stagger group 0 — no delay) ─────────────────
            // slideInVertically + fadeIn share the SAME tween duration + delay
            // so both start simultaneously. Slide uses EaseOutCubic (decelerates
            // into rest) — the M3 Expressive "Emphasized Decelerate" curve.
            AnimatedVisibility(
                visibleState = visDisplay,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = PartsTokens.MotionDurationEnter,
                        delayMillis    = 0,
                        easing         = EaseOutCubic,
                    ),
                ) + slideInVertically(
                    animationSpec  = tween(
                        durationMillis = PartsTokens.MotionDurationSlide,
                        delayMillis    = 0,
                        easing         = EaseOutCubic,
                    ),
                    initialOffsetY = { it / PartsTokens.MotionSlideDistance },
                ),
            ) {
                Column {
                    PartsCategory(stringResource(R.string.xiaomi_parts_category_display))
                    PartsCard {
                        PartsRow(
                            icon    = Icons.Filled.Palette,
                            title   = stringResource(R.string.display_title),
                            summary = stringResource(R.string.display_summary),
                            onClick = onNavigateToDisplay,
                        )
                    }
                }
            }

            Spacer(Modifier.height(PartsTokens.cardBlockSpacing))

            // ── Performance section  (stagger group 1 — 1× step delay) ────────
            AnimatedVisibility(
                visibleState = visPerformance,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = PartsTokens.MotionDurationEnter,
                        delayMillis    = PartsTokens.MotionStaggerStep,
                        easing         = EaseOutCubic,
                    ),
                ) + slideInVertically(
                    animationSpec  = tween(
                        durationMillis = PartsTokens.MotionDurationSlide,
                        delayMillis    = PartsTokens.MotionStaggerStep,
                        easing         = EaseOutCubic,
                    ),
                    initialOffsetY = { it / PartsTokens.MotionSlideDistance },
                ),
            ) {
                Column {
                    PartsCategory(stringResource(R.string.xiaomi_parts_category_performance))
                    PartsCard {
                        PartsRow(
                            icon    = Icons.Filled.Thermostat,
                            title   = stringResource(R.string.thermal_title),
                            summary = stringResource(
                                if (thermalEnabled) R.string.thermal_summary_active
                                else               R.string.thermal_summary_disabled
                            ),
                            onClick = onNavigateToThermal,
                        )
                        HorizontalDivider(
                            modifier  = Modifier.padding(horizontal = PartsTokens.contentPaddingHorizontal),
                            thickness = 0.5.dp,
                            color     = MaterialTheme.colorScheme.outlineVariant,
                        )
                        PartsRow(
                            icon    = Icons.Filled.TouchApp,
                            title   = stringResource(R.string.htsr_title),
                            summary = stringResource(R.string.htsr_summary),
                            onClick = onNavigateToTouch,
                        )
                    }
                }
            }

            Spacer(Modifier.height(PartsTokens.cardBlockSpacing))

            // ── Diagnostics section  (stagger group 2 — 2× step delay) ────────
            AnimatedVisibility(
                visibleState = visDiagnostics,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = PartsTokens.MotionDurationEnter,
                        delayMillis    = PartsTokens.MotionStaggerStep * 2,
                        easing         = EaseOutCubic,
                    ),
                ) + slideInVertically(
                    animationSpec  = tween(
                        durationMillis = PartsTokens.MotionDurationSlide,
                        delayMillis    = PartsTokens.MotionStaggerStep * 2,
                        easing         = EaseOutCubic,
                    ),
                    initialOffsetY = { it / PartsTokens.MotionSlideDistance },
                ),
            ) {
                Column {
                    PartsCategory(stringResource(R.string.xiaomi_parts_category_diagnostics))
                    PartsCard {
                        PartsRow(
                            icon    = Icons.Filled.Science,
                            title   = stringResource(R.string.cit_title),
                            summary = stringResource(R.string.cit_summary),
                            onClick = {
                                if (!CitLauncher.launch(context))
                                    android.widget.Toast.makeText(
                                        context,
                                        R.string.cit_not_found,
                                        android.widget.Toast.LENGTH_SHORT,
                                    ).show()
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(PartsTokens.listBottomPadding))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PartsCategory(label: String, modifier: Modifier = Modifier) {
    Text(
        text     = label,
        style    = MaterialTheme.typography.titleSmall,
        color    = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(
            start  = PartsTokens.contentPaddingHorizontal,
            top    = PartsTokens.categoryTopPadding,
            bottom = PartsTokens.categoryBottomPadding,
        ),
    )
}

@Composable
fun PartsCard(
    modifier: Modifier = Modifier,
    content:  @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier       = modifier
            .fillMaxWidth()
            .padding(horizontal = PartsTokens.contentPaddingHorizontal),
        shape          = PartsTokens.cardShape,
        color          = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = PartsTokens.cardElevation,
    ) {
        Column(content = content)
    }
}

@Composable
fun PartsRow(
    icon:     ImageVector,
    title:    String,
    summary:  String,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(
                horizontal = PartsTokens.contentPaddingHorizontal,
                vertical   = PartsTokens.rowPaddingVertical,
            ),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PartsTokens.rowElementSpacing),
    ) {
        Box(
            modifier = Modifier
                .size(PartsTokens.leadingIconContainerSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier           = Modifier.size(PartsTokens.leadingIconSize),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text  = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(PartsTokens.trailingIconSize),
            )
        }
    }
}

@Composable
private fun ChargingBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier       = modifier.fillMaxWidth(),
        shape          = PartsTokens.bannerShape,
        color          = MaterialTheme.colorScheme.tertiaryContainer,
        tonalElevation = PartsTokens.cardElevation,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = PartsTokens.bannerInnerPaddingH,
                vertical   = PartsTokens.bannerInnerPaddingV,
            ),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PartsTokens.bannerIconSpacing),
        ) {
            Icon(
                imageVector        = Icons.Filled.BatteryChargingFull,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier           = Modifier.size(PartsTokens.bannerIconSize),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = stringResource(R.string.thermal_charging_active),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    text  = stringResource(R.string.thermal_charging_active_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}
