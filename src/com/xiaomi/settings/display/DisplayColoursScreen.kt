/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.display

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.xiaomi.settings.PartsCard
import com.xiaomi.settings.R
import com.xiaomi.settings.ui.PartsTokens

private typealias ColorMode = ColorService.ColorMode

private val ColorMode.hue: Color
    @Composable get() = when (this) {
        ColorMode.VIVID     -> PartsTokens.Colors.Hues.vivid
        ColorMode.SATURATED -> PartsTokens.Colors.Hues.saturated
        ColorMode.STANDARD  -> PartsTokens.Colors.Hues.standard
        ColorMode.ORIGINAL  -> PartsTokens.Colors.Hues.original
        ColorMode.P3        -> PartsTokens.Colors.Hues.p3
        ColorMode.SRGB      -> PartsTokens.Colors.Hues.srgb
    }

private const val ShimmerStart       = -600f
private const val ShimmerEnd         = 1200f
private const val ShimmerWidth       = 300f
private const val ShimmerAlpha       = 0.05f
private const val BlobRadiusFraction = 0.38f

@Composable
private fun ColourPreviewHero(
    selectedId: Int,
    modifier:   Modifier = Modifier,
) {
    val allModes = ColorMode.entries
    val alphas   = allModes.map { mode ->
        animateFloatAsState(
            targetValue   = if (mode.id == selectedId) 1f else 0.35f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
            label         = "alpha_${mode.name}",
        )
    }
    val hues = allModes.map { it.hue }

    val shimmerOffset by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue  = ShimmerStart,
        targetValue   = ShimmerEnd,
        animationSpec = infiniteRepeatable(
            animation  = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerOffset",
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(PartsTokens.heroPreviewHeight)
            .clip(PartsTokens.cardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        val cx = size.width / 2
        val cy = size.height / 2
        val r  = size.width * BlobRadiusFraction
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
                brush  = Brush.radialGradient(
                    colors = listOf(hues[i].copy(alpha = alphas[i].value), Color.Transparent),
                    center = positions.getOrElse(i) { Offset(cx, cy) },
                    radius = r,
                ),
                radius = r,
                center = positions.getOrElse(i) { Offset(cx, cy) },
            )
        }
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.White.copy(0f), Color.White.copy(ShimmerAlpha), Color.White.copy(0f)),
                start  = Offset(shimmerOffset, 0f),
                end    = Offset(shimmerOffset + ShimmerWidth, size.height),
            ),
        )
    }
}

@Composable
private fun SelectionRow(
    mode:       ColorMode,
    isSelected: Boolean,
    onClick:    () -> Unit,
) {
    val bgAlpha by animateFloatAsState(
        targetValue   = if (isSelected) PartsTokens.selectedStateLayerAlpha else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label         = "selectionBg",
    )
    val hue = mode.hue

    ListItem(
        modifier = Modifier
            .padding(horizontal = PartsTokens.contentPaddingHorizontal)
            .clip(PartsTokens.dialogSelectionShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = bgAlpha))
            .clickable(role = Role.RadioButton, onClick = onClick),
        headlineContent = {
            Text(
                text  = stringResource(mode.label),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) PartsTokens.Colors.dialogSelectedText else PartsTokens.Colors.textPrimary,
            )
        },
        supportingContent = {
            Text(
                text  = stringResource(mode.description),
                style = MaterialTheme.typography.bodySmall,
                color = PartsTokens.Colors.textSecondary,
            )
        },
        leadingContent = {
            Box(
                modifier         = Modifier
                    .size(PartsTokens.colourModeLeadingSize)
                    .clip(CircleShape)
                    .background(hue.copy(alpha = PartsTokens.colourModeIconContainerAlpha)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(PartsTokens.colourModeDotSize)
                        .clip(CircleShape)
                        .background(hue.copy(alpha = PartsTokens.colourModeIconDotAlpha)),
                )
            }
        },
        trailingContent = {
            AnimatedContent(
                targetState   = isSelected,
                transitionSpec = {
                    fadeIn(tween(PartsTokens.motionCheckFadeInMs)) togetherWith fadeOut(tween(PartsTokens.motionCheckFadeOutMs))
                },
                label = "checkIcon",
            ) { selected ->
                if (selected) {
                    Icon(
                        imageVector        = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint               = PartsTokens.Colors.dialogSelectedText,
                        modifier           = Modifier.size(PartsTokens.trailingIconSize),
                    )
                } else {
                    Box(Modifier.size(PartsTokens.trailingIconSize))
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayColoursScreen(onBack: () -> Unit) {
    val context    = LocalContext.current
    var selectedId by remember { mutableIntStateOf(ColorService.getColorMode(context)) }

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
                        text     = stringResource(R.string.display_colours_title),
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
            Spacer(Modifier.height(PartsTokens.cardBlockSpacing))

            ColourPreviewHero(
                selectedId = selectedId,
                modifier   = Modifier.padding(horizontal = PartsTokens.contentPaddingHorizontal),
            )

            Spacer(Modifier.height(PartsTokens.cardBlockSpacing))

            PartsCard {
                ColorMode.entries.forEach { mode ->
                    SelectionRow(
                        mode       = mode,
                        isSelected = mode.id == selectedId,
                        onClick    = {
                            runCatching {
                                ColorService.setColorMode(context, mode.id)
                                selectedId = mode.id
                            }.onFailure {
                                Toast.makeText(context, R.string.display_colour_failed, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
                }
            }

            Spacer(Modifier.height(PartsTokens.listBottomPadding))
        }
    }
}
