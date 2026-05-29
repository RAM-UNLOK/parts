/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.touchsampling

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.R
import com.xiaomi.settings.utils.PartsToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchBoostScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(TouchSamplingService.isEnabled(context)) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    fun toggle(newValue: Boolean) {
        runCatching {
            TouchSamplingService.setEnabled(context, newValue)
            enabled = newValue
        }.onFailure {
            PartsToast.show(context, R.string.touch_boost_failed)
        }
    }

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text     = stringResource(R.string.touch_boost_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_up),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Toggle card — surfaceContainerHigh lifts above background so the
            // wallpaper-derived Monet tonal difference is visible.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = MaterialTheme.shapes.large,
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                ListItem(
                    modifier = Modifier.clickable { toggle(!enabled) },
                    headlineContent = {
                        Text(
                            text  = stringResource(R.string.touch_boost_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    supportingContent = {
                        Text(
                            text  = stringResource(R.string.touch_boost_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked         = enabled,
                            onCheckedChange = { toggle(it) },
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Unspecified),
                )
            }

            // Info callout — tertiaryContainer is the Monet accent-complement zone.
            // In dark mode it produces a clearly tinted warm/cool surface that makes
            // the wallpaper palette immediately legible, unlike secondaryContainer
            // which tends to land near-neutral in dark Monet themes.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = MaterialTheme.shapes.large,
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
            ) {
                Row(
                    modifier              = Modifier.padding(20.dp),
                    verticalAlignment     = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Info,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier           = Modifier.padding(top = 2.dp),
                    )
                    Text(
                        text  = stringResource(R.string.touch_boost_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }
    }
}
