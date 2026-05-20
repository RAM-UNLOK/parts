/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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

    // ── Scroll behaviour ──────────────────────────────────────────────────────
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec  = spring(
            dampingRatio = PartsTokens.MotionDampingRatio,
            stiffness    = Spring.StiffnessHigh,
        ),
        flingAnimationSpec = exponentialDecay(frictionMultiplier = 2f),
    )

    // ── Section entrance animation states ─────────────────────────────────────
    val visDisplay     = remember { MutableTransitionState(false).apply { targetState = true } }
    val visPerformance = remember { MutableTransitionState(false).apply { targetState = true } }
    val visDiagnostics = remember { MutableTransitionState(false).apply { targetState = true } }

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
            // ── Display section  (stagger group 0 — no delay) ─────────────────
            AnimatedVisibility(
                visibleState = visDisplay,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = PartsTokens.MotionDurationEnter,
                        delayMillis    = 0,
                        easing         = EaseOutCubic,
                    ),
                ) + slideInVertically(
                    animationSpec  = tween(
                        durationMillis = PartsTokens.MotionDurationSlide,
                        delayMillis    = 0,
                        easing         = EaseOutCubic,
                    ),
                    initialOffsetY = { it / PartsTokens.MotionSlideDistance },
                ),
            ) {
                Column {
                    PartsCategory(stringResource(R.string.xiaomi_parts_category_display))
                    PartsCard {
                        PartsRow(
                            icon    = Icons.Filled.Palette,
                            title   = stringResource(R.string.display_title),
                            summary = stringResource(R.string.display_summary),
                            onClick = onNavigateToDisplay,
                        )
                    }
                }
            }

            Spacer(Modifier.height(PartsTokens.cardBlockSpacing))

            // ── Performance section  (stagger group 1 — 1× step delay) ────────
            AnimatedVisibility(
                visibleState = visPerformance,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = PartsTokens.MotionDurationEnter,
                        delayMillis    = PartsTokens.MotionStaggerStep,
                        easing         = EaseOutCubic,
                    ),
                ) + slideInVertically(
                    animationSpec  = tween(
                        durationMillis = PartsTokens.MotionDurationSlide,
                        delayMillis    = PartsTokens.MotionStaggerStep,
                        easing         = EaseOutCubic,
                    ),
                    initialOffsetY = { it / PartsTokens.MotionSlideDistance },
                ),
            ) {
                Column {
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
                }
            }

            Spacer(Modifier.height(PartsTokens.cardBlockSpacing))

            // ── Diagnostics section  (stagger group 2 — 2× step delay) ────────
            AnimatedVisibility(
                visibleState = visDiagnostics,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = PartsTokens.MotionDurationEnter,
                        delayMillis    = PartsTokens.MotionStaggerStep * 2,
                        easing         = EaseOutCubic,
                    ),
                ) + slideInVertically(
                    animationSpec  = tween(
                        durationMillis = PartsTokens.MotionDurationSlide,
                        delayMillis    = PartsTokens.MotionStaggerStep * 2,
                        easing         = EaseOutCubic,
                    ),
                    initialOffsetY = { it / PartsTokens.MotionSlideDistance },
                ),
            ) {
                Column {
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
                }
            }

            Spacer(Modifier.height(PartsTokens.listBottomPadding))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared composables
// ─────────────────────────────────────────────────────────────────────────────

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

@Composable
fun PartsCard(
    modifier: Modifier = Modifier,
    content:  @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier       = modifier
            .fillMaxWidth()
            .padding(horizontal = PartsTokens.contentPaddingHorizontal),
        shape          = PartsTokens.cardShape,
        color          = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = PartsTokens.cardElevation,
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
