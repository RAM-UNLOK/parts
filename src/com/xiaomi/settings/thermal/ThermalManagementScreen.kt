/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.thermal

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Process
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties
import com.xiaomi.settings.PartsCard
import com.xiaomi.settings.PartsCategory
import com.xiaomi.settings.PartsRow
import com.xiaomi.settings.R
import com.xiaomi.settings.ui.PartsTokens
import com.xiaomi.settings.utils.ChargingMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ────────────────────────────────────────────────────────
// Data
// ────────────────────────────────────────────────────────

@Immutable
private data class AppEntry(
    val packageName: String,
    val label:       String,
    val icon:        ImageBitmap,
    val state:       ThermalUtils.ThermalState,
)

// ────────────────────────────────────────────────────────
// State helpers
// ────────────────────────────────────────────────────────

private fun ThermalUtils.ThermalState.stateIcon(): ImageVector = when (this) {
    ThermalUtils.ThermalState.DEFAULT         -> Icons.Rounded.Tune
    ThermalUtils.ThermalState.BENCHMARK       -> Icons.Filled.BugReport
    ThermalUtils.ThermalState.BROWSER         -> Icons.Filled.Web
    ThermalUtils.ThermalState.CAMERA          -> Icons.Filled.CameraAlt
    ThermalUtils.ThermalState.DIALER          -> Icons.Filled.Phone
    ThermalUtils.ThermalState.GAMING          -> Icons.Filled.SportsEsports
    ThermalUtils.ThermalState.NAVIGATION      -> Icons.Filled.Map
    ThermalUtils.ThermalState.VIDEO_CALL      -> Icons.Filled.VideoCall
    ThermalUtils.ThermalState.VIDEO_STREAMING -> Icons.Filled.LiveTv
    ThermalUtils.ThermalState.VIDEO           -> Icons.Filled.PlayCircle
    ThermalUtils.ThermalState.SOCIAL          -> Icons.Filled.People
    ThermalUtils.ThermalState.MUSIC           -> Icons.AutoMirrored.Filled.VolumeUp
    ThermalUtils.ThermalState.STREAMING       -> Icons.Filled.Stream
}

// ────────────────────────────────────────────────────────
// Charging banner
// ────────────────────────────────────────────────────────

@Composable
private fun ChargingInfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PartsTokens.contentPaddingHorizontal)
            .clip(PartsTokens.cardShape)
            .background(PartsTokens.Colors.bannerContainer)
            .padding(
                horizontal = PartsTokens.contentPaddingHorizontal,
                vertical   = PartsTokens.rowPaddingVertical,
            ),
        horizontalArrangement = Arrangement.spacedBy(PartsTokens.rowElementSpacing),
        verticalAlignment     = Alignment.Top,
    ) {
        Icon(
            imageVector        = Icons.Filled.BatteryChargingFull,
            contentDescription = null,
            tint               = PartsTokens.Colors.bannerContent,
            modifier           = Modifier
                .padding(top = 2.dp)
                .size(PartsTokens.bannerIconSize),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text  = stringResource(R.string.thermal_charging_active),
                style = MaterialTheme.typography.labelLarge,
                color = PartsTokens.Colors.bannerContent,
            )
            Text(
                text  = stringResource(R.string.thermal_charging_info_body),
                style = MaterialTheme.typography.bodySmall,
                color = PartsTokens.Colors.bannerContent,
            )
        }
    }
}

