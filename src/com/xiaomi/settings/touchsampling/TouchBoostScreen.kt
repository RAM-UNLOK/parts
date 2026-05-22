/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.touchsampling

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.exponentialDecay
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.xiaomi.settings.PartsCard
import com.xiaomi.settings.R
import com.xiaomi.settings.ui.PartsTokens
import com.xiaomi.settings.utils.PartsToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchBoostScreen(onBack: () -> Unit) {
    val context      = LocalContext.current
    val leadingShape = PartsTokens.leadingIconShape
    val effectsSpec  = PartsTokens.MotionDefaultEffects
    val spatialSpec  = PartsTokens.MotionDefaultSpatial

    var enabled by remember { mutableStateOf(TouchSamplingService.isEnabled(context)) }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec  = spatialSpec,
        flingAnimationSpec = exponentialDecay(frictionMultiplier = PartsTokens.frictionMultiplier),
    )

    val iconContainer by animateColorAsState(
        targetValue   = if (enabled) PartsTokens.Colors.touchIconContainer
                        else         MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = effectsSpec,
        label         = "touchIconContainer",
    )
    val iconContent by animateColorAsState(
        targetValue   = if (enabled) PartsTokens.Colors.touchIconContent
                        else         MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = effectsSpec,
        label         = "touchIconContent",
    )

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = PartsTokens.Colors.page,
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text     = stringResource(R.string.touch_boost_title),
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
            item(key = "toggle") {
                PartsCard(
                    modifier = Modifier.padding(top = PartsTokens.cardBlockSpacing),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = PartsTokens.contentPaddingHorizontal,
                                vertical   = PartsTokens.rowPaddingVertical,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(PartsTokens.leadingIconContainerSize)
                                .clip(leadingShape)
                                .background(iconContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector        = ImageVector.vectorResource(R.drawable.ic_touch_boost),
                                contentDescription = null,
                                tint               = iconContent,
                                modifier           = Modifier.size(PartsTokens.leadingIconSize),
                            )
                        }
                        Spacer(Modifier.size(PartsTokens.rowElementSpacing))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = stringResource(R.string.touch_boost_title),
                                style = PartsTokens.Type.rowHeadline,
                                color = PartsTokens.Colors.textPrimary,
                            )
                            Text(
                                text  = stringResource(R.string.touch_boost_summary),
                                style = PartsTokens.Type.rowSupporting,
                                color = PartsTokens.Colors.textSecondary,
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
                    style    = PartsTokens.Type.infoBody,
                    color    = PartsTokens.Colors.textSecondary,
                    modifier = Modifier.padding(
                        horizontal = PartsTokens.contentPaddingHorizontal,
                        vertical   = PartsTokens.rowPaddingVertical,
                    ),
                )
            }

            item(key = "spacer") { Spacer(Modifier.height(PartsTokens.listBottomPadding)) }
        }
    }
}
