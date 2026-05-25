/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.display

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.PartsCard
import com.xiaomi.settings.R
import com.xiaomi.settings.utils.PartsToast

private typealias ColorMode = ColorService.ColorMode
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private const val SHIMMER_DURATION_MS = 2800

private val ColorMode.hue: Color
    @Composable get() = when (this) {
        ColorMode.VIVID     -> MaterialTheme.colorScheme.error
        ColorMode.SATURATED -> MaterialTheme.colorScheme.tertiary
        ColorMode.STANDARD  -> MaterialTheme.colorScheme.primary
        ColorMode.ORIGINAL  -> MaterialTheme.colorScheme.onSurface
        ColorMode.P3        -> MaterialTheme.colorScheme.secondary
        ColorMode.SRGB      -> MaterialTheme.colorScheme.onSurfaceVariant
    }

@Composable
private fun ColourPreviewHero(
    selectedId: Int,
    modifier:   Modifier = Modifier,
) {
    val allModes = ColorMode.entries

    val alphas = allModes.map { mode ->
        animateFloatAsState(
            targetValue   = if (mode.id == selectedId) 1f else 0.35f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
            label         = "alpha_${mode.name}",
        )
    }

    val hues = allModes.map { it.hue }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        val widthPx      = with(LocalDensity.current) { maxWidth.toPx() }
        val shimmerStart = -widthPx * 0.5f
        val shimmerEnd   = widthPx * 1.5f
        val shimmerWidth = widthPx * 0.25f

        val shimmerOffset by rememberInfiniteTransition(label = "shimmer").animateFloat(
            initialValue  = shimmerStart,
            targetValue   = shimmerEnd,
            animationSpec = infiniteRepeatable(
                animation  = tween(SHIMMER_DURATION_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "shimmerOffset",
        )

        Canvas(modifier = Modifier.matchParentSize()) {
            val cx = size.width  / 2
            val cy = size.height / 2
            val r  = size.width  * 0.38f

            val positions = listOf(
                Offset(cx * 0.3f, cy * 0.5f),
                Offset(cx * 0.9f, cy * 0.4f),
                Offset(cx * 1.5f, cy * 0.5f),
                Offset(cx * 0.5f, cy * 1.5f),
                Offset(cx * 1.1f, cy * 1.6f),
                Offset(cx * 1.7f, cy * 1.5f),
            )

            allModes.forEachIndexed { i, _ ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            hues[i].copy(alpha = alphas[i].value),
                            Color.Transparent,
                        ),
                        center = positions.getOrElse(i) { Offset(cx, cy) },
                        radius = r,
                    ),
                    radius = r,
                    center = positions.getOrElse(i) { Offset(cx, cy) },
                )
            }

            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0f),
                        Color.White.copy(alpha = 0.05f),
                        Color.White.copy(alpha = 0f),
                    ),
                    start = Offset(shimmerOffset, 0f),
                    end   = Offset(shimmerOffset + shimmerWidth, size.height),
                ),
            )
        }
    }
}

@Composable
private fun SelectionRow(
    mode:       ColorMode,
    isSelected: Boolean,
    onClick:    () -> Unit,
) {
    val bgAlpha by animateFloatAsState(
        targetValue   = if (isSelected) 0.12f else 0f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label         = "selectionBg",
    )
    val selectionColor = MaterialTheme.colorScheme.primary
    val hue            = mode.hue

    ListItem(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(selectionColor.copy(alpha = bgAlpha))
            .clickable(role = Role.RadioButton, onClick = onClick),
        headlineContent = {
            Text(
                text  = stringResource(mode.label),
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                        else            MaterialTheme.colorScheme.onSurface,
            )
        },
        supportingContent = {
            Text(
                text  = stringResource(mode.description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(hue.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(hue.copy(alpha = 0.85f)),
                )
            }
        },
        trailingContent = {
            AnimatedContent(
                targetState = isSelected,
                transitionSpec = {
                    fadeIn(tween(150, easing = LinearOutSlowInEasing)) togetherWith
                        fadeOut(tween(100, easing = FastOutLinearInEasing))
                },
                label = "checkIcon",
            ) { selected ->
                if (selected) {
                    Icon(
                        imageVector        = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayColoursScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var selectedId by remember {
        mutableIntStateOf(ColorService.getColorMode(context))
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text     = stringResource(R.string.display_colours_title),
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
                colors = TopAppBarDefaults.topAppBarColors(
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
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            PartsCard(modifier = Modifier.padding(top = 16.dp)) {
                ColourPreviewHero(
                    selectedId = selectedId,
                    modifier   = Modifier.padding(16.dp),
                )
                Spacer(Modifier.height(8.dp))
            }

            PartsCard {
                ColorMode.entries.forEach { mode ->
                    SelectionRow(
                        mode       = mode,
                        isSelected = selectedId == mode.id,
                        onClick    = {
                            runCatching {
                                ColorService.setColorMode(context, mode.id)
                                selectedId = mode.id
                            }.onFailure {
                                PartsToast.show(context, R.string.display_colours_failed)
                            }
                        },
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
