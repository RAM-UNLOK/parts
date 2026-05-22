/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.thermal

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.xiaomi.settings.PartsCard
import com.xiaomi.settings.PartsCategory
import com.xiaomi.settings.R
import com.xiaomi.settings.ui.PartsTokens
import kotlinx.coroutines.launch

data class AppThermalEntry(
    val packageName: String,
    val label:       String,
    val profileId:   Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalManagementScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec  = PartsTokens.MotionSpringEnter,
        flingAnimationSpec = exponentialDecay(frictionMultiplier = 2f),
    )

    var selectedApp by remember { mutableStateOf<AppThermalEntry?>(null) }

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = PartsTokens.Colors.page,
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text     = stringResource(R.string.thermal_title),
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
                .padding(innerPadding),
        ) {
            PartsCategory(stringResource(R.string.thermal_per_app_category))

            LazyColumn(
                modifier           = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(PartsTokens.cardBlockSpacing),
            ) {
                items(ThermalService.getAppList(context)) { entry ->
                    AppThermalRow(
                        entry   = entry,
                        onClick = { selectedApp = entry },
                    )
                }
                item { Spacer(Modifier.height(PartsTokens.listBottomPadding)) }
            }
        }
    }

    selectedApp?.let { app ->
        PerAppPremiumCard(
            app       = app,
            onDismiss = { selectedApp = null },
            onSaved   = { profile ->
                runCatching {
                    ThermalService.setAppProfile(context, app.packageName, profile)
                    selectedApp = null
                }.onFailure {
                    Toast.makeText(context, R.string.thermal_failed_toast, Toast.LENGTH_SHORT).show()
                }
            },
        )
    }
}

@Composable
private fun AppThermalRow(
    entry:   AppThermalEntry,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PartsTokens.contentPaddingHorizontal)
            .clip(PartsTokens.cardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
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
                imageVector        = Icons.Filled.Thermostat,
                contentDescription = null,
                tint               = PartsTokens.Colors.iconContent,
                modifier           = Modifier.size(PartsTokens.leadingIconSize),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = entry.label,
                style    = MaterialTheme.typography.bodyLarge,
                color    = PartsTokens.Colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text  = ThermalService.profileLabel(entry.profileId),
                style = MaterialTheme.typography.bodySmall,
                color = PartsTokens.Colors.textSecondary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PerAppPremiumCard(
    app:      AppThermalEntry,
    onDismiss: () -> Unit,
    onSaved:  (Int) -> Unit,
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope      = rememberCoroutineScope()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PartsTokens.contentPaddingHorizontal),
        shape  = PartsTokens.cardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = PartsTokens.Colors.premiumCardSurface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = androidx.compose.ui.unit.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PartsTokens.contentPaddingHorizontal),
            verticalArrangement = Arrangement.spacedBy(PartsTokens.rowPaddingVertical),
        ) {
            Text(
                text  = app.label,
                style = MaterialTheme.typography.titleMedium,
                color = PartsTokens.Colors.premiumCardContent,
            )
            Text(
                text  = ThermalService.profileLabel(app.profileId),
                style = MaterialTheme.typography.bodyMedium,
                color = PartsTokens.Colors.premiumCardContent,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(PartsTokens.rowElementSpacing),
            ) {
                Button(
                    onClick = { showSheet = true },
                    shape   = PartsTokens.buttonShape,
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor   = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(stringResource(R.string.thermal_select_profile))
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState       = sheetState,
            shape            = PartsTokens.bottomSheetTopShape,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = PartsTokens.listBottomPadding),
            ) {
                Text(
                    text     = stringResource(R.string.thermal_select_profile),
                    style    = MaterialTheme.typography.titleMedium,
                    color    = PartsTokens.Colors.textPrimary,
                    modifier = Modifier.padding(
                        horizontal = PartsTokens.contentPaddingHorizontal,
                        vertical   = PartsTokens.rowPaddingVertical,
                    ),
                )
                ThermalService.profiles().forEach { profile ->
                    val isSelected = profile.id == app.profileId
                    val bgAlpha by animateFloatAsState(
                        targetValue   = if (isSelected) PartsTokens.selectedStateLayerAlpha else 0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
                        label         = "profileBg_${profile.id}",
                    )
                    ListItem(
                        modifier = Modifier
                            .padding(horizontal = PartsTokens.contentPaddingHorizontal)
                            .clip(PartsTokens.dialogSelectionShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = bgAlpha))
                            .clickable(role = Role.RadioButton) {
                                scope.launch {
                                    sheetState.hide()
                                }.invokeOnCompletion {
                                    showSheet = false
                                    onSaved(profile.id)
                                }
                            },
                        headlineContent = {
                            Text(
                                text  = profile.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) PartsTokens.Colors.dialogSelectedText else PartsTokens.Colors.textPrimary,
                            )
                        },
                        trailingContent = {
                            AnimatedContent(
                                targetState   = isSelected,
                                transitionSpec = {
                                    fadeIn(tween(PartsTokens.motionCheckFadeInMs)) togetherWith
                                    fadeOut(tween(PartsTokens.motionCheckFadeOutMs))
                                },
                                label = "check_${profile.id}",
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
            }
        }
    }
}
