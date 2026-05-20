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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.utils.CitLauncher

// ─────────────────────────────────────────────────────────────────────────────
// Home screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XiaomiPartsHomeScreen(
    onNavigateToDisplay : () -> Unit,
    onNavigateToThermal : () -> Unit,
    onNavigateToTouch   : () -> Unit,
) {
    val context = LocalContext.current

    var isCharging by remember {
        val sticky  = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = sticky?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        mutableStateOf(plugged != 0)
    }
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                isCharging = plugged != 0
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text  = stringResource(R.string.xiaomi_parts_title),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
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
            AnimatedVisibility(
                visible = isCharging,
                enter   = expandVertically(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)),
                exit    = shrinkVertically(spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium)),
            ) {
                ChargingBanner(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            PartsCategory(stringResource(R.string.xiaomi_parts_category_display))

            PartsCard {
                PartsRow(
                    icon    = Icons.Filled.Palette,
                    title   = stringResource(R.string.display_title),
                    summary = stringResource(R.string.display_summary),
                    onClick = onNavigateToDisplay,
                )
            }

            Spacer(Modifier.height(8.dp))

            PartsCategory(stringResource(R.string.xiaomi_parts_category_performance))

            PartsCard {
                PartsRow(
                    icon    = Icons.Filled.Thermostat,
                    title   = stringResource(R.string.thermal_title),
                    summary = stringResource(R.string.thermal_summary),
                    onClick = onNavigateToThermal,
                )
                PartsRow(
                    icon    = Icons.Filled.TouchApp,
                    title   = stringResource(R.string.htsr_title),
                    summary = stringResource(R.string.htsr_summary),
                    onClick = onNavigateToTouch,
                )
            }

            Spacer(Modifier.height(8.dp))

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

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared M3 components  (Clover Settings design language)
// ─────────────────────────────────────────────────────────────────────────────

/** Section label — primary colour, label-medium, left-padded. */
@Composable
fun PartsCategory(label: String, modifier: Modifier = Modifier) {
    Text(
        text     = label,
        style    = MaterialTheme.typography.labelMedium,
        color    = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp),
    )
}

/**
 * Tonal card block — surfaceContainer fill, extraLarge corners, no stroke.
 * Matches Clover ContextualCardStyle.
 */
@Composable
fun PartsCard(
    modifier : Modifier = Modifier,
    content  : @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier       = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape          = MaterialTheme.shapes.extraLarge,
        color          = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
    ) {
        Column(content = content)
    }
}

/**
 * Single preference row inside a PartsCard.
 */
@Composable
fun PartsRow(
    icon     : ImageVector,
    title    : String,
    summary  : String,
    onClick  : () -> Unit,
    modifier : Modifier = Modifier,
    trailing : @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier         = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier           = Modifier.size(22.dp),
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
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(20.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Charging banner
// ─────────────────────────────────────────────────────────────────────────────

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
