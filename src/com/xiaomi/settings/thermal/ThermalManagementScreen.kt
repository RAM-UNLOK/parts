/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.thermal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
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
import com.xiaomi.settings.utils.PartsToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
private fun appIcon(packageName: String): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(packageName) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(packageName) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                context.packageManager.getApplicationIcon(packageName).toBitmap().asImageBitmap()
            }.getOrNull()
        }
    }
    return bitmap
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalManagementScreen(onBack: () -> Unit) {
    val context    = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope      = rememberCoroutineScope()

    val cardShape      = PartsTokens.cardShape
    val selectionShape = PartsTokens.dialogSelectionShape
    val sheetShape     = PartsTokens.bottomSheetTopShape
    val buttonShape    = PartsTokens.buttonShape
    val leadingShape   = PartsTokens.leadingIconShape
    val spatialSpec    = PartsTokens.motionDefaultSpatial<Float>()
    // Hoisted here so it can be captured in non-@Composable transitionSpec lambdas
    val effectsSpec: FiniteAnimationSpec<Float> = PartsTokens.motionDefaultEffects()

    var pendingApp    by remember { mutableStateOf<AppThermalEntry?>(null) }
    var showSheet     by remember { mutableStateOf(false) }
    var globalProfile by remember { mutableIntStateOf(ThermalService.getGlobalProfile(context)) }
    var appList       by remember { mutableStateOf<List<AppThermalEntry>>(emptyList()) }

    LaunchedEffect(Unit) {
        appList = withContext(Dispatchers.IO) {
            runCatching { ThermalService.getAppList(context) }.getOrElse {
                PartsToast.show(context, R.string.thermal_failed_toast)
                emptyList()
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec  = spatialSpec,
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_up),
                        )
                    }
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
            item(key = "global-label") { PartsCategory(stringResource(R.string.thermal_global_label)) }
            item(key = "global-card") {
                PartsCard {
                    ThermalService.profiles().forEach { profile ->
                        val isActive = profile.id == globalProfile
                        val bgAlpha by animateFloatAsState(
                            targetValue   = if (isActive) PartsTokens.selectedStateLayerAlpha else 0f,
                            animationSpec = effectsSpec,
                            label         = "globalBg_${profile.id}",
                        )
                        ListItem(
                            modifier = Modifier
                                .padding(horizontal = PartsTokens.contentPaddingHorizontal)
                                .clip(selectionShape)
                                .background(PartsTokens.Colors.selectionLayer.copy(alpha = bgAlpha))
                                .clickable(role = Role.RadioButton) {
                                    runCatching {
                                        ThermalService.setGlobalProfile(context, profile.id)
                                        globalProfile = profile.id
                                    }.onFailure {
                                        PartsToast.show(context, R.string.thermal_failed_toast)
                                    }
                                },
                            headlineContent = {
                                Text(
                                    text  = context.getString(profile.label),
                                    style = PartsTokens.Type.rowHeadline,
                                    color = if (isActive) PartsTokens.Colors.dialogSelectedText
                                            else          PartsTokens.Colors.textPrimary,
                                )
                            },
                            trailingContent = {
                                AnimatedContent(
                                    targetState    = isActive,
                                    transitionSpec = {
                                        fadeIn(effectsSpec) togetherWith fadeOut(effectsSpec)
                                    },
                                    label = "check_${profile.id}",
                                ) { active ->
                                    if (active) {
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

            item(key = "per-app-label") { PartsCategory(stringResource(R.string.thermal_per_app_label)) }

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
                        cardShape       = cardShape,
                        leadingShape    = leadingShape,
                        buttonShape     = buttonShape,
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
            shape            = sheetShape,
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
                        animationSpec = effectsSpec,
                        label         = "profileBg_${profile.id}",
                    )
                    ListItem(
                        modifier = Modifier
                            .padding(horizontal = PartsTokens.contentPaddingHorizontal)
                            .clip(selectionShape)
                            .background(PartsTokens.Colors.selectionLayer.copy(alpha = bgAlpha))
                            .clickable(role = Role.RadioButton) {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showSheet = false
                                    pendingApp?.let { app ->
                                        runCatching {
                                            ThermalService.setAppProfile(
                                                context,
                                                app.packageName,
                                                profile.id,
                                            )
                                        }.onFailure {
                                            PartsToast.show(context, R.string.thermal_failed_toast)
                                        }
                                    }
                                    pendingApp = null
                                }
                            },
                        headlineContent = {
                            Text(
                                text  = context.getString(profile.label),
                                style = PartsTokens.Type.rowHeadline,
                                color = if (isSelected) PartsTokens.Colors.dialogSelectedText
                                        else            PartsTokens.Colors.textPrimary,
                            )
                        },
                        trailingContent = {
                            AnimatedContent(
                                targetState    = isSelected,
                                transitionSpec = {
                                    fadeIn(effectsSpec) togetherWith fadeOut(effectsSpec)
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
private fun AppThermalPremiumCard(
    entry:           AppThermalEntry,
    showDivider:     Boolean,
    cardShape:       androidx.compose.ui.graphics.Shape,
    leadingShape:    androidx.compose.ui.graphics.Shape,
    buttonShape:     androidx.compose.ui.graphics.Shape,
    onSelectProfile: () -> Unit,
) {
    val context = LocalContext.current
    val icon    = appIcon(entry.packageName)

    Column {
        ElevatedCard(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = PartsTokens.contentPaddingHorizontal,
                    vertical   = PartsTokens.premiumCardSpacing,
                ),
            shape     = cardShape,
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
                        bitmap             = icon,
                        contentDescription = null,
                        modifier           = Modifier
                            .size(PartsTokens.appIconSize)
                            .clip(leadingShape),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(PartsTokens.appIconSize)
                            .clip(leadingShape)
                            .background(PartsTokens.Colors.appIconFallback),
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
                    shape   = buttonShape,
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = PartsTokens.Colors.premiumCardButton,
                        contentColor   = PartsTokens.Colors.premiumCardButtonContent,
                    ),
                ) {
                    Text(
                        text     = ThermalService.profileLabel(context, entry.profileId),
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
