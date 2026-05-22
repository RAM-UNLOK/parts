/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.touchsampling

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.xiaomi.settings.PartsCard
import com.xiaomi.settings.R
import com.xiaomi.settings.ui.PartsTokens

private fun readHtsr(context: Context): Boolean =
    Settings.System.getIntForUser(
        context.contentResolver,
        "touch_boost_enabled",
        0,
        UserHandle.USER_CURRENT,
    ) != 0

private fun writeHtsr(context: Context, enabled: Boolean) {
    Settings.System.putIntForUser(
        context.contentResolver,
        "touch_boost_enabled",
        if (enabled) 1 else 0,
        UserHandle.USER_CURRENT,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchBoostScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(readHtsr(context)) }

    val iconContainerColor by animateColorAsState(
        targetValue   = if (enabled) PartsTokens.Colors.touchIconContainer
                        else         PartsTokens.Colors.iconContainer,
        animationSpec = PartsTokens.MotionSpringColor,
        label         = "iconContainer",
    )
    val iconColor by animateColorAsState(
        targetValue   = if (enabled) PartsTokens.Colors.touchIconContent
                        else         PartsTokens.Colors.iconContent,
        animationSpec = PartsTokens.MotionSpringColor,
        label         = "iconColor",
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec  = PartsTokens.MotionSpringEnter,
        flingAnimationSpec = exponentialDecay(frictionMultiplier = PartsTokens.frictionMultiplier),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(PartsTokens.cardBlockSpacing))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PartsTokens.contentPaddingHorizontal)
                    .clip(PartsTokens.bannerShape)
                    .background(PartsTokens.Colors.infoBannerContainer)
                    .padding(
                        horizontal = PartsTokens.contentPaddingHorizontal,
                        vertical   = PartsTokens.bannerVerticalPadding,
                    ),
                horizontalArrangement = Arrangement.spacedBy(PartsTokens.rowElementSpacing),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector        = Icons.Filled.Info,
                    contentDescription = null,
                    tint               = PartsTokens.Colors.infoBannerContent,
                    modifier           = Modifier.size(PartsTokens.leadingIconSize),
                )
                Text(
                    text  = stringResource(R.string.touch_boost_info),
                    style = PartsTokens.Type.infoBody,
                    color = PartsTokens.Colors.infoBannerContent,
                )
            }

            Spacer(Modifier.height(PartsTokens.cardBlockSpacing))

            PartsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            role    = Role.Switch,
                            onClick = {
                                val next = !enabled
                                runCatching {
                                    writeHtsr(context, next)
                                    enabled = next
                                }.onFailure {
                                    Toast.makeText(context, R.string.touch_boost_write_failed, Toast.LENGTH_SHORT).show()
                                }
                            },
                        )
                        .padding(
                            horizontal = PartsTokens.contentPaddingHorizontal,
                            vertical   = PartsTokens.rowPaddingVertical,
                        ),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PartsTokens.rowElementSpacing),
                ) {
                    Box(
                        modifier = Modifier
                            .size(PartsTokens.leadingIconContainerSize)
                            .clip(PartsTokens.leadingIconShape)
                            .background(iconContainerColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.TouchApp,
                            contentDescription = null,
                            tint               = iconColor,
                            modifier           = Modifier.size(PartsTokens.leadingIconSize),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = stringResource(R.string.touch_boost_switch_title),
                            style = PartsTokens.Type.rowHeadline,
                            color = PartsTokens.Colors.textPrimary,
                        )
                        Text(
                            text  = stringResource(R.string.touch_boost_switch_summary),
                            style = PartsTokens.Type.rowSupporting,
                            color = PartsTokens.Colors.textSecondary,
                        )
                    }
                    Switch(
                        checked         = enabled,
                        onCheckedChange = null,
                    )
                }
            }

            Spacer(Modifier.height(PartsTokens.listBottomPadding))
        }
    }
}
