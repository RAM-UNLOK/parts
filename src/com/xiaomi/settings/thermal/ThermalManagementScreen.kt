/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.thermal

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.graphics.drawable.toBitmap
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

@Composable
private fun appIcon(packageName: String): Drawable? {
    val pm = LocalContext.current.packageManager
    return remember(packageName) {
        runCatching { pm.getApplicationIcon(packageName) }.getOrNull()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalManagementScreen(onBack: () -> Unit) {
    val context    = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope      = rememberCoroutineScope()

    var pendingApp by remember { mutableStateOf<AppThermalEntry?>(null) }
    var showSheet  by remember { mutableStateOf(false) }

    val appList = remember {
        runCatching { ThermalService.getAppList(context) }.getOrElse {
            emptyList<AppThermalEntry>().also {
                Toast.makeText(context, R.string.thermal_failed_toast, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec  = PartsTokens.MotionSpringEnter,
        flingAnimationSpec = exponentialDecay(frictionMultiplier = PartsTokens.frictionMultiplier),
    )

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
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            item(key = "spacer-top") { Spacer(Modifier.height(PartsTokens.cardBlockSpacing)) }

            item(key = "per-app-label") {
                PartsCategory(stringResource(R.string.thermal_per_app_category))
            }

            if (appList.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text     = stringResource(R.string.thermal_no_apps),
                        style    = PartsTokens.Type.rowSupporting,
                        color    = PartsTokens.Colors.textSecondary,
                        modifier = Modifier.padding(
                            horizontal = PartsTokens.contentPaddingHorizontal,
                            vertical   = PartsTokens.rowPaddingVertical,
                        ),
                    )
                }
            } else {
                itemsIndexed(
                    items = appList,
                    key   = { _, entry -> entry.packageName },
                ) { index, entry ->
                    AppThermalPremiumCard(
                        entry           = entry,
                        showDivider     = index < appList.lastIndex,
                        onSelectProfile = {
                            pendingApp = entry
                            showSheet  = true
                        },
                    )
                }
            }

            item(key = "bottom-spacer") { Spacer(Modifier.height(PartsTokens.listBottomPadding)) }
        }
    }

    if (showSheet && pendingApp != null) {
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
                    style    = PartsTokens.Type.sheetTitle,
                    color    = PartsTokens.Colors.textPrimary,
                    modifier = Modifier.padding(
                        horizontal = PartsTokens.contentPaddingHorizontal,
                        vertical   = PartsTokens.rowPaddingVertical,
                    ),
                )
                ThermalService.profiles().forEach { profile ->
                    val isSelected = profile.id == pendingApp?.profileId
                    val bgAlpha by animateFloatAsState(
                        targetValue   = if (isSelected) PartsTokens.selectedStateLayerAlpha else 0f,
                        animationSpec = PartsTokens.MotionSpringNoBouncy,
                        label         = "profileBg_${profile.id}",
                    )
                    ListItem(
                        modifier = Modifier
                            .padding(horizontal = PartsTokens.contentPaddingHorizontal)
                            .clip(PartsTokens.dialogSelectionShape)
                            .background(PartsTokens.Colors.dialogSelectedLayer.copy(alpha = bgAlpha))
                            .clickable(role = Role.RadioButton) {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showSheet = false
                                    pendingApp?.let { app ->
                                        runCatching {
                                            ThermalService.setAppProfile(context, app.packageName, profile.id)
                                        }.onFailure {
                                            Toast.makeText(context, R.string.thermal_failed_toast, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    pendingApp = null
                                }
                            },
                        headlineContent = {
                            Text(
                                text  = profile.label,
                                style = PartsTokens.Type.rowHeadline,
                                color = if (isSelected) PartsTokens.Colors.dialogSelectedText
                                        else            PartsTokens.Colors.textPrimary,
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
                        colors = ListItemDefaults.colors(containerColor = PartsTokens.Colors.transparent),
                    )
                }
            }
        }
    }
}

@Composable
private fun AppThermalPremiumCard(
    entry:           AppThermalEntry,
    showDivider:     Boolean,
    onSelectProfile: () -> Unit,
) {
    val icon = appIcon(entry.packageName)

    Column {
        ElevatedCard(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = PartsTokens.contentPaddingHorizontal,
                    vertical   = PartsTokens.premiumCardSpacing,
                ),
            shape     = PartsTokens.cardShape,
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = PartsTokens.premiumCardElevation,
            ),
            colors    = CardDefaults.elevatedCardColors(
                containerColor = PartsTokens.Colors.premiumCardSurface,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PartsTokens.contentPaddingHorizontal),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PartsTokens.rowElementSpacing),
            ) {
                if (icon != null) {
                    Image(
                        bitmap             = icon.toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier           = Modifier
                            .size(PartsTokens.appIconSize)
                            .clip(PartsTokens.leadingIconShape),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(PartsTokens.appIconSize)
                            .clip(PartsTokens.leadingIconShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text     = entry.label,
                        style    = PartsTokens.Type.cardTitle,
                        color    = PartsTokens.Colors.premiumCardContent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Button(
                    onClick = onSelectProfile,
                    shape   = PartsTokens.buttonShape,
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = PartsTokens.Colors.premiumCardButton,
                        contentColor   = PartsTokens.Colors.premiumCardButtonContent,
                    ),
                ) {
                    Text(
                        text     = ThermalService.profileLabel(entry.profileId),
                        style    = PartsTokens.Type.buttonLabel,
                        maxLines = 1,
                    )
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = PartsTokens.contentPaddingHorizontal),
                thickness = PartsTokens.dividerThickness,
                color     = PartsTokens.Colors.outlineVariant,
            )
        }
    }
}
