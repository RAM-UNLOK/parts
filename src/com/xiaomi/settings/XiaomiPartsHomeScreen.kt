/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * Xiaomi Parts home screen.
 *
 * M3 design tokens used:
 *  Colour    — background, surfaceContainerHigh, surfaceContainerLow,
 *               secondaryContainer, onSecondaryContainer, tertiaryContainer,
 *               onTertiaryContainer, primary, onSurface, onSurfaceVariant
 *  Typography — headlineMedium (collapsed bar title), bodyLarge (row title),
 *               bodySmall (row summary), labelMedium (section label)
 *  Shape     — extraLarge (cards), full circle (icon containers)
 *  Motion    — spring() for banner enter/exit, exitUntilCollapsed TopAppBar
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
fun XiaomiPartsHomeScreen(
    onNavigateToDisplay : () -> Unit,
    onNavigateToThermal : () -> Unit,
    onNavigateToTouch   : () -> Unit,
) {
    val context = LocalContext.current

    // Charging state — initial value from sticky broadcast, live updates via receiver.
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text  = stringResource(R.string.xiaomi_parts_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor      = MaterialTheme.colorScheme.onSurface,
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

            // Charging banner — spring enter/exit animation
            AnimatedVisibility(
                visible = isCharging,
                enter   = expandVertically(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)),
                exit    = shrinkVertically(spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium)),
            ) {
                ChargingBanner(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            // Display section
            PartsCategory(stringResource(R.string.xiaomi_parts_category_display))
            PartsCard {
                PartsRow(
                    icon    = Icons.Filled.Palette,
                    title   = stringResource(R.string.display_title),
                    summary = stringResource(R.string.display_summary),
                    onClick = onNavigateToDisplay,
                )
            }

            // Performance section
            PartsCategory(stringResource(R.string.xiaomi_parts_category_performance))
            PartsCard {
                PartsRow(
                    icon    = Icons.Filled.Thermostat,
                    title   = stringResource(R.string.thermal_title),
                    summary = stringResource(R.string.thermal_summary),
                    onClick = onNavigateToThermal,
                )
                PartsDivider()
                PartsRow(
                    icon    = Icons.Filled.TouchApp,
                    title   = stringResource(R.string.htsr_title),
                    summary = stringResource(R.string.htsr_summary),
                    onClick = onNavigateToTouch,
                )
            }

            // Diagnostics section
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
// Shared M3 components (used across all sub-screens via import)
// ─────────────────────────────────────────────────────────────────────────────

/** M3 section label — labelMedium, primary colour, 20dp top padding. */
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
 * M3 card container — surfaceContainerHigh tonal surface, extraLarge shape.
 * Groups related rows inside a visually distinct rounded card.
 */
@Composable
fun PartsCard(
    modifier : Modifier = Modifier,
    content  : @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape          = MaterialTheme.shapes.extraLarge,
        color          = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
        content = { Column(content = content) },
    )
}

/** M3 inset divider — indented past icon + spacing to align with text. */
@Composable
fun PartsDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier  = modifier.padding(start = 72.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.outlineVariant,
    )
}

/**
 * M3 settings row.
 *
 * Layout: [icon container 40dp] [title + summary, weight=1] [chevron]
 * Colors: secondaryContainer icon bg, bodyLarge title, bodySmall summary,
 *         onSurfaceVariant chevron.
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
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // M3 icon container — secondaryContainer tonal circle
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
        if (trailing != null) trailing()
        else Icon(
            imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(20.dp),
        )
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

// Keep legacy aliases so sub-screens compiled before this refactor still resolve.
@Composable fun SettingsCategoryLabel(label: String, modifier: Modifier = Modifier) = PartsCategory(label, modifier)
@Composable fun SettingsBlock(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) = PartsCard(modifier, content)
@Composable fun SettingsDivider(modifier: Modifier = Modifier) = PartsDivider(modifier)
