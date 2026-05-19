/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * Display Colours sub-screen — Material 3.
 *
 * Note: applyColorMode() only writes to Settings.System.
 *   ColorService’s ContentObserver fires automatically and calls
 *   mode.setCurrent() on its own Handler — no direct AIDL call here.
 */

package com.xiaomi.settings.display

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.R
import com.xiaomi.settings.SettingsBlock
import com.xiaomi.settings.SettingsCategoryLabel
import com.xiaomi.settings.SettingsDivider

@Composable
fun DisplayColoursScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var selectedId by remember {
        mutableIntStateOf(
            Settings.System.getIntForUser(
                context.contentResolver,
                Settings.System.DISPLAY_COLOR_MODE,
                ColorService.ColorMode.STANDARD.id,
                UserHandle.USER_CURRENT,
            )
        )
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.display_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .selectableGroup(),
        ) {
            SettingsCategoryLabel(stringResource(R.string.color_section_standard))
            SettingsBlock {
                ColorModeRow(
                    label      = stringResource(R.string.color_mode_vivid),
                    summary    = stringResource(R.string.color_mode_vivid_summary),
                    selected   = selectedId == ColorService.ColorMode.VIVID.id,
                    onSelected = {
                        applyColorMode(context, ColorService.ColorMode.VIVID, it) {
                            selectedId = ColorService.ColorMode.VIVID.id
                        }
                    },
                )
                SettingsDivider()
                ColorModeRow(
                    label      = stringResource(R.string.color_mode_saturated),
                    summary    = stringResource(R.string.color_mode_saturated_summary),
                    selected   = selectedId == ColorService.ColorMode.SATURATED.id,
                    onSelected = {
                        applyColorMode(context, ColorService.ColorMode.SATURATED, it) {
                            selectedId = ColorService.ColorMode.SATURATED.id
                        }
                    },
                )
                SettingsDivider()
                ColorModeRow(
                    label      = stringResource(R.string.color_mode_standard),
                    summary    = stringResource(R.string.color_mode_standard_summary),
                    selected   = selectedId == ColorService.ColorMode.STANDARD.id,
                    onSelected = {
                        applyColorMode(context, ColorService.ColorMode.STANDARD, it) {
                            selectedId = ColorService.ColorMode.STANDARD.id
                        }
                    },
                )
            }

            SettingsCategoryLabel(stringResource(R.string.color_section_expert))
            SettingsBlock {
                ColorModeRow(
                    label      = stringResource(R.string.color_mode_original),
                    summary    = stringResource(R.string.color_mode_original_summary),
                    selected   = selectedId == ColorService.ColorMode.ORIGINAL.id,
                    onSelected = {
                        applyColorMode(context, ColorService.ColorMode.ORIGINAL, it) {
                            selectedId = ColorService.ColorMode.ORIGINAL.id
                        }
                    },
                )
                SettingsDivider()
                ColorModeRow(
                    label      = stringResource(R.string.color_mode_p3),
                    summary    = stringResource(R.string.color_mode_p3_summary),
                    selected   = selectedId == ColorService.ColorMode.P3.id,
                    onSelected = {
                        applyColorMode(context, ColorService.ColorMode.P3, it) {
                            selectedId = ColorService.ColorMode.P3.id
                        }
                    },
                )
                SettingsDivider()
                ColorModeRow(
                    label      = stringResource(R.string.color_mode_srgb),
                    summary    = stringResource(R.string.color_mode_srgb_summary),
                    selected   = selectedId == ColorService.ColorMode.SRGB.id,
                    onSelected = {
                        applyColorMode(context, ColorService.ColorMode.SRGB, it) {
                            selectedId = ColorService.ColorMode.SRGB.id
                        }
                    },
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ColorModeRow(
    label      : String,
    summary    : String,
    selected   : Boolean,
    onSelected : (String) -> Unit,
    modifier   : Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue   = if (selected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = spring(),
        label         = "row-bg",
    )

    Surface(color = bgColor) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(role = Role.RadioButton) { onSelected(label) }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RadioButton(
                selected = selected,
                onClick  = null,
                colors   = RadioButtonDefaults.colors(
                    selectedColor   = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = label,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color      = if (selected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text  = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun applyColorMode(
    context  : Context,
    mode     : ColorService.ColorMode,
    label    : String,
    onSuccess: () -> Unit,
) {
    runCatching {
        Settings.System.putIntForUser(
            context.contentResolver,
            Settings.System.DISPLAY_COLOR_MODE,
            mode.id,
            UserHandle.USER_CURRENT,
        )
        onSuccess()
        Toast.makeText(
            context,
            context.getString(R.string.color_mode_applied, label),
            Toast.LENGTH_SHORT,
        ).show()
    }.onFailure {
        Toast.makeText(context, R.string.color_mode_failed, Toast.LENGTH_SHORT).show()
    }
}
