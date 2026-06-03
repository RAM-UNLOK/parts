/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.display

import android.os.UserHandle
import android.provider.Settings
import android.view.Display
import android.view.WindowManagerGlobal
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.R
import com.xiaomi.settings.ui.Motion
import com.xiaomi.settings.utils.PartsToast
import com.xiaomi.settings.utils.dlog

private const val TAG        = "ScreenResolutionScreen"
private const val PREF_KEY   = "custom_screen_resolution_key"
private const val NATIVE_KEY = "res_1220p"

private data class ResolutionOption(
    val key:        String,
    val labelRes:   Int,
    val summaryRes: Int,
    val width:      Int,
    val height:     Int,
    val density:    Int,
    val isNative:   Boolean = false,
)

/**
 * Resolutions for POCO X7 Pro (native panel: 1220x2712 @ 446 PPI).
 * Non-native densities: round((width / 1220.0) * 446).
 */
private val RESOLUTIONS = listOf(
    ResolutionOption("res_1440p", R.string.screen_resolution_1440p, R.string.screen_resolution_1440p_summary, 1440, 3200, 526),
    ResolutionOption("res_1220p", R.string.screen_resolution_1220p, R.string.screen_resolution_1220p_summary, 1220, 2712, 446, isNative = true),
    ResolutionOption("res_1080p", R.string.screen_resolution_1080p, R.string.screen_resolution_1080p_summary, 1080, 2400, 394),
    ResolutionOption("res_720p",  R.string.screen_resolution_720p,  R.string.screen_resolution_720p_summary,   720, 1600, 263),
)

private fun applyResolution(option: ResolutionOption) {
    val wm = WindowManagerGlobal.getWindowManagerService()
    if (option.isNative) {
        wm.clearForcedDisplaySize(Display.DEFAULT_DISPLAY)
        wm.clearForcedDisplayDensityForUser(Display.DEFAULT_DISPLAY, UserHandle.USER_CURRENT)
    } else {
        wm.setForcedDisplaySize(Display.DEFAULT_DISPLAY, option.width, option.height)
        wm.setForcedDisplayDensityForUser(Display.DEFAULT_DISPLAY, option.density, UserHandle.USER_CURRENT)
    }
}

@Composable
private fun ResolutionRowCard(
    option:     ResolutionOption,
    isSelected: Boolean,
    shape:      Shape,
    modifier:   Modifier = Modifier,
    onClick:    () -> Unit,
) {
    Card(
        onClick  = onClick,
        modifier = modifier.fillMaxWidth(),
        shape    = shape,
        colors   = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text  = stringResource(option.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            supportingContent = {
                Text(
                    text  = stringResource(option.summaryRes),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            leadingContent = {
                RadioButton(
                    selected = isSelected,
                    onClick  = null,
                )
            },
            trailingContent = {
                AnimatedContent(
                    targetState  = isSelected,
                    transitionSpec = {
                        fadeIn(Motion.checkFadeInSpec()) togetherWith fadeOut(Motion.checkFadeOutSpec())
                    },
                    label = "check_${option.key}",
                ) { selected ->
                    if (selected) {
                        Icon(
                            imageVector        = Icons.Filled.Check,
                            contentDescription = null,
                            modifier           = Modifier.size(24.dp),
                        )
                    } else {
                        Box(Modifier.size(24.dp))
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenResolutionScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var selectedKey by remember {
        mutableStateOf(
            Settings.System.getString(context.contentResolver, PREF_KEY) ?: NATIVE_KEY
        )
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val shapeOuter = MaterialTheme.shapes.extraLarge
    val shapeInner = MaterialTheme.shapes.extraSmall

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text     = stringResource(R.string.screen_resolution_title),
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
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top    = innerPadding.calculateTopPadding() + 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 32.dp,
                ),
        ) {
            Text(
                text     = stringResource(R.string.screen_resolution_description),
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
            )

            RESOLUTIONS.forEachIndexed { index, option ->
                val isFirst   = index == 0
                val isLast    = index == RESOLUTIONS.lastIndex
                val bottomPad = if (isLast) 0.dp else 2.dp

                val shape = shapeOuter.copy(
                    topStart    = if (isFirst) shapeOuter.topStart    else shapeInner.topStart,
                    topEnd      = if (isFirst) shapeOuter.topEnd      else shapeInner.topEnd,
                    bottomStart = if (isLast)  shapeOuter.bottomStart else shapeInner.bottomStart,
                    bottomEnd   = if (isLast)  shapeOuter.bottomEnd   else shapeInner.bottomEnd,
                )

                ResolutionRowCard(
                    option     = option,
                    isSelected = selectedKey == option.key,
                    shape      = shape,
                    modifier   = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = bottomPad),
                    onClick = {
                        runCatching {
                            applyResolution(option)
                            Settings.System.putString(context.contentResolver, PREF_KEY, option.key)
                            selectedKey = option.key
                        }.onFailure { e ->
                            dlog(TAG, "Failed to apply resolution: ${e.message}")
                            PartsToast.show(context, R.string.screen_resolution_failed)
                        }
                    },
                )
            }
        }
    }
}
