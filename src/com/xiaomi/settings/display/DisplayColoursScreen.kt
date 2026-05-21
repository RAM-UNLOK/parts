/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.display

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.PartsCard
import com.xiaomi.settings.R
import com.xiaomi.settings.ui.PartsTokens

private typealias ColorMode = ColorService.ColorMode

/** Maps each ColorMode to its representative hue from [PartsTokens.Colors.Hues]. */
private val ColorMode.hue: Color
    get() = when (this) {
        ColorMode.VIVID     -> PartsTokens.Colors.Hues.vivid
        ColorMode.SATURATED -> PartsTokens.Colors.Hues.saturated
        ColorMode.STANDARD  -> PartsTokens.Colors.Hues.standard
        ColorMode.ORIGINAL  -> PartsTokens.Colors.Hues.original
        ColorMode.P3        -> PartsTokens.Colors.Hues.p3
        ColorMode.SRGB      -> PartsTokens.Colors.Hues.srgb
    }

// ────────────────────────────────────────────────────────
// Hero gradient preview
// ────────────────────────────────────────────────────────

// Internal shimmer geometry — not user-visible tokens.
private const val ShimmerStart  = -600f
private const val ShimmerEnd    = 1200f
private const val ShimmerWidth  = 300f
private const val ShimmerAlpha  = 0.05f
// Blob radius as a fraction of the card width.
private const val BlobRadiusFraction = 0.38f

/**
 * Six radial colour blobs, one per [ColorMode]. The active mode’s blob
 * glows at full opacity; the others fade to 35 %. A gentle shimmer
 * diagonal glint sweeps continuously for an “alive” feel.
 *
 * No drawable resource required — pure Compose Canvas.
 */
@Composable
private fun ColourPreviewHero(
    selectedId: Int,
    modifier:   Modifier = Modifier,
) {
    val allModes = ColorMode.entries

    // Per-mode opacity — 1.0 for selected, 0.35 for others
    val alphas = allModes.map { mode ->
        animateFloatAsState(
            targetValue   = if (mode.id == selectedId) 1f else 0.35f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness    = Spring.StiffnessMediumLow,
            ),
            label = "hero-alpha-${mode.name}",
        ).value
    }

    // Shimmer sweep
    val shimmer = rememberInfiniteTransition(label = "hero-shimmer")
    val shimmerX by shimmer.animateFloat(
        initialValue  = ShimmerStart,
        targetValue   = ShimmerEnd,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "hero-shimmer-x",
    )

    Box(
        modifier = modifier
            .padding(horizontal = PartsTokens.contentPaddingHorizontal)
            .fillMaxWidth()
            .height(PartsTokens.heroPreviewHeight)
            .clip(PartsTokens.cardShape)
            .background(PartsTokens.Colors.heroBase),
    ) {
        // Blob positions: staggered 2x3 grid (fractional x/y)
        val positions = listOf(
            Offset(0.17f, 0.30f),  // VIVID     — top-left
            Offset(0.50f, 0.65f),  // SATURATED — mid-centre
            Offset(0.83f, 0.30f),  // STANDARD  — top-right
            Offset(0.17f, 0.75f),  // ORIGINAL  — bottom-left
            Offset(0.50f, 0.25f),  // P3        — top-centre
            Offset(0.83f, 0.75f),  // SRGB      — bottom-right
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            allModes.forEachIndexed { i, mode ->
                val centre = Offset(size.width * positions[i].x, size.height * positions[i].y)
                val radius = size.width * BlobRadiusFraction
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(mode.hue.copy(alpha = alphas[i]), Color.Transparent),
                        center = centre,
                        radius = radius,
                    ),
                    radius = radius,
                    center = centre,
                )
            }
            // Soft white diagonal shimmer glint
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = ShimmerAlpha),
                        Color.Transparent,
                    ),
                    start = Offset(shimmerX, 0f),
                    end   = Offset(shimmerX + ShimmerWidth, size.height),
                ),
            )
        }
    }
}

// ────────────────────────────────────────────────────────
// Main screen
// ────────────────────────────────────────────────────────

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
                        text     = stringResource(R.string.display_title),
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
            // 1. Hero preview
            Spacer(Modifier.height(PartsTokens.cardBlockSpacing))
            ColourPreviewHero(selectedId = selectedId)

            // 2. Standard section
            SectionHeader(stringResource(R.string.color_section_standard))
            PartsCard {
                listOf(ColorMode.VIVID, ColorMode.SATURATED, ColorMode.STANDARD)
                    .forEach { mode ->
                        mode.SelectionRow(
                            selectedId = selectedId,
                            onSelected = { applyMode(context, it) { selectedId = it.id } },
                        )
                    }
            }

            // 3. Expert section
            SectionHeader(stringResource(R.string.color_section_expert))
            PartsCard {
                listOf(ColorMode.ORIGINAL, ColorMode.P3, ColorMode.SRGB)
                    .forEach { mode ->
                        mode.SelectionRow(
                            selectedId = selectedId,
                            onSelected = { applyMode(context, it) { selectedId = it.id } },
                        )
                    }
            }

            Spacer(Modifier.height(PartsTokens.listBottomPadding))
        }
    }
}

// ────────────────────────────────────────────────────────
// Section header
// ────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelLarge,
        color    = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(
            start  = PartsTokens.sectionHeaderStartPadding,
            top    = PartsTokens.sectionHeaderTopPadding,
            bottom = PartsTokens.categoryBottomPadding,
        ),
    )
}

// ────────────────────────────────────────────────────────
// Selection row
// ────────────────────────────────────────────────────────

@Composable
private fun ColorMode.SelectionRow(
    selectedId: Int,
    onSelected: (ColorMode) -> Unit,
) {
    val isSelected = this.id == selectedId

    val containerColor by animateColorAsState(
        targetValue   = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = PartsTokens.colourModeSelectedAlpha)
        else
            Color.Transparent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness    = Spring.StiffnessMediumLow,
        ),
        label = "row-bg-${this.name}",
    )

    ListItem(
        modifier = Modifier.clickable(role = Role.RadioButton) { onSelected(this@SelectionRow) },
        colors   = ListItemDefaults.colors(containerColor = containerColor),
        leadingContent = {
            Box(
                modifier         = Modifier
                    .size(PartsTokens.colourModeLeadingSize)
                    .clip(CircleShape)
                    .background(this.hue.copy(alpha = PartsTokens.colourModeIconContainerAlpha)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(PartsTokens.colourModeDotSize)
                        .clip(CircleShape)
                        .background(this.hue.copy(alpha = PartsTokens.colourModeIconDotAlpha)),
                )
            }
        },
        headlineContent = {
            Text(
                text  = stringResource(this.labelRes),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    PartsTokens.Colors.textPrimary,
            )
        },
        supportingContent = {
            Text(
                text  = stringResource(this.summaryRes),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                else
                    PartsTokens.Colors.textSecondary,
            )
        },
        trailingContent = {
            AnimatedContent(
                targetState    = isSelected,
                transitionSpec = {
                    fadeIn(tween(PartsTokens.motionCheckFadeInMs)) togetherWith
                    fadeOut(tween(PartsTokens.motionCheckFadeOutMs))
                },
                label = "check-${this.name}",
            ) { selected ->
                if (selected) {
                    Icon(
                        imageVector        = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(PartsTokens.trailingIconSize),
                    )
                } else {
                    // Placeholder preserves trailing column width on deselection.
                    Box(Modifier.size(PartsTokens.trailingIconSize))
                }
            }
        },
    )
}

// ────────────────────────────────────────────────────────
// Write helper (unchanged)
// ────────────────────────────────────────────────────────

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
