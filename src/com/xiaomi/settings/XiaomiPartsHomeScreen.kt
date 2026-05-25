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
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.utils.CitLauncher
import com.xiaomi.settings.utils.PartsToast

private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private const val TOAST_DEBOUNCE_MS = 2_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XiaomiPartsHomeScreen(
    onNavigateToDisplay: () -> Unit,
    onNavigateToThermal: () -> Unit,
    onNavigateToTouch:   () -> Unit,
) {
    val context = LocalContext.current

    var isCharging         by remember { mutableStateOf(false) }
    var showChargingBanner by remember { mutableStateOf(false) }
    var lastToastTime      by remember { mutableLongStateOf(0L) }

    DisposableEffect(Unit) {
        val stickyIntent   = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val initialPlugged = stickyIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        isCharging         = initialPlugged != 0
        showChargingBanner = isCharging

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val now     = SystemClock.elapsedRealtime()
                val plugged = intent.action == Intent.ACTION_POWER_CONNECTED
                isCharging         = plugged
                showChargingBanner = plugged
                if (now - lastToastTime < TOAST_DEBOUNCE_MS) return
                lastToastTime = now
                PartsToast.show(ctx, if (plugged) R.string.charging_connected else R.string.charging_disconnected)
            }
        }

        @Suppress("UnspecifiedRegisterReceiverFlag")
        context.registerReceiver(receiver, IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        })
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
                        text     = stringResource(R.string.xiaomi_parts_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item(key = "charging-banner") {
                AnimatedVisibility(
                    visible = showChargingBanner,
                    enter   = fadeIn(tween(150, easing = FastOutSlowInEasing)) +
                        slideInVertically(tween(250, easing = EmphasizedDecelerate)) { -it / 2 },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 20.dp)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.BatteryChargingFull,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier           = Modifier.size(24.dp),
                        )
                        Text(
                            text     = stringResource(R.string.charging_connected),
                            style    = MaterialTheme.typography.titleSmall,
                            color    = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { showChargingBanner = false }) {
                            Icon(
                                imageVector        = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.dismiss),
                                tint               = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier           = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            item(key = "display-label") { PartsCategory(stringResource(R.string.display_category)) }
            item(key = "display-card") {
                PartsCard {
                    PartsRow(
                        icon               = ImageVector.vectorResource(R.drawable.ic_display_colours),
                        iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconContentColor   = MaterialTheme.colorScheme.onTertiaryContainer,
                        title              = stringResource(R.string.display_colours_title),
                        summary            = stringResource(R.string.display_colours_summary),
                        onClick            = onNavigateToDisplay,
                        showDivider        = false,
                    )
                }
            }

            item(key = "perf-label") { PartsCategory(stringResource(R.string.performance_category)) }
            item(key = "perf-card") {
                PartsCard {
                    PartsRow(
                        icon               = ImageVector.vectorResource(R.drawable.ic_thermal_settings),
                        iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                        iconContentColor   = MaterialTheme.colorScheme.onErrorContainer,
                        title              = stringResource(R.string.thermal_title),
                        summary            = stringResource(R.string.thermal_summary),
                        onClick            = onNavigateToThermal,
                        showDivider        = true,
                    )
                    PartsRow(
                        icon               = ImageVector.vectorResource(R.drawable.ic_touch_boost),
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconContentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
                        title              = stringResource(R.string.touch_boost_title),
                        summary            = stringResource(R.string.touch_boost_summary),
                        onClick            = onNavigateToTouch,
                        showDivider        = false,
                    )
                }
            }

            item(key = "diag-label") { PartsCategory(stringResource(R.string.xiaomi_parts_category_diagnostics)) }
            item(key = "diag-card") {
                PartsCard {
                    PartsRow(
                        icon               = Icons.Filled.Fingerprint,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconContentColor   = MaterialTheme.colorScheme.onSecondaryContainer,
                        title              = stringResource(R.string.fingerprint_calibration_title),
                        summary            = stringResource(R.string.fingerprint_calibration_summary),
                        onClick = {
                            if (!CitLauncher.launchFingerprintCalibration(context)) {
                                PartsToast.show(context, R.string.fingerprint_calibration_not_found)
                            }
                        },
                        showDivider = true,
                    )
                    PartsRow(
                        icon               = Icons.Filled.Speaker,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconContentColor   = MaterialTheme.colorScheme.onSecondaryContainer,
                        title              = stringResource(R.string.speaker_calibration_title),
                        summary            = stringResource(R.string.speaker_calibration_summary),
                        onClick = {
                            if (!CitLauncher.launchSpeakerCalibration(context)) {
                                PartsToast.show(context, R.string.speaker_calibration_not_found)
                            }
                        },
                        showDivider = false,
                    )
                }
            }
        }
    }
}

@Composable
fun PartsCategory(label: String) {
    Text(
        text     = label,
        style    = MaterialTheme.typography.labelLarge,
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
fun PartsCard(
    modifier: Modifier = Modifier,
    content:  @Composable () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        shape          = MaterialTheme.shapes.extraLarge,
        color          = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 4.dp,
    ) {
        Column { content() }
    }
}

@Composable
fun PartsRow(
    icon:               ImageVector,
    iconContainerColor: Color,
    iconContentColor:   Color,
    title:              String,
    summary:            String,
    onClick:            () -> Unit,
    showDivider:        Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = iconContentColor,
                    modifier           = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text  = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector        = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(24.dp),
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color     = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}
