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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.PartsCard
import com.xiaomi.settings.PartsCategory
import com.xiaomi.settings.R
import com.xiaomi.settings.ui.PartsTokens

/**
 * Display Colours sub-screen.
 *
 * Back navigation: the [navigationIcon] IconButton is intentionally absent.
 * Android's predictive-back gesture handles back natively on Android 13+.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayColoursScreen() {
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

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec  = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness    = Spring.StiffnessMediumLow,
        ),
        flingAnimationSpec = exponentialDecay(frictionMultiplier = 2f),
    )

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text     = stringResource(R.string.display_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                // No navigationIcon: back handled by predictive-back gesture.
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
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
                            modifier = Modifier.padding(
                                horizontal = PartsTokens.contentPaddingHorizontal,
                            ),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }

            PartsCategory(stringResource(R.string.color_section_expert))
            PartsCard(modifier = Modifier.selectableGroup()) {
                expertModes.forEachIndexed { index, mode ->
                    mode.Row(selectedId) { applyMode(context, it) { selectedId = it.id } }
                    if (index < expertModes.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(
                                horizontal = PartsTokens.contentPaddingHorizontal,
                            ),
                            color = MaterialTheme.colorScheme.outlineVariant,
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

    // Selected rows: secondaryContainer highlight.
    // Unselected rows: Color.Transparent — flush with the surfaceContainer card.
    val bgColor by animateColorAsState(
        targetValue   = if (selected)
            MaterialTheme.colorScheme.secondaryContainer
        else
            Color.Transparent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness    = Spring.StiffnessMediumLow,
        ),
        label = "color-row-bg-${this.name}",
    )

    // Single source of truth for content color — used by both title and summary.
    // Selected: onSecondaryContainer. Unselected: title uses onSurface,
    // summary uses onSurfaceVariant (one step quieter per M3 list spec).
    val selectedContentColor   = MaterialTheme.colorScheme.onSecondaryContainer
    val unselectedTitleColor   = MaterialTheme.colorScheme.onSurface
    val unselectedSummaryColor = MaterialTheme.colorScheme.onSurfaceVariant

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
                color      = if (selected) selectedContentColor else unselectedTitleColor,
            )
            Text(
                text  = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) selectedContentColor else unselectedSummaryColor,
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
