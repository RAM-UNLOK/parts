/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.display

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.PartsCard
import com.xiaomi.settings.PartsCategory
import com.xiaomi.settings.R
import com.xiaomi.settings.ui.PartsTokens

@OptIn(ExperimentalMaterial3Api::class)
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
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text  = stringResource(R.string.display_title),
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
            val standardModes = listOf(ColorMode.VIVID, ColorMode.SATURATED, ColorMode.STANDARD)
            val expertModes   = listOf(ColorMode.ORIGINAL, ColorMode.P3, ColorMode.SRGB)

            PartsCategory(stringResource(R.string.color_section_standard))
            PartsCard(modifier = Modifier.selectableGroup()) {
                standardModes.forEachIndexed { index, mode ->
                    mode.Row(selectedId) { applyMode(context, it) { selectedId = it.id } }
                    if (index < standardModes.lastIndex) {
                        HorizontalDivider(
                            modifier  = Modifier.padding(horizontal = PartsTokens.contentPaddingHorizontal),
                            thickness = 0.5.dp,
                            color     = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }

            // No explicit Spacer here — PartsCategory already contributes
            // categoryTopPadding (24dp) above its label. Adding a cardBlockSpacing
            // (16dp) Spacer on top of that produced a 40dp gap which was visually
            // too wide and inconsistent with the home screen section rhythm.
            PartsCategory(stringResource(R.string.color_section_expert))
            PartsCard(modifier = Modifier.selectableGroup()) {
                expertModes.forEachIndexed { index, mode ->
                    mode.Row(selectedId) { applyMode(context, it) { selectedId = it.id } }
                    if (index < expertModes.lastIndex) {
                        HorizontalDivider(
                            modifier  = Modifier.padding(horizontal = PartsTokens.contentPaddingHorizontal),
                            thickness = 0.5.dp,
                            color     = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(PartsTokens.listBottomPadding))
        }
    }
}

private typealias ColorMode = ColorService.ColorMode

@Composable
private fun ColorMode.Row(
    selectedId: Int,
    onSelected: (ColorMode) -> Unit,
) {
    val selected = this.id == selectedId

    // Use secondaryContainer as the selected-row background.
    //
    // Why NOT lerp(surfaceContainerLow, primaryContainer, 0.12f):
    //   Dynamic colour (wallpaper-derived tonal palette) can place
    //   primaryContainer very close in lightness to surfaceContainerLow,
    //   making a 12% lerp invisible — especially when Xiaomi Parts is
    //   hosted inside the Settings process via Connection Preferences.
    //
    // Why secondaryContainer:
    //   M3 Expressive selected-row pattern (used in Pixel Settings radio
    //   groups, navigation drawer, etc.) uses secondaryContainer as the
    //   persistent selected-state fill. It is always tonally distinct from
    //   every surface level in the dynamic colour system by construction —
    //   secondary and surface hues are generated from different palette
    //   keys. This guarantees perceptible contrast regardless of wallpaper.
    val bgColor by animateColorAsState(
        targetValue   = if (selected)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = spring(),
        label         = "color-row-bg-${this.name}",
    )

    // Text and radio tint: onSecondaryContainer when selected so the
    // foreground colour is guaranteed to meet contrast on the new bg.
    val contentColor = if (selected)
        MaterialTheme.colorScheme.onSecondaryContainer
    else
        MaterialTheme.colorScheme.onSurface

    val label   = stringResource(this.labelRes)
    val summary = stringResource(this.summaryRes)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(role = Role.RadioButton) { onSelected(this@Row) }
            .padding(
                horizontal = PartsTokens.contentPaddingHorizontal,
                vertical   = PartsTokens.rowPaddingVertical,
            ),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PartsTokens.rowElementSpacing),
    ) {
        RadioButton(
            selected = selected,
            onClick  = null,
            colors   = RadioButtonDefaults.colors(
                selectedColor   = MaterialTheme.colorScheme.onSecondaryContainer,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = label,
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color      = contentColor,
            )
            Text(
                text  = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun applyMode(
    context:   Context,
    mode:      ColorMode,
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
        Toast.makeText(
            context,
            context.getString(R.string.color_mode_applied, context.getString(mode.labelRes)),
            Toast.LENGTH_SHORT,
        ).show()
    }.onFailure {
        Toast.makeText(context, R.string.color_mode_failed, Toast.LENGTH_SHORT).show()
    }
}
