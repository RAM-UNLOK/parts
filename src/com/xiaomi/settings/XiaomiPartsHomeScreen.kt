/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {

            item(key = "display-label") {
                PartsCategory(
                    label    = stringResource(R.string.display_category),
                    modifier = Modifier.animateItem(),
                )
            }
            item(key = "display-card") {
                Card(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape  = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                ) {
                    PartsListItem(
                        icon               = ImageVector.vectorResource(R.drawable.ic_display_colours),
                        iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconContentColor   = MaterialTheme.colorScheme.onTertiaryContainer,
                        title              = stringResource(R.string.display_colours_title),
                        summary            = stringResource(R.string.display_colours_summary),
                        onClick            = onNavigateToDisplay,
                    )
                }
            }

            item(key = "perf-label") {
                PartsCategory(
                    label    = stringResource(R.string.performance_category),
                    modifier = Modifier.animateItem(),
                )
            }
            item(key = "perf-card") {
                Card(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape  = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                ) {
                    Column {
                        PartsListItem(
                            icon               = ImageVector.vectorResource(R.drawable.ic_thermal_settings),
                            iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                            iconContentColor   = MaterialTheme.colorScheme.onErrorContainer,
                            title              = stringResource(R.string.thermal_title),
                            summary            = stringResource(R.string.thermal_summary),
                            onClick            = onNavigateToThermal,
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color    = MaterialTheme.colorScheme.outlineVariant,
                        )
                        PartsListItem(
                            icon               = ImageVector.vectorResource(R.drawable.ic_touch_boost),
                            iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            iconContentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
                            title              = stringResource(R.string.touch_boost_title),
                            summary            = stringResource(R.string.touch_boost_summary),
                            onClick            = onNavigateToTouch,
                        )
                    }
                }
            }

            item(key = "diag-label") {
                PartsCategory(
                    label    = stringResource(R.string.xiaomi_parts_category_diagnostics),
                    modifier = Modifier.animateItem(),
                )
            }
            item(key = "diag-card") {
                Card(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape  = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                ) {
                    Column {
                        PartsListItem(
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
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color    = MaterialTheme.colorScheme.outlineVariant,
                        )
                        PartsListItem(
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
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PartsCategory(label: String, modifier: Modifier = Modifier) {
    Text(
        text     = label,
        style    = MaterialTheme.typography.titleSmall,
        color    = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 24.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun PartsListItem(
    icon:               ImageVector,
    iconContainerColor: Color,
    iconContentColor:   Color,
    title:              String,
    summary:            String,
    onClick:            () -> Unit,
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
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
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
        },
        trailingContent = {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurface,
                modifier           = Modifier.size(16.dp),
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Unspecified),
    )
}
