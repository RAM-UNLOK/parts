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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.PartsCard
import com.xiaomi.settings.PartsCategory
import com.xiaomi.settings.R
import com.xiaomi.settings.ui.PartsTokens
import com.xiaomi.settings.utils.ChargingMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Immutable
private data class AppEntry(
    val packageName: String,
    val label:       String,
    val icon:        ImageBitmap,
    val state:       ThermalUtils.ThermalState,
)

@Composable
private fun ThermalUtils.ThermalState.dotColor(): Color = when (this) {
    ThermalUtils.ThermalState.DEFAULT         -> MaterialTheme.colorScheme.outline
    ThermalUtils.ThermalState.BENCHMARK       -> MaterialTheme.colorScheme.error
    ThermalUtils.ThermalState.BROWSER         -> MaterialTheme.colorScheme.primary
    ThermalUtils.ThermalState.CAMERA          -> MaterialTheme.colorScheme.tertiary
    ThermalUtils.ThermalState.DIALER          -> MaterialTheme.colorScheme.secondary
    ThermalUtils.ThermalState.GAMING          -> MaterialTheme.colorScheme.error
    ThermalUtils.ThermalState.NAVIGATION      -> MaterialTheme.colorScheme.primary
    ThermalUtils.ThermalState.VIDEO_CALL      -> MaterialTheme.colorScheme.secondary
    ThermalUtils.ThermalState.VIDEO_STREAMING -> MaterialTheme.colorScheme.tertiary
    ThermalUtils.ThermalState.VIDEO           -> MaterialTheme.colorScheme.tertiary
    ThermalUtils.ThermalState.SOCIAL          -> MaterialTheme.colorScheme.primary
    ThermalUtils.ThermalState.MUSIC           -> MaterialTheme.colorScheme.secondary
    ThermalUtils.ThermalState.STREAMING       -> MaterialTheme.colorScheme.tertiary
}

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

/**
 * Charging-override banner shown inside the Thermal screen while a charger
 * is physically connected.
 *
 * Colour role: tertiaryContainer / onTertiaryContainer — battery/charging
 * semantic. No colour logic changes — this is the existing token assignment.
 */
