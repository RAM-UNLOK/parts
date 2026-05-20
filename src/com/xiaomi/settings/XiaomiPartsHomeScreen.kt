/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import com.xiaomi.settings.thermal.ThermalUtils
import com.xiaomi.settings.ui.PartsTokens
import com.xiaomi.settings.utils.CitLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XiaomiPartsHomeScreen(
    onNavigateToDisplay: () -> Unit,
    onNavigateToThermal: () -> Unit,
    onNavigateToTouch:   () -> Unit,
) {
    val context      = LocalContext.current
    val thermalUtils = remember { ThermalUtils.getInstance(context) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
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
            LargeTopAppBar(
                title  = { Text(text = stringResource(R.string.xiaomi_parts_title)) },
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
            // ── Display ──────────────────────────────────────────
            PartsCategory(stringResource(R.string.xiaomi_parts_category_display))
            PartsCard {
                PartsRow(
                    icon    = Icons.Filled.Palette,
                    title   = stringResource(R.string.display_title),
                    summary = stringResource(R.string.display_summary),
                    onClick = onNavigateToDisplay,
                )
            }

            // ── Performance ─────────────────────────────────────
            PartsCategory(stringResource(R.string.xiaomi_parts_category_performance))
            PartsCard {
                PartsRow(
                    icon    = Icons.Filled.Thermostat,
                    title   = stringResource(R.string.thermal_title),
                    summary = stringResource(
                        if (thermalUtils.enabled) R.string.thermal_summary_active
                        else                      R.string.thermal_summary_disabled
                    ),
                    onClick = onNavigateToThermal,
                )
                HorizontalDivider(
                    modifier  = Modifier.padding(horizontal = PartsTokens.contentPaddingHorizontal),
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant,
                )
                PartsRow(
                    icon    = Icons.Filled.TouchApp,
                    title   = stringResource(R.string.htsr_title),
                    summary = stringResource(R.string.htsr_summary),
                    onClick = onNavigateToTouch,
                )
            }

            // ── Diagnostics ─────────────────────────────────────
            PartsCategory(stringResource(R.string.xiaomi_parts_category_diagnostics))
            PartsCard {
                PartsRow(
                    icon    = Icons.Filled.Science,
                    title   = stringResource(R.string.cit_title),
                    summary = stringResource(R.string.cit_summary),
                    onClick = {
                        if (!CitLauncher.launch(context))
                            Toast.makeText(
                                context,
                                R.string.cit_not_found,
                                Toast.LENGTH_SHORT,
                            ).show()
                    },
                )
            }

            Spacer(Modifier.height(PartsTokens.listBottomPadding))
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────
// Shared composables used by HomeScreen and sub-screens
// ───────────────────────────────────────────────────────────────────────────

@Composable
fun PartsCategory(label: String, modifier: Modifier = Modifier) {
    Text(
        text     = label,
        style    = MaterialTheme.typography.titleSmall,
        color    = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(
            start  = PartsTokens.contentPaddingHorizontal,
            top    = PartsTokens.categoryTopPadding,
            bottom = PartsTokens.categoryBottomPadding,
        ),
    )
}

/**
 * Shared card container used on every screen.
 *
 * Background: [MaterialTheme.colorScheme.surfaceContainer]
 * Why surfaceContainer and not surfaceContainerLow:
 *   On dark Monet palettes (which is the dominant mode for this app),
 *   surfaceContainerLow is only ~1-2% lighter than `surface`, making cards
 *   visually invisible. surfaceContainer is the correct M3 token for
 *   "elevated but not prominent" containers — it matches AOSP Settings
 *   card appearance on both light and dark dynamic-colour schemes.
 *
 * Tonal elevation is intentionally omitted: M3 spec says use either a
 * distinct container colour OR tonal elevation, not both. Adding tonal
 * elevation on top of surfaceContainer double-tints in dark mode.
 */
@Composable
fun PartsCard(
    modifier: Modifier = Modifier,
    content:  @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PartsTokens.contentPaddingHorizontal),
        shape    = PartsTokens.cardShape,
        color    = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(content = content)
    }
}

@Composable
fun PartsRow(
    icon:     ImageVector,
    title:    String,
    summary:  String,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
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
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier           = Modifier.size(PartsTokens.leadingIconSize),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text  = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(PartsTokens.trailingIconSize),
            )
        }
    }
}
