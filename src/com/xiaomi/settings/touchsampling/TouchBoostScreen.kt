/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.touchsampling

import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.Lightbulb
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
import com.xiaomi.settings.PartsCategory
import com.xiaomi.settings.R
import com.xiaomi.settings.ui.PartsTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchBoostScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs   = remember {
        context.getSharedPreferences(TouchSamplingService.SHAREDHTSR, Context.MODE_PRIVATE)
    }
    var enabled by remember {
        mutableStateOf(prefs.getBoolean(TouchSamplingService.HTSR_STATE, false))
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec  = PartsTokens.MotionSpringEnter,
        flingAnimationSpec = exponentialDecay(frictionMultiplier = 2f),
    )

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = PartsTokens.Colors.page,
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text     = stringResource(R.string.htsr_title),
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
            PartsCategory(stringResource(R.string.htsr_category))
            PartsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Switch) {
                            toggleHtsr(context, prefs, !enabled) { enabled = it }
                        }
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
                            .clip(CircleShape)
                            .background(PartsTokens.Colors.iconContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Vibration,
                            contentDescription = null,
                            tint               = PartsTokens.Colors.iconContent,
                            modifier           = Modifier.size(PartsTokens.leadingIconSize),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = stringResource(R.string.htsr_enable_title),
                            style = MaterialTheme.typography.bodyLarge,
                            color = PartsTokens.Colors.textPrimary,
                        )
                        Text(
                            text  = stringResource(R.string.htsr_enable_summary),
                            style = MaterialTheme.typography.bodyMedium,
                            color = PartsTokens.Colors.textSecondary,
                        )
                    }
                    Switch(
                        checked         = enabled,
                        onCheckedChange = { toggleHtsr(context, prefs, it) { enabled = it } },
                    )
                }
            }

            Spacer(Modifier.height(PartsTokens.cardBlockSpacing))

            // Info banner — uses bannerContainer/bannerContent (tertiaryContainer).
            // Consistent with the charging banners in HomeScreen and ThermalManagementScreen.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PartsTokens.contentPaddingHorizontal)
                    .clip(PartsTokens.cardShape)
                    .background(PartsTokens.Colors.bannerContainer)
                    .padding(
                        horizontal = PartsTokens.contentPaddingHorizontal,
                        vertical   = PartsTokens.appRowPaddingVertical,
                    ),
                horizontalArrangement = Arrangement.spacedBy(PartsTokens.rowElementSpacing),
                verticalAlignment     = Alignment.Top,
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint               = PartsTokens.Colors.bannerContent,
                    modifier           = Modifier
                        .padding(top = PartsTokens.bannerIconTopOffset)
                        .size(PartsTokens.leadingIconSize),
                )
                Text(
                    text  = stringResource(R.string.htsr_info_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = PartsTokens.Colors.bannerContent,
                )
            }

            Spacer(Modifier.height(PartsTokens.listBottomPadding))
        }
    }
}

private fun toggleHtsr(
    context:  Context,
    prefs:    android.content.SharedPreferences,
    target:   Boolean,
    onResult: (Boolean) -> Unit,
) {
    runCatching {
        prefs.edit().putBoolean(TouchSamplingService.HTSR_STATE, target).apply()
        val serviceIntent = Intent(context, TouchSamplingService::class.java)
        if (target) {
            context.startServiceAsUser(serviceIntent, UserHandle.CURRENT)
        } else {
            context.stopServiceAsUser(serviceIntent, UserHandle.CURRENT)
        }
        onResult(target)
        val msg = if (target) R.string.htsr_enabled_toast else R.string.htsr_disabled_toast
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(context, R.string.htsr_failed_toast, Toast.LENGTH_SHORT).show()
    }
}
