/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.thermal

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
    var showSheet   by remember { mutableStateOf(false) }
    val sheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope       = rememberCoroutineScope()
    val appList     = remember { ThermalService.getAppList(context) }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            item(key = "spacer-top") { Spacer(Modifier.height(PartsTokens.cardBlockSpacing)) }

            item(key = "per-app-label") {
                PartsCategory(stringResource(R.string.thermal_per_app_category))
            }

            selectedApp?.let { app ->
                item(key = "premium-card") {
                    ElevatedCard(
                        modifier  = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = PartsTokens.contentPaddingHorizontal,
                                vertical   = PartsTokens.appRowSpacing,
                            ),
                        shape     = PartsTokens.cardShape,
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = PartsTokens.premiumCardElevation,
                        ),
                        colors    = CardDefaults.elevatedCardColors(
                            containerColor = PartsTokens.Colors.premiumCardSurface,
                        ),
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
            }

            item(key = "app-list-card") {
                PartsCard {
                    appList.forEachIndexed { index, entry ->
                        AppThermalRow(
                            entry       = entry,
                            isSelected  = entry.packageName == selectedApp?.packageName,
                            showDivider = index < appList.lastIndex,
                            onClick     = {
                                selectedApp = if (selectedApp?.packageName == entry.packageName) null
                                              else entry
                            },
                        )
                    }
                }
            }

            item(key = "bottom-spacer") { Spacer(Modifier.height(PartsTokens.listBottomPadding)) }
        }
    }

    if (showSheet && selectedApp != null) {
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
                Spacer(Modifier.height(PartsTokens.sheetContentTopPadding))
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
                    val isSelected = profile.id == selectedApp?.profileId
                    val bgAlpha by animateFloatAsState(
                        targetValue   = if (isSelected) PartsTokens.selectedStateLayerAlpha else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness    = Spring.StiffnessMedium,
                        ),
                        label = "profileBg_${profile.id}",
                    )
                    ListItem(
                        modifier = Modifier
                            .padding(horizontal = PartsTokens.contentPaddingHorizontal)
                            .clip(PartsTokens.dialogSelectionShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = bgAlpha))
                            .clickable(role = Role.RadioButton) {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showSheet = false
                                    selectedApp?.let { app ->
                                        runCatching {
                                            ThermalService.setAppProfile(context, app.packageName, profile.id)
                                            selectedApp = null
                                        }.onFailure {
                                            Toast.makeText(context, R.string.thermal_failed_toast, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                        headlineContent = {
                            Text(
                                text  = profile.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) PartsTokens.Colors.dialogSelectedText
                                        else PartsTokens.Colors.textPrimary,
                            )
                        },
                        trailingContent = {
                            AnimatedContent(
                                targetState    = isSelected,
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

@Composable
private fun AppThermalRow(
    entry:       AppThermalEntry,
    isSelected:  Boolean,
    showDivider: Boolean,
    onClick:     () -> Unit,
) {
    Column {
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
                    .clip(PartsTokens.iconContainerShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else            PartsTokens.Colors.iconContainer,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Filled.Thermostat,
                    contentDescription = null,
                    tint               = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                         else            PartsTokens.Colors.iconContent,
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
        if (showDivider) {
            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = PartsTokens.contentPaddingHorizontal),
                thickness = DividerDefaults.Thickness,
                color     = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}
