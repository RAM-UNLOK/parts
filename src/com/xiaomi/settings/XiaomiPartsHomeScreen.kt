/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.utils.CitLauncher
import com.xiaomi.settings.utils.PartsToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XiaomiPartsHomeScreen(
    onNavigateToDisplay: () -> Unit,
    onNavigateToThermal: () -> Unit,
    onNavigateToTouch:   () -> Unit,
) {
    val context        = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface, // Lighter dark-grey background
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
                    containerColor         = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top    = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 32.dp,
            ),
        ) {
            item(key = "display-label") { PartsCategory(stringResource(R.string.display_category)) }
            item(key = "display-card") {
                PartsCard {
                    PartsListItem(
                        icon    = ImageVector.vectorResource(R.drawable.ic_display_colours),
                        title   = stringResource(R.string.display_colours_title),
                        summary = stringResource(R.string.display_colours_summary),
                        onClick = onNavigateToDisplay,
                    )
                }
            }

            item(key = "perf-label") { PartsCategory(stringResource(R.string.performance_category)) }
            item(key = "perf-card") {
                PartsCard {
                    Column {
                        PartsListItem(
                            icon    = ImageVector.vectorResource(R.drawable.ic_thermal_settings),
                            title   = stringResource(R.string.thermal_title),
                            summary = stringResource(R.string.thermal_summary),
                            onClick = onNavigateToThermal,
                        )
                        PartsDivider()
                        PartsListItem(
                            icon    = ImageVector.vectorResource(R.drawable.ic_touch_boost),
                            title   = stringResource(R.string.touch_boost_title),
                            summary = stringResource(R.string.touch_boost_summary),
                            onClick = onNavigateToTouch,
                        )
                    }
                }
            }

            item(key = "diag-label") { PartsCategory(stringResource(R.string.xiaomi_parts_category_diagnostics)) }
            item(key = "diag-card") {
                PartsCard {
                    Column {
                        PartsListItem(
                            icon    = Icons.Filled.Fingerprint,
                            title   = stringResource(R.string.fingerprint_calibration_title),
                            summary = stringResource(R.string.fingerprint_calibration_summary),
                            onClick = {
                                if (!CitLauncher.launchFingerprintCalibration(context)) {
                                    PartsToast.show(context, R.string.fingerprint_calibration_not_found)
                                }
                            },
                        )
                        PartsDivider()
                        PartsListItem(
                            icon    = Icons.Filled.Speaker,
                            title   = stringResource(R.string.speaker_calibration_title),
                            summary = stringResource(R.string.speaker_calibration_summary),
                            onClick = {
                                if (!CitLauncher.launchSpeakerCalibration(context)) {
                                    PartsToast.show(context, R.string.speaker_calibration_not_found)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PartsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape  = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, // Distinct, lighter card color
        ),
        content = { content() }
    )
}

@Composable
fun PartsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

@Composable
fun PartsCategory(label: String) {
    Text(
        text     = label,
        style    = MaterialTheme.typography.titleSmall,
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun PartsListItem(
    icon:    ImageVector,
    title:   String,
    summary: String,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Text(
                text  = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        supportingContent = {
            Text(
                text  = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = {
            // Removed the circular background to match the clean AOSP sub-menu look
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(24.dp).padding(top = 2.dp), // Slight padding to align with text
            )
        },
        trailingContent = {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(16.dp),
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}