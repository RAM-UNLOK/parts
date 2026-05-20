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
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    // ── Charging state ────────────────────────────────────────────────────
    // Initial value from sticky broadcast (synchronous, no allocation).
    var isCharging by remember {
        val sticky  = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = sticky?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        mutableStateOf(plugged != 0)
    }
    // Live updates — dual-path: POWER_CONNECTED/DISCONNECTED for immediate
    // response + BATTERY_CHANGED for USB connections that may not fire the
    // POWER_CONNECTED action on some Xiaomi kernels.
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_POWER_CONNECTED    -> isCharging = true
                    Intent.ACTION_POWER_DISCONNECTED -> isCharging = false
                    Intent.ACTION_BATTERY_CHANGED    -> {
                        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                        isCharging  = plugged != 0
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    // ── Thermal-enabled state ─────────────────────────────────────────────
    // Observed via SharedPreferences so toggling in ThermalManagementScreen
    // propagates back here immediately without a recompose trigger.
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        // surfaceContainer matches Theme.Settings.Home.Expressive colorBackground.
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text  = stringResource(R.string.xiaomi_parts_title),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
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
            // Charging banner — only shown when thermal is active AND charger
            // is connected. Using both conditions avoids the misleading state
            // where the banner claims a charging profile is active but the
            // ThermalService is not running.
            AnimatedVisibility(
                visible = isCharging && thermalEnabled,
                enter   = expandVertically(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)),
                exit    = shrinkVertically(spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium)),
            ) {
                ChargingBanner(
                    Modifier.padding(
                        horizontal = PartsTokens.contentPaddingHorizontal,
                        vertical   = PartsTokens.bannerPaddingVertical,
                    )
                )
            }

            // ── Display section ───────────────────────────────────────────
            PartsCategory(stringResource(R.string.xiaomi_parts_category_display))
            PartsCard {
                PartsRow(
                    icon    = Icons.Filled.Palette,
                    title   = stringResource(R.string.display_title),
                    summary = stringResource(R.string.display_summary),
                    onClick = onNavigateToDisplay,
                )
            }

            Spacer(Modifier.height(PartsTokens.cardBlockSpacing))

            // ── Performance section ───────────────────────────────────────
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
                // Divider between Thermal and Touch rows — matches Pixel
                // Settings which separates rows inside a card with a 0.5dp
                // outlineVariant line inset by the content horizontal margin.
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

            Spacer(Modifier.height(PartsTokens.cardBlockSpacing))

            // ── Diagnostics section ───────────────────────────────────────
            PartsCategory(stringResource(R.string.xiaomi_parts_category_diagnostics))
            PartsCard {
                PartsRow(
                    icon    = Icons.Filled.Science,
                    title   = stringResource(R.string.cit_title),
                    summary = stringResource(R.string.cit_summary),
                    onClick = {
                        if (!CitLauncher.launch(context))
                            Toast.makeText(context, R.string.cit_not_found, Toast.LENGTH_SHORT).show()
                    },
                )
            }

            Spacer(Modifier.height(PartsTokens.listBottomPadding))
        }
    }
}

@Composable
fun PartsCategory(label: String, modifier: Modifier = Modifier) {
    Text(
        text     = label,
        style    = MaterialTheme.typography.labelLarge,
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
            modifier         = Modifier
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
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                        .copy(alpha = PartsTokens.containerSubtitleAlpha),
                )
            }
        }
    }
}
