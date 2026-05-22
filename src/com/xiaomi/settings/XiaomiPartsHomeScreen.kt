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
import androidx.compose.animation.core.exponentialDecay
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
import com.xiaomi.settings.ui.PartsTokens
import com.xiaomi.settings.utils.CitLauncher
import com.xiaomi.settings.utils.PartsToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XiaomiPartsHomeScreen(
    onNavigateToDisplay: () -> Unit,
    onNavigateToThermal: () -> Unit,
    onNavigateToTouch:   () -> Unit,
) {
    val context = LocalContext.current

    val bannerShape = PartsTokens.bannerShape
    val spatialSpec = PartsTokens.MotionDefaultSpatial
    val effectsSpec = PartsTokens.MotionDefaultEffects

    var isCharging         by remember { mutableStateOf(false) }
    var showChargingBanner by remember { mutableStateOf(false) }
    var lastToastTime      by remember { mutableLongStateOf(0L) }

    DisposableEffect(Unit) {
        val stickyIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val initialStatus = stickyIntent?.getIntExtra(
            BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN,
        ) ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        isCharging         = initialStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                             initialStatus == BatteryManager.BATTERY_STATUS_FULL
        showChargingBanner = isCharging

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val now     = SystemClock.elapsedRealtime()
                val plugged = intent.action == Intent.ACTION_POWER_CONNECTED
                isCharging         = plugged
                showChargingBanner = plugged
                if (now - lastToastTime < PartsTokens.toastDebounceMs) return
                lastToastTime = now
                PartsToast.show(ctx, if (plugged) R.string.charging_connected else R.string.charging_disconnected)
            }
        }
        context.registerReceiver(receiver, IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        })
        onDispose { context.unregisterReceiver(receiver) }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec  = spatialSpec,
        flingAnimationSpec = exponentialDecay(frictionMultiplier = PartsTokens.frictionMultiplier),
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
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = PartsTokens.listBottomPadding),
        ) {
            item(key = "charging-banner") {
                AnimatedVisibility(
                    visible = showChargingBanner,
                    enter   = fadeIn(effectsSpec) +
                              slideInVertically(animationSpec = spatialSpec) { -it / 2 },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PartsTokens.contentPaddingHorizontal)
                            .padding(top = PartsTokens.bannerTopSpacing)
                            .clip(bannerShape)
                            .background(PartsTokens.Colors.chargingBannerContainer)
                            .padding(
                                horizontal = PartsTokens.contentPaddingHorizontal,
                                vertical   = PartsTokens.bannerVerticalPadding,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(PartsTokens.rowElementSpacing),
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.BatteryChargingFull,
                            contentDescription = null,
                            tint               = PartsTokens.Colors.chargingBannerContent,
                            modifier           = Modifier.size(PartsTokens.leadingIconSize),
                        )
                        Text(
                            text     = stringResource(R.string.charging_connected),
                            style    = PartsTokens.Type.bannerLabel,
                            color    = PartsTokens.Colors.chargingBannerContent,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { showChargingBanner = false }) {
                            Icon(
                                imageVector        = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.dismiss),
                                tint               = PartsTokens.Colors.chargingBannerContent,
                                modifier           = Modifier.size(PartsTokens.closeIconSize),
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
                        iconContainerColor = PartsTokens.Colors.displayIconContainer,
                        iconContentColor   = PartsTokens.Colors.displayIconContent,
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
                        iconContainerColor = PartsTokens.Colors.thermalIconContainer,
                        iconContentColor   = PartsTokens.Colors.thermalIconContent,
                        title              = stringResource(R.string.thermal_title),
                        summary            = stringResource(R.string.thermal_summary),
                        onClick            = onNavigateToThermal,
                        showDivider        = true,
                    )
                    PartsRow(
                        icon               = ImageVector.vectorResource(R.drawable.ic_touch_boost),
                        iconContainerColor = PartsTokens.Colors.touchIconContainer,
                        iconContentColor   = PartsTokens.Colors.touchIconContent,
                        title              = stringResource(R.string.touch_boost_title),
                        summary            = stringResource(R.string.touch_boost_summary),
                        onClick            = onNavigateToTouch,
                        showDivider        = false,
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
        style    = PartsTokens.Type.categoryLabel,
        color    = PartsTokens.Colors.categoryLabelColor,
        modifier = Modifier.padding(
            start  = PartsTokens.contentPaddingHorizontal,
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
        modifier  = modifier
            .fillMaxWidth()
            .padding(horizontal = PartsTokens.contentPaddingHorizontal)
            .padding(top = PartsTokens.cardBlockSpacing),
        shape     = PartsTokens.cardShape,
        color     = PartsTokens.Colors.topBarScrolled,
        tonalElevation = PartsTokens.premiumCardElevation,
    ) {
        Column { content() }
    }
}

@Composable
fun PartsRow(
    icon:               ImageVector,
    iconContainerColor: androidx.compose.ui.graphics.Color,
    iconContentColor:   androidx.compose.ui.graphics.Color,
    title:              String,
    summary:            String,
    onClick:            () -> Unit,
    showDivider:        Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
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
                    .clip(PartsTokens.leadingIconShape)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = iconContentColor,
                    modifier           = Modifier.size(PartsTokens.leadingIconSize),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = title,
                    style = PartsTokens.Type.rowHeadline,
                    color = PartsTokens.Colors.textPrimary,
                )
                Text(
                    text  = summary,
                    style = PartsTokens.Type.rowSupporting,
                    color = PartsTokens.Colors.textSecondary,
                )
            }
            Icon(
                imageVector        = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint               = PartsTokens.Colors.textSecondary,
                modifier           = Modifier.size(PartsTokens.trailingIconSize),
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = PartsTokens.contentPaddingHorizontal),
                thickness = PartsTokens.dividerThickness,
                color     = PartsTokens.Colors.outlineVariant,
            )
        }
    }
}