@Composable
private fun ChargingInfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PartsTokens.contentPaddingHorizontal)
            .clip(PartsTokens.cardShape)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
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
            tint               = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier           = Modifier
                .padding(top = 2.dp)
                .size(18.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text  = stringResource(R.string.thermal_charging_active),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Text(
                text  = stringResource(R.string.thermal_charging_info_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

/**
 * Thermal Management screen.
 *
 * Back navigation: no [navigationIcon] back button — predictive-back
 * gesture handles it natively. [onBack] is kept for NavHost popBackStack.
 */
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
            icon    = { Icon(Icons.Filled.RestartAlt, null, tint = MaterialTheme.colorScheme.primary) },
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
        snapAnimationSpec  = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness    = Spring.StiffnessMediumLow,
        ),
        flingAnimationSpec = exponentialDecay(frictionMultiplier = 2f),
    )
    val listState = rememberLazyListState()

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text     = stringResource(R.string.thermal_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(
                            imageVector        = Icons.Filled.RestartAlt,
                            contentDescription = stringResource(R.string.thermal_reset),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
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
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text  = stringResource(R.string.thermal_loading),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.Tune,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier           = Modifier.size(PartsTokens.leadingIconSize),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = stringResource(R.string.thermal_enable),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text  = stringResource(R.string.thermal_enable_summary),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked         = enabled,
                            onCheckedChange = { toggleService(context, thermalUtils, it) { enabled = it } },
                        )
                    }
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        appEntries.forEachIndexed { index, entry ->
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
                            if (index < appEntries.lastIndex) {
                                HorizontalDivider(
                                    modifier  = Modifier.padding(horizontal = PartsTokens.contentPaddingHorizontal),
                                    thickness = 0.5.dp,
                                    color     = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                        }
                    }
                    if (isCharging) {
                        Text(
                            text     = stringResource(R.string.thermal_charging_override_hint),
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(
                                    horizontal = PartsTokens.contentPaddingHorizontal * 2,
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

/**
 * Per-app thermal profile row.
 *
 * Dropdown fix
 * ──────────────
 * The previous version had a double-toggle race condition:
 *   1. ExposedDropdownMenuBox.onExpandedChange receives the new boolean
 *      and sets expanded = it.
 *   2. FilledTonalButton.onClick then also ran `expanded = !expanded`,
 *      flipping it back in the same composition pass.
 * Result: menu opened for one frame then immediately closed.
 *
 * Fix:
 *   - Remove onClick from FilledTonalButton entirely.
 *     menuAnchor(PrimaryNotEditable) already wires the button tap to
 *     ExposedDropdownMenuBox, which calls onExpandedChange. One toggle,
 *     one place.
 *   - onExpandedChange: when charging-locked, show the toast and leave
 *     expanded = false. When unlocked, set expanded = newValue directly
 *     (no manual toggle needed — the Box provides the correct value).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppThermalRow(
    entry:          AppEntry,
    chargingLocked: Boolean,
    onStateChange:  (Int) -> Unit,
) {
    var expanded by remember(entry.packageName) { mutableStateOf(false) }
    val context = LocalContext.current

    // Chevron rotation: 0° closed → 180° open.
    val chevronRotation by animateFloatAsState(
        targetValue   = if (expanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness    = Spring.StiffnessMediumLow,
        ),
        label = "chevron-${entry.packageName}",
    )

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = PartsTokens.contentPaddingHorizontal,
                vertical   = PartsTokens.appRowPaddingVertical,
            ),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PartsTokens.appRowIconSpacing),
    ) {
        Image(
            bitmap             = entry.icon,
            contentDescription = null,
            modifier           = Modifier
                .size(PartsTokens.leadingIconContainerSize)
                .clip(CircleShape),
        )
        Text(
            text     = entry.label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        // ExposedDropdownMenuBox owns the open/close state.
        // onExpandedChange is the single source of truth for `expanded`.
        ExposedDropdownMenuBox(
            expanded         = expanded,
            onExpandedChange = { newValue ->
                if (chargingLocked) {
                    // Show locked-toast; do not open the menu.
                    Toast.makeText(
                        context,
                        R.string.thermal_charging_locked_hint,
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    expanded = newValue
                }
            },
        ) {
            // No onClick on the button — menuAnchor handles taps.
            FilledTonalButton(
                onClick        = { /* handled by menuAnchor */ },
                modifier       = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            ) {
                Icon(
                    imageVector        = entry.state.stateIcon(),
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp),
                    tint               = entry.state.dotColor(),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text     = stringResource(entry.state.label),
                    style    = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector        = Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    modifier           = Modifier
                        .size(16.dp)
                        .rotate(chevronRotation),
                )
            }

            ExposedDropdownMenu(
                expanded         = expanded,
                onDismissRequest = { expanded = false },
                modifier         = Modifier.width(IntrinsicSize.Max),
            ) {
                ThermalUtils.ThermalState.entries.forEach { state ->
                    val isSelected = state == entry.state
                    DropdownMenuItem(
                        text = {
                            Text(
                                text  = stringResource(state.label),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        onClick = {
                            expanded = false
                            onStateChange(state.id)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector        = state.stateIcon(),
                                contentDescription = null,
                                modifier           = Modifier.size(22.dp),
                                tint               = state.dotColor(),
                            )
                        },
                        trailingIcon = if (isSelected) ({
                            Icon(
                                imageVector        = Icons.Filled.Check,
                                contentDescription = null,
                                modifier           = Modifier.size(20.dp),
                                tint               = MaterialTheme.colorScheme.primary,
                            )
                        }) else null,
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical   = PartsTokens.dropdownItemVerticalPadding,
                        ),
                    )
                }
            }
        }
    }
}

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