// ────────────────────────────────────────────────────────
// M3 profile picker dialog
// ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThermalProfileDialog(
    entry:         AppEntry,
    onDismiss:     () -> Unit,
    onStateChange: (Int) -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(
            dismissOnBackPress      = true,
            dismissOnClickOutside   = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        AnimatedVisibility(
            visible = true,
            enter   = scaleIn(
                animationSpec   = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMediumLow,
                ),
                initialScale    = 0.85f,
                transformOrigin = TransformOrigin.Center,
            ) + fadeIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness    = Spring.StiffnessMedium,
                ),
            ),
            exit    = scaleOut(
                animationSpec   = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness    = Spring.StiffnessMedium,
                ),
                targetScale     = 0.92f,
                transformOrigin = TransformOrigin.Center,
            ) + fadeOut(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness    = Spring.StiffnessMedium,
                ),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .clip(PartsTokens.cardShape)
                    .background(PartsTokens.Colors.dialogSurface)
                    .padding(vertical = PartsTokens.dialogOuterPaddingVertical),
            ) {

                // ── Header ───────────────────────────────────────────
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(
                            start  = PartsTokens.sectionHeaderStartPadding,
                            end    = PartsTokens.sectionHeaderStartPadding,
                            top    = PartsTokens.dialogHeaderTopPadding,
                            bottom = PartsTokens.dialogHeaderBottomPadding,
                        ),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PartsTokens.dialogHeaderIconSpacing),
                ) {
                    Image(
                        bitmap             = entry.icon,
                        contentDescription = null,
                        modifier           = Modifier
                            .size(PartsTokens.dialogHeaderIconSize)
                            .clip(CircleShape),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text     = entry.label,
                            style    = MaterialTheme.typography.titleMedium,
                            color    = PartsTokens.Colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text  = stringResource(R.string.thermal_profile_picker_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = PartsTokens.Colors.textSecondary,
                        )
                    }
                }
                // No HorizontalDivider — M3 uses spatial grouping, not rules.

                // ── Profile list ─────────────────────────────────────
                LazyColumn(
                    modifier       = Modifier
                        .fillMaxWidth()
                        .heightIn(max = PartsTokens.dialogListMaxHeight),
                    contentPadding = PaddingValues(vertical = PartsTokens.dialogListContentPadding),
                ) {
                    items(
                        items = ThermalUtils.ThermalState.entries,
                        key   = { it.id },
                    ) { state ->
                        val isSelected = state == entry.state

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PartsTokens.dialogRowHorizontalInset)
                                .clip(PartsTokens.dialogSelectionShape)
                                .background(
                                    if (isSelected) PartsTokens.Colors.dialogSelectedBackground
                                    else            Color.Transparent,
                                )
                                .clickable(role = Role.RadioButton) {
                                    onStateChange(state.id)
                                    onDismiss()
                                }
                                .padding(
                                    horizontal = PartsTokens.contentPaddingHorizontal,
                                    vertical   = PartsTokens.dialogRowPaddingVertical,
                                ),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PartsTokens.dialogHeaderIconSpacing),
                        ) {
                            Box(
                                modifier         = Modifier
                                    .size(PartsTokens.dialogRowIconSize)
                                    .clip(CircleShape)
                                    .background(PartsTokens.Colors.iconContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector        = state.stateIcon(),
                                    contentDescription = null,
                                    tint               = if (state == ThermalUtils.ThermalState.DEFAULT)
                                        PartsTokens.Colors.textSecondary
                                    else
                                        PartsTokens.Colors.iconContent,
                                    modifier           = Modifier.size(PartsTokens.dialogRowIconInnerSize),
                                )
                            }

                            Text(
                                text     = stringResource(state.label),
                                style    = MaterialTheme.typography.bodyLarge,
                                color    = if (isSelected)
                                    PartsTokens.Colors.dialogSelectedText
                                else
                                    PartsTokens.Colors.textPrimary,
                                modifier = Modifier.weight(1f),
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector        = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint               = PartsTokens.Colors.dialogSelectedText,
                                    modifier           = Modifier.size(PartsTokens.trailingIconSize),
                                )
                            }
                        }
                    }
                }
                // No HorizontalDivider above actions.

                // ── Actions ───────────────────────────────────────────
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(
                            end = PartsTokens.contentPaddingHorizontal,
                            top = PartsTokens.dialogActionsTopPadding,
                        ),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text  = stringResource(R.string.cancel),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────
