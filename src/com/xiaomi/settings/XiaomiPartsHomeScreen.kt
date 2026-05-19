/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * Home screen — Xiaomi Parts hub.
 *
 * Charging detection:
 *   Initial state from sticky ACTION_BATTERY_CHANGED (EXTRA_PLUGGED default=0).
 *   Live updates via dynamically registered BroadcastReceiver.
 *   ACTION_POWER_CONNECTED does NOT work on Android 8+ for background components.
 *
 * Charging toast is shown by ThermalService (works whether UI is open or not).
 * XiaomiPartsHomeScreen only drives the animated ChargingBanner UI state.
 */

package com.xiaomi.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.utils.CitLauncher

@Composable
fun XiaomiPartsHomeScreen(
    onNavigateToDisplay : () -> Unit,
    onNavigateToThermal : () -> Unit,
    onNavigateToTouch   : () -> Unit,
) {
    val context = LocalContext.current

    // Read initial charging state from sticky broadcast.
    var isCharging by remember {
        val sticky = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = sticky?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        mutableStateOf(plugged != 0)
    }

    // Live charging state for the animated banner.
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                    isCharging = plugged != 0
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessMediumLow,
        )
    )

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text  = stringResource(R.string.xiaomi_parts_title),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding),
        ) {
            AnimatedVisibility(
                visible = isCharging,
                enter   = expandVertically(spring(Spring.DampingRatioMediumBouncy)),
                exit    = shrinkVertically(spring(Spring.StiffnessMedium)),
            ) {
                ChargingBanner(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            SettingsCategoryLabel(stringResource(R.string.xiaomi_parts_category_display))
            SettingsBlock {
                SettingsRow(
                    icon    = Icons.Filled.Palette,
                    title   = stringResource(R.string.display_title),
                    summary = stringResource(R.string.display_summary),
                    onClick = onNavigateToDisplay,
                )
            }

            SettingsCategoryLabel(stringResource(R.string.xiaomi_parts_category_performance))
            SettingsBlock {
                SettingsRow(
                    icon    = Icons.Filled.Thermostat,
                    title   = stringResource(R.string.thermal_title),
                    summary = stringResource(R.string.thermal_summary),
                    onClick = onNavigateToThermal,
                )
                SettingsDivider()
                SettingsRow(
                    icon    = Icons.Filled.TouchApp,
                    title   = stringResource(R.string.htsr_title),
                    summary = stringResource(R.string.htsr_summary),
                    onClick = onNavigateToTouch,
                )
            }

            SettingsCategoryLabel(stringResource(R.string.xiaomi_parts_category_diagnostics))
            SettingsBlock {
                SettingsRow(
                    icon    = Icons.Filled.Science,
                    title   = stringResource(R.string.cit_title),
                    summary = stringResource(R.string.cit_summary),
                    onClick = {
                        val launched = CitLauncher.launch(context)
                        if (!launched)
                            Toast.makeText(context, R.string.cit_not_found, Toast.LENGTH_SHORT).show()
                    },
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsCategoryLabel(label: String, modifier: Modifier = Modifier) {
    Text(
        text     = label,
        style    = MaterialTheme.typography.labelMedium,
        color    = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp),
    )
}

@Composable
fun SettingsBlock(
    modifier : Modifier = Modifier,
    content  : @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape  = MaterialTheme.shapes.extraLarge,
        color  = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(start = 68.dp, end = 0.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
fun SettingsRow(
    icon    : ImageVector,
    title   : String,
    summary : String,
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier           = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
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
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ChargingBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier       = modifier.fillMaxWidth(),
        shape          = MaterialTheme.shapes.extraLarge,
        color          = MaterialTheme.colorScheme.tertiaryContainer,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector        = Icons.Filled.BatteryChargingFull,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier           = Modifier.size(24.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = stringResource(R.string.thermal_charging_active),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    text  = stringResource(R.string.thermal_charging_active_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}
