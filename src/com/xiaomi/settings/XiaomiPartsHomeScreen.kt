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
    // broadcast arrives. Only register for CONNECTED / DISCONNECTED edge events
    // — dropping ACTION_BATTERY_CHANGED from the live receiver prevents the
    // double-fire that produced multiple charging toasts on a single plug event.
    var isCharging by remember {
        val sticky  = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = sticky?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        mutableStateOf(plugged != 0)
    }

    // Debounce: suppress any second edge event arriving within 2 s (e.g. a
    // charger that briefly disconnects during negotiation).
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
        // Only CONNECTED + DISCONNECTED — no ACTION_BATTERY_CHANGED.
        // BATTERY_CHANGED fires repeatedly (every % change) and causes
        // duplicate state updates + duplicate toasts.
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
    // exitUntilCollapsed: correct for the home screen LargeTopAppBar so the
    // title collapses as the user scrolls into the content list.
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec  = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness    = Spring.StiffnessHigh,
        ),
        flingAnimationSpec = exponentialDecay(frictionMultiplier = 2f),
    )

    // ── Section entrance animation states ─────────────────────────────────────
    val visDisplay     = remember { MutableTransitionState(false).apply { targetState = true } }
    val visPerformance = remember { MutableTransitionState(false).apply { targetState = true } }
    val visDiagnostics = remember { MutableTransitionState(false).apply { targetState = true } }

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        // surface: cards use surfaceContainerLow giving them visible lift against
        // the page background — matches Pixel/AOSP dynamic-colour Settings look.
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
            // Shown only when thermal is enabled AND device is charging.
            // spring enter/exit: banner slides in/out smoothly without a hard
            // pop — matches M3 Expressive motion guidance for contextual cards.
            AnimatedVisibility(
                visible = isCharging && thermalEnabled,
                enter   = expandVertically(
                    spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)
                ) + fadeIn(tween(220)),
                exit    = shrinkVertically(
                    spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium)
                ) + fadeOut(tween(160)),
            ) {
                ChargingBanner(
                    Modifier.padding(
                        horizontal = PartsTokens.contentPaddingHorizontal,
                        vertical   = PartsTokens.bannerPaddingVertical,
                    )
                )
            }

            // ── Display section ───────────────────────────────────────────────
            AnimatedVisibility(
                visibleState = visDisplay,
                enter = fadeIn(tween(220)) +
                        slideInVertically(
                            animationSpec  = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMediumLow,
                            ),
                            initialOffsetY = { it / 5 },
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

            // ── Performance section ───────────────────────────────────────────
            AnimatedVisibility(
                visibleState = visPerformance,
                enter = fadeIn(tween(220, delayMillis = 60)) +
                        slideInVertically(
                            animationSpec  = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMediumLow,
                            ),
                            initialOffsetY = { it / 5 },
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

            // ── Diagnostics section ───────────────────────────────────────────
            AnimatedVisibility(
                visibleState = visDiagnostics,
                enter = fadeIn(tween(220, delayMillis = 120)) +
                        slideInVertically(
                            animationSpec  = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness    = Spring.StiffnessMediumLow,
                            ),
                            initialOffsetY = { it / 5 },
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
// Shared composables — used by HomeScreen and sub-screens
// ─────────────────────────────────────────────────────────────────────────────

/**
 * M3 Expressive category header.
 *
 * Uses [MaterialTheme.typography.titleSmall] (was labelLarge) to match the
 * M3 Expressive spec for list-section headers. Primary colour so the header
 * reads as a structural landmark without competing with content.
 */
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

/**
 * M3 card container for settings rows.
 *
 * Backed by [Surface] with [MaterialTheme.colorScheme.surfaceContainerLow] so
 * the card has a single consistent background colour sourced from the dynamic
 * Monet palette. The shape uses [PartsTokens.cardShape] (28 dp corner radius —
 * M3 Expressive extra-large shape).
 */
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

/**
 * M3 Expressive settings row.
 *
 * Leading icon sits in a 40 dp tonal container (secondaryContainer fill,
 * onSecondaryContainer icon tint) — the correct M3 Expressive tonal icon
 * treatment. Trailing chevron uses onSurfaceVariant to stay visually quiet.
 */
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
        // Tonal icon container — M3 Expressive 40 dp circle with secondaryContainer
        // background. Replaces the earlier manual Box(CircleShape)+clip pattern
        // with a semantically equivalent but token-correct version.
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
    // tertiaryContainer / onTertiaryContainer: the correct M3 semantic for
    // battery / charging context (warm accent, distinct from primary/secondary).
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
