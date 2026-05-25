/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.touchsampling

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.PartsCard
import com.xiaomi.settings.R
import com.xiaomi.settings.utils.PartsToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchBoostScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var enabled by remember { mutableStateOf(TouchSamplingService.isEnabled(context)) }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val iconContainer by animateColorAsState(
        targetValue   = if (enabled) MaterialTheme.colorScheme.primaryContainer
                        else         MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label         = "touchIconContainer",
    )
    val iconContent by animateColorAsState(
        targetValue   = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer
                        else         MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label         = "touchIconContent",
    )

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MediumTopAppBar(
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item(key = "toggle") {
                PartsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(40.dp)
                                .clip(MaterialTheme.shapes.large)
                                .background(iconContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector        = ImageVector.vectorResource(R.drawable.ic_touch_boost),
                                contentDescription = null,
                                tint               = iconContent,
                                modifier           = Modifier.size(24.dp),
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = stringResource(R.string.touch_boost_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text  = stringResource(R.string.touch_boost_summary),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked         = enabled,
                            onCheckedChange = { checked ->
                                runCatching {
                                    TouchSamplingService.setEnabled(context, checked)
                                    enabled = checked
                                }.onFailure {
                                    PartsToast.show(context, R.string.touch_boost_failed)
                                }
                            },
                        )
                    }
                }
            }

            item(key = "info") {
                Text(
                    text     = stringResource(R.string.touch_boost_description),
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                )
            }

            item(key = "spacer") { Spacer(Modifier.height(32.dp)) }
        }
    }
}
