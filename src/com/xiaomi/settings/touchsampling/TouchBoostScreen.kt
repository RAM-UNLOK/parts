/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.touchsampling

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Spring
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.xiaomi.settings.PartsCard
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessMediumLow,
        )
    )

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text  = stringResource(R.string.htsr_title),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                        modifier         = Modifier
                            .size(PartsTokens.leadingIconContainerSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.TouchApp,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier           = Modifier.size(PartsTokens.leadingIconSize),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = stringResource(R.string.htsr_enable_title),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text  = stringResource(R.string.htsr_enable_summary),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked         = enabled,
                        onCheckedChange = { toggleHtsr(context, prefs, it) { enabled = it } },
                    )
                }
            }

            Spacer(Modifier.height(PartsTokens.cardBlockSpacing))

            // Info banner — 28dp pill, surfaceContainerHigh = neutral info surface
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PartsTokens.contentPaddingHorizontal),
                shape          = PartsTokens.bannerShape,
                color          = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = PartsTokens.cardElevation,
            ) {
                Row(
                    modifier              = Modifier.padding(
                        horizontal = PartsTokens.bannerInnerPaddingH,
                        vertical   = PartsTokens.bannerInnerPaddingV,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(PartsTokens.bannerIconSpacing),
                    verticalAlignment     = Alignment.Top,
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Info,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(PartsTokens.leadingIconSize),
                    )
                    Text(
                        text  = stringResource(R.string.htsr_info_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
        onResult(target)
        val msg = if (target) R.string.htsr_enabled_toast else R.string.htsr_disabled_toast
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(context, R.string.htsr_failed_toast, Toast.LENGTH_SHORT).show()
    }
}
