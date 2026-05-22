/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.navigation.NavController
import com.xiaomi.settings.ui.PartsTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XiaomiPartsHomeScreen(nav: NavController) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec  = PartsTokens.MotionSpringEnter,
        flingAnimationSpec = exponentialDecay(frictionMultiplier = 2f),
    )

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = PartsTokens.Colors.page,
        topBar = {
            LargeTopAppBar(
                title  = { Text(stringResource(R.string.xiaomi_parts_title)) },
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
            PartsCategory(stringResource(R.string.category_display))

            PartsCard {
                HomeRow(
                    icon  = Icons.Outlined.ColorLens,
                    title = stringResource(R.string.display_colours_title),
                    onClick = { nav.navigate("displayColours") },
                )
            }

            Spacer(Modifier.height(PartsTokens.cardBlockSpacing))

            PartsCategory(stringResource(R.string.category_performance))

            PartsCard {
                HomeRow(
                    icon  = Icons.Outlined.Thermostat,
                    title = stringResource(R.string.thermal_title),
                    onClick = { nav.navigate("thermal") },
                )
                HomeRow(
                    icon  = Icons.Outlined.Speed,
                    title = stringResource(R.string.htsr_title),
                    onClick = { nav.navigate("touchBoost") },
                )
            }

            Spacer(Modifier.height(PartsTokens.listBottomPadding))
        }
    }
}

@Composable
private fun HomeRow(
    icon:    androidx.compose.ui.graphics.vector.ImageVector,
    title:   String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
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
                .background(PartsTokens.Colors.iconContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = PartsTokens.Colors.iconContent,
                modifier           = Modifier.size(PartsTokens.leadingIconSize),
            )
        }
        Text(
            text  = title,
            style = MaterialTheme.typography.bodyLarge,
            color = PartsTokens.Colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun PartsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = PartsTokens.contentPaddingHorizontal)
            .fillMaxWidth()
            .clip(PartsTokens.cardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        content()
    }
}

@Composable
fun PartsCategory(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelMedium,
        color    = PartsTokens.Colors.textSecondary,
        modifier = Modifier
            .padding(
                start  = PartsTokens.contentPaddingHorizontal,
                top    = PartsTokens.categoryTopPadding,
                bottom = PartsTokens.categoryBottomPadding,
            ),
    )
}
