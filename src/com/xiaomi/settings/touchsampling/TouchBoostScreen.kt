/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.touchsampling

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    fun toggle(newValue: Boolean) {
        runCatching {
            TouchSamplingService.setEnabled(context, newValue)
            enabled = newValue
        }.onFailure {
            PartsToast.show(context, R.string.touch_boost_failed)
        }
    }

    // Animate hero card color between primaryContainer (on) and surfaceContainerHigh (off)
    // so the wallpaper-derived Monet palette becomes visible the moment the user toggles.
    val heroContainerColor by animateColorAsState(
        targetValue = if (enabled)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "heroContainerColor",
    )
    val heroContentColor by animateColorAsState(
        targetValue = if (enabled)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurface,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "heroContentColor",
    )

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
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
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Hero toggle card — color animates with the switch state so the
            // Monet primaryContainer token surfaces immediately on enable.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = MaterialTheme.shapes.extraLarge,
                colors   = CardDefaults.cardColors(containerColor = heroContainerColor),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Tonal icon button reflects state with Monet color
                        FilledTonalIconToggleButton(
                            checked   = enabled,
                            onCheckedChange = { toggle(it) },
                            modifier  = Modifier.size(52.dp),
                            colors    = IconToggleButtonDefaults.filledTonalIconToggleButtonColors(
                                checkedContainerColor = MaterialTheme.colorScheme.primary,
                                checkedContentColor   = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.TouchApp,
                                contentDescription = null,
                                modifier           = Modifier.size(26.dp),
                            )
                        }
                        Switch(
                            checked         = enabled,
                            onCheckedChange = { toggle(it) },
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text  = stringResource(R.string.touch_boost_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = heroContentColor,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = stringResource(R.string.touch_boost_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = heroContentColor.copy(alpha = 0.75f),
                    )
                }
            }

            // Info callout — tertiaryContainer is the Monet accent-complement zone.
            // Produces a distinctly tinted surface in dark Monet themes versus the
            // near-neutral secondaryContainer, making the wallpaper palette legible.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = MaterialTheme.shapes.large,
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text  = stringResource(R.string.touch_boost_description),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector        = Icons.Outlined.Info,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor   = Color.Unspecified,
                        headlineColor    = MaterialTheme.colorScheme.onTertiaryContainer,
                        leadingIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ),
                )
            }
        }
    }
}