// Main screen
// ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalManagementScreen(onBack: () -> Unit) {
    val context      = LocalContext.current
    val thermalUtils = remember { ThermalUtils.getInstance(context) }

    var appEntries      by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var isLoading       by remember { mutableStateOf(true) }
    var enabled         by remember { mutableStateOf(thermalUtils.enabled) }
    var showResetDialog by remember { mutableStateOf(false) }

    var isCharging by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        val monitor = ChargingMonitor(context) { info ->
            isCharging = info.isCharging
        }
        isCharging = monitor.isCharging
        monitor.start()
        onDispose { monitor.stop(final = true) }
    }

    LaunchedEffect(Unit) {
        appEntries = loadApps(context, thermalUtils)
        isLoading  = false
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon    = { Icon(Icons.Filled.RestartAlt, null, tint = PartsTokens.Colors.textCategory) },
            title   = { Text(stringResource(R.string.thermal_reset), style = MaterialTheme.typography.headlineSmall) },
            text    = { Text(stringResource(R.string.thermal_reset_confirm), style = MaterialTheme.typography.bodyMedium) },
            shape   = PartsTokens.cardShape,
            confirmButton = {
                TextButton(onClick = {
                    runCatching {
                        thermalUtils.resetProfiles()
                        appEntries = appEntries.map {
                            it.copy(state = thermalUtils.getStateForPackage(it.packageName))
                        }
                        Toast.makeText(context, R.string.thermal_reset_success, Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(context, R.string.thermal_reset_failed, Toast.LENGTH_SHORT).show()
                    }
                    showResetDialog = false
                }) { Text(stringResource(R.string.thermal_reset_confirm_btn)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec  = PartsTokens.MotionSpringEnter,
        flingAnimationSpec = exponentialDecay(frictionMultiplier = 2f),
    )
    val listState = rememberLazyListState()

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
            state          = listState,
            modifier       = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = PartsTokens.listBottomPadding),
        ) {
            if (isLoading) {
                item(key = "loading") {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(top = PartsTokens.loadingTopPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(strokeWidth = 3.dp)
                            Spacer(Modifier.height(PartsTokens.loadingSpinnerLabelSpacing))
                            Text(
                                text  = stringResource(R.string.thermal_loading),
                                style = MaterialTheme.typography.bodySmall,
                                color = PartsTokens.Colors.textSecondary,
                            )
                        }
                    }
                }
                return@LazyColumn
            }

            item(key = "toggle-label") {
                PartsCategory(stringResource(R.string.thermal_enable))
            }
            item(key = "toggle-card") {
                PartsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Switch) {
                                toggleService(context, thermalUtils, !enabled) { enabled = it }
                            }
                            .padding(
                                horizontal = PartsTokens.contentPaddingHorizontal,
                                vertical   = PartsTokens.rowPaddingVertical,
                            ),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PartsTokens.rowElementSpacing),
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(PartsTokens.leadingIconContainerSize)
                                .clip(CircleShape)
                                .background(PartsTokens.Colors.iconContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.Tune,
                                contentDescription = null,
                                tint               = PartsTokens.Colors.iconContent,
                                modifier           = Modifier.size(PartsTokens.leadingIconSize),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = stringResource(R.string.thermal_enable),
                                style = MaterialTheme.typography.bodyLarge,
                                color = PartsTokens.Colors.textPrimary,
                            )
                            Text(
                                text  = stringResource(R.string.thermal_enable_summary),
                                style = MaterialTheme.typography.bodyMedium,
                                color = PartsTokens.Colors.textSecondary,
                            )
                        }
                        Switch(
                            checked         = enabled,
                            onCheckedChange = { toggleService(context, thermalUtils, it) { enabled = it } },
                        )
                    }
                }
            }

            item(key = "reset-label") {
                PartsCategory(stringResource(R.string.thermal_reset))
            }
            item(key = "reset-card") {
                PartsCard {
                    PartsRow(
                        icon        = Icons.Filled.RestartAlt,
                        title       = stringResource(R.string.thermal_reset),
                        summary     = stringResource(R.string.thermal_reset_confirm),
                        onClick     = { showResetDialog = true },
                        showDivider = false,
                        trailing    = {},
                    )
                }
            }

            if (!enabled) {
                item(key = "disabled-hint") {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(top = PartsTokens.disabledHintTopPadding)
                            .alpha(0.4f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text  = stringResource(R.string.thermal_disabled_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = PartsTokens.Colors.textSecondary,
                        )
                    }
                }
            } else {
                if (isCharging) {
                    item(key = "charging-info") {
                        Spacer(Modifier.height(PartsTokens.categoryTopPadding))
                        ChargingInfoBanner()
                    }
                }

                item(key = "apps-label") {
                    PartsCategory(stringResource(R.string.thermal_apps_category))
                }

                item(key = "apps-card") {
                    PartsCard(
                        modifier = Modifier.alpha(if (isCharging) 0.38f else 1f),
                    ) {
                        appEntries.forEach { entry ->
                            AppThermalRow(
                                entry          = entry,
                                chargingLocked = isCharging,
                                onStateChange  = onStateChange@{ newStateId ->
                                    if (isCharging) {
                                        Toast.makeText(
                                            context,
                                            R.string.thermal_charging_locked_hint,
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                        return@onStateChange
                                    }
                                    runCatching {
                                        thermalUtils.writePackage(entry.packageName, newStateId)
                                        val ns = ThermalUtils.ThermalState.entries
                                            .firstOrNull { it.id == newStateId }
                                            ?: ThermalUtils.ThermalState.DEFAULT
                                        appEntries = appEntries.map {
                                            if (it.packageName == entry.packageName) it.copy(state = ns)
                                            else it
                                        }
                                        Toast.makeText(
                                            context,
                                            context.getString(
                                                R.string.thermal_profile_applied,
                                                entry.label,
                                                context.getString(ns.label),
                                            ),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.thermal_profile_failed, entry.label),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                },
                            )
                        }
                    }
                    if (isCharging) {
                        Text(
                            text     = stringResource(R.string.thermal_charging_override_hint),
                            style    = MaterialTheme.typography.bodySmall,
                            color    = PartsTokens.Colors.textSecondary,
                            modifier = Modifier
                                .padding(
                                    horizontal = PartsTokens.chargingHintIndent,
                                    vertical   = PartsTokens.categoryTopPadding,
                                )
                                .alpha(0.6f),
                        )
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────
// App row
// ────────────────────────────────────────────────────────

@Composable
private fun AppThermalRow(
    entry:          AppEntry,
    chargingLocked: Boolean,
    onStateChange:  (Int) -> Unit,
) {
    var showDialog by remember(entry.packageName) { mutableStateOf(false) }
    val context = LocalContext.current

    ListItem(
        modifier = Modifier.clickable {
            if (chargingLocked) {
                Toast.makeText(
                    context,
                    R.string.thermal_charging_locked_hint,
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
                showDialog = true
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        leadingContent = {
            Image(
                bitmap             = entry.icon,
                contentDescription = null,
                modifier           = Modifier
                    .size(PartsTokens.dialogHeaderIconSize)
                    .clip(CircleShape),
            )
        },
        headlineContent = {
            Text(
                text     = entry.label,
                style    = MaterialTheme.typography.bodyLarge,
                color    = PartsTokens.Colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text  = stringResource(entry.state.label),
                style = MaterialTheme.typography.bodyMedium,
                color = if (entry.state == ThermalUtils.ThermalState.DEFAULT)
                    PartsTokens.Colors.textSecondary
                else
                    MaterialTheme.colorScheme.primary,
            )
        },
    )

    if (showDialog) {
        ThermalProfileDialog(
            entry         = entry,
            onDismiss     = { showDialog = false },
            onStateChange = onStateChange,
        )
    }
}

// ────────────────────────────────────────────────────────
// Service helpers
// ────────────────────────────────────────────────────────

private fun toggleService(
    context:      Context,
    thermalUtils: ThermalUtils,
    target:       Boolean,
    onResult:     (Boolean) -> Unit,
) {
    runCatching {
        thermalUtils.enabled = target
        onResult(target)
        val msg = if (target) R.string.thermal_service_started else R.string.thermal_service_stopped
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(context, R.string.thermal_toggle_failed, Toast.LENGTH_SHORT).show()
    }
}

private suspend fun loadApps(
    context:      Context,
    thermalUtils: ThermalUtils,
): List<AppEntry> = withContext(Dispatchers.IO) {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    launcherApps.getActivityList(null, Process.myUserHandle())
        .mapNotNull { info ->
            runCatching {
                val drawable = info.getIcon(0) ?: return@runCatching null
                AppEntry(
                    packageName = info.applicationInfo.packageName,
                    label       = info.label.toString(),
                    icon        = drawable.toBitmapSafe(),
                    state       = thermalUtils.getStateForPackage(info.applicationInfo.packageName),
                )
            }.getOrNull()
        }
        .sortedBy { it.label.lowercase() }
}

private fun Drawable.toBitmapSafe(): ImageBitmap {
    val bmp = Bitmap.createBitmap(
        intrinsicWidth.coerceAtLeast(1),
        intrinsicHeight.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888,
    )
    val canvas = Canvas(bmp)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bmp.asImageBitmap()
}
