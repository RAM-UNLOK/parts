/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xiaomi.settings.display

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.PartsCard
import com.xiaomi.settings.PartsCategory
import com.xiaomi.settings.PartsDivider
import com.xiaomi.settings.R

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
                title  = {
                    Text(
                        text  = stringResource(R.string.display_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint               = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor      = MaterialTheme.colorScheme.onSurface,
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
            PartsCategory(stringResource(R.string.color_section_standard))
            PartsCard {
                ColorMode.VIVID.Row(selectedId)     { applyMode(context, it) { selectedId = it.id } }
                PartsDivider()
                ColorMode.SATURATED.Row(selectedId) { applyMode(context, it) { selectedId = it.id } }
                PartsDivider()
                ColorMode.STANDARD.Row(selectedId)  { applyMode(context, it) { selectedId = it.id } }
            }

            PartsCategory(stringResource(R.string.color_section_expert))
            PartsCard {
                ColorMode.ORIGINAL.Row(selectedId) { applyMode(context, it) { selectedId = it.id } }
                PartsDivider()
                ColorMode.P3.Row(selectedId)       { applyMode(context, it) { selectedId = it.id } }
                PartsDivider()
                ColorMode.SRGB.Row(selectedId)     { applyMode(context, it) { selectedId = it.id } }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private typealias ColorMode = ColorService.ColorMode

@Composable
private fun ColorMode.Row(
    selectedId : Int,
    onSelected : (ColorMode) -> Unit,
) {
    val selected = this.id == selectedId
    val bgColor by animateColorAsState(
        targetValue   = if (selected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = spring(),
        label         = "color-row-bg-${this.name}",
    )
    val label   = stringResource(this.labelRes)
    val summary = stringResource(this.summaryRes)

    Surface(color = bgColor) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.RadioButton) { onSelected(this@Row) }
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

private fun applyMode(
    context  : Context,
    mode     : ColorMode,
    onSuccess: (ColorMode) -> Unit,
) {
    runCatching {
        Settings.System.putIntForUser(
            context.contentResolver,
            Settings.System.DISPLAY_COLOR_MODE,
            mode.id,
            UserHandle.USER_CURRENT,
        )
        onSuccess(mode)
        val label = context.getString(mode.labelRes)
        Toast.makeText(context, context.getString(R.string.color_mode_applied, label), Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(context, R.string.color_mode_failed, Toast.LENGTH_SHORT).show()
    }
}
