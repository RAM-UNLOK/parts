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
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.DividerDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.ui.PartsTokens
import com.xiaomi.settings.utils.CitLauncher

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
        val stickyIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        )
        val initialStatus = stickyIntent?.getIntExtra(
            BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN,
        ) ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        isCharging = initialStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                     initialStatus == BatteryManager.BATTERY_STATUS_FULL
        showChargingBanner = isCharging

        var activeToast: Toast? = null

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val now     = SystemClock.elapsedRealtime()
                val plugged = intent.action == Intent.ACTION_POWER_CONNECTED
                isCharging         = plugged
                showChargingBanner = plugged

                if (now - lastToastTime < TOAST_DEBOUNCE_MS) return
                lastToastTime = now

                activeToast?.cancel()
                val msgRes = if (plugged) R.string.charging_connected
                             else         R.string.charging_disconnected
                val t = Toast.makeText(ctx, msgRes, Toast.LENGTH_SHORT)
                t.show()
                activeToast = t
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        context.registerReceiver(receiver, filter)
        onDispose {
            context.unregisterReceiver(receiver)
            activeToast?.cancel()
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec  = PartsTokens.MotionSpringEnter,
        flingAnimationSpec = exponentialDecay(frictionMultiplier = 2f),
    )

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = PartsTokens.Colors.page,
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
                    containerColor         = PartsTokens.Colors.topBarResting,
                    scrolledContainerColor = PartsTokens.Colors.topBarScrolled,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = PartsTokens.listBottomPadding),
        ) {

            item(key = "charging-banner") {
                AnimatedVisibility(
                    visible = showChargingBanner,
                    enter   = fadeIn(tween(220)) + slideInVertically(
                        animationSpec  = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow),
                        initialOffsetY = { -it / 2 },
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PartsTokens.contentPaddingHorizontal)
                            .padding(top = PartsTokens.bannerTopSpacing)
                            .clip(PartsTokens.bannerShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(
                                horizontal = PartsTokens.contentPaddingHorizontal,
                                vertical   = PartsTokens.rowPaddingVertical,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(PartsTokens.rowElementSpacing),
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.BatteryChargingFull,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier           = Modifier.size(PartsTokens.leadingIconSize),
                        )
                        Text(
                            text     = stringResource(R.string.charging_connected),
                            style    = MaterialTheme.typography.labelLarge,
                            color    = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.weight(1f),
                        )
                        // H-03: use default IconButton (48dp touch target) not size(32dp)
                        IconButton(onClick = { showChargingBanner = false }) {
                            Icon(
                                imageVector        = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.dismiss),
                                tint               = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier           = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            item(key = "display-label") {
                PartsCategory(stringResource(R.string.display_category))
            }
            item(key = "display-card") {
                PartsCard {
                    PartsRow(
                        icon        = ImageVector.vectorResource(R.drawable.ic_display_colours),
                        title       = stringResource(R.string.display_colours_title),
                        summary     = stringResource(R.string.display_colours_summary),
                        onClick     = onNavigateToDisplay,
                        showDivider = false,
                    )
                }
            }

            item(key = "perf-label") {
                PartsCategory(stringResource(R.string.performance_category))
            }
            item(key = "perf-card") {
                PartsCard {
                    PartsRow(
                        icon    = ImageVector.vectorResource(R.drawable.ic_thermal_settings),
                        title   = stringResource(R.string.thermal_title),
                        summary = stringResource(R.string.thermal_summary),
                        onClick = onNavigateToThermal,
                    )
                    PartsRow(
                        icon        = ImageVector.vectorResource(R.drawable.ic_touch_boost),
                        title       = stringResource(R.string.touch_boost_title),
                        summary     = stringResource(R.string.touch_boost_summary),
                        onClick     = onNavigateToTouch,
                        showDivider = false,
                    )
                }
            }

            item(key = "diag-label") {
                PartsCategory(stringResource(R.string.xiaomi_parts_category_diagnostics))
            }
            item(key = "diag-card") {
                PartsCard {
                    PartsRow(
                        icon    = Icons.Filled.Fingerprint,
                        title   = stringResource(R.string.fingerprint_calibration_title),
                        summary = stringResource(R.string.fingerprint_calibration_summary),
                        onClick = {
                            if (!CitLauncher.launchFingerprintCalibration(context)) {
                                Toast.makeText(
                                    context,
                                    R.string.fingerprint_calibration_not_found,
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        },
                    )
                    PartsRow(
                        icon        = Icons.Filled.Speaker,
                        title       = stringResource(R.string.speaker_calibration_title),
                        summary     = stringResource(R.string.speaker_calibration_summary),
                        onClick     = {
                            if (!CitLauncher.launchSpeakerCalibration(context)) {
                                Toast.makeText(
                                    context,
                                    R.string.speaker_calibration_not_found,
                                    Toast.LENGTH_SHORT,
                                ).show()
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
fun PartsCategory(
    title:    String,
    modifier: Modifier = Modifier,
) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelMedium,
        color    = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(
            start  = PartsTokens.contentPaddingHorizontal,
            end    = PartsTokens.contentPaddingHorizontal,
            top    = PartsTokens.categoryTopPadding,
            bottom = PartsTokens.categoryBottomPadding,
        ),
    )
}

@Composable
fun PartsCard(
    modifier: Modifier = Modifier,
    content:  @Composable () -> Unit,
) {
    Surface(
        modifier       = modifier
            .fillMaxWidth()
            .padding(horizontal = PartsTokens.contentPaddingHorizontal),
        shape          = PartsTokens.cardShape,
        color          = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
    ) {
        Column { content() }
    }
}

@Composable
fun PartsRow(
    icon:        ImageVector,
    title:       String,
    summary:     String,
    onClick:     () -> Unit,
    modifier:    Modifier  = Modifier,
    showDivider: Boolean   = true,
    trailing:    @Composable () -> Unit = {
        Icon(
            imageVector        = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(PartsTokens.trailingIconSize),
        )
    },
) {
    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    horizontal = PartsTokens.contentPaddingHorizontal,
                    vertical   = PartsTokens.rowPaddingVertical,
                ),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PartsTokens.rowElementSpacing),
        ) {
            // H-02: squircle shape instead of CircleShape
            Box(
                modifier         = Modifier
                    .size(PartsTokens.leadingIconContainerSize)
                    .clip(PartsTokens.iconContainerShape)
                    .background(PartsTokens.Colors.iconContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = PartsTokens.Colors.iconContent,
                    modifier           = Modifier.size(PartsTokens.leadingIconSize),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PartsTokens.Colors.textPrimary,
                )
                Text(
                    text  = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PartsTokens.Colors.textSecondary,
                )
            }
            trailing()
        }
        if (showDivider) {
            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = PartsTokens.contentPaddingHorizontal),
                thickness = DividerDefaults.Thickness,
                color     = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}
