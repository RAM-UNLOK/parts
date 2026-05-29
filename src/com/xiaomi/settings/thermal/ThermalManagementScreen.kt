/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.thermal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.xiaomi.settings.PartsCategory
import com.xiaomi.settings.R
import kotlinx.coroutines.Dispatchers
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

private fun readIsCharging(context: Context): Boolean {
    val sticky = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    return (sticky?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0) != 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalManagementScreen(onBack: () -> Unit) {
    val context      = LocalContext.current
    val thermalUtils = remember { ThermalUtils.getInstance(context) }

    var thermalEnabled  by remember { mutableStateOf(thermalUtils.enabled) }
    var appList         by remember { mutableStateOf<List<AppThermalEntry>>(emptyList()) }
    var isCharging      by remember { mutableStateOf(readIsCharging(context)) }
    var showResetDialog by remember { mutableStateOf(false) }
    var pendingApp      by remember { mutableStateOf<AppThermalEntry?>(null) }

    val controlsEnabled = thermalEnabled && !isCharging

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                isCharging = (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)) != 0
            }
        }
        @Suppress("UnspecifiedRegisterReceiverFlag")
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }

    LaunchedEffect(Unit) {
        appList = withContext(Dispatchers.IO) {
            runCatching { ThermalService.getAppList(context) }.getOrDefault(emptyList())
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val profiles = remember { ThermalService.profiles().filter { it.userSelectable } }

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
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
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item(key = "charging-banner") {
                AnimatedVisibility(
                    modifier = Modifier.animateItem(),
                    visible  = isCharging,
                    enter    = expandVertically() + fadeIn(),
                    exit     = shrinkVertically() + fadeOut(),
                ) { ChargingBanner() }
            }

            item(key = "enable-card") {
                Card(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .padding(top = 12.dp),
                    shape  = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                ) {
                    ListItem(
                        modifier = Modifier
                            .alpha(if (isCharging) 0.38f else 1f)
                            .clickable(enabled = !isCharging, role = Role.Switch) {
                                thermalEnabled = !thermalEnabled
                                thermalUtils.enabled = thermalEnabled
                            },
                        headlineContent = {
                            Text(
                                text  = stringResource(R.string.thermal_enable_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked         = thermalEnabled,
                                enabled         = !isCharging,
                                onCheckedChange = { checked ->
                                    thermalEnabled = checked
                                    thermalUtils.enabled = checked
                                },
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Unspecified),
                    )
                }
            }

            item(key = "info-card") {
                // tertiaryContainer is the Monet accent-complement zone — produces a
                // clearly tinted warm/cool surface in dark mode unlike secondaryContainer
                // which resolves near-neutral in most Monet dark themes.
                Card(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape  = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                ) {
                    Row(
                        modifier              = Modifier.padding(20.dp),
                        verticalAlignment     = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Info,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier           = Modifier.padding(top = 2.dp),
                        )
                        Text(
                            text  = stringResource(R.string.thermal_enable_summary),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
            }

            item(key = "reset-card") {
                Card(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape  = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    ListItem(
                        modifier = Modifier
                            .alpha(if (controlsEnabled) 1f else 0.38f)
                            .clickable(enabled = controlsEnabled, role = Role.Button) {
                                showResetDialog = true
                            },
                        headlineContent = {
                            Text(
                                text  = stringResource(R.string.thermal_reset_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        },
                        supportingContent = {
                            Text(
                                text  = stringResource(R.string.thermal_reset_summary),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector        = Icons.Filled.RestartAlt,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Unspecified),
                    )
                }
            }

            item(key = "per-app-label") {
                PartsCategory(
                    label    = stringResource(R.string.thermal_per_app_label),
                    modifier = Modifier.animateItem(),
                )
            }

            if (appList.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text     = stringResource(R.string.thermal_no_apps),
                        style    = MaterialTheme.typography.bodyLarge,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 32.dp, vertical = 16.dp),
                    )
                }
            } else {
                items(
                    items = appList,
                    key   = { it.packageName },
                ) { entry ->
                    AppThermalCard(
                        entry    = entry,
                        enabled  = controlsEnabled,
                        onClick  = { if (controlsEnabled) pendingApp = entry },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }

    pendingApp?.let { app ->
        var tempProfile by remember(app.packageName) { mutableIntStateOf(app.profileId) }
        ProfilePickerDialog(
            title      = app.label,
            profiles   = profiles,
            selectedId = tempProfile,
            onSelect   = { tempProfile = it },
            onDismiss  = { pendingApp = null },
            onConfirm  = {
                runCatching {
                    ThermalService.setAppProfile(context, app.packageName, tempProfile)
                    appList = appList.map {
                        if (it.packageName == app.packageName) it.copy(profileId = tempProfile) else it
                    }
                }
                pendingApp = null
            },
        )
    }

    if (showResetDialog) {
        Dialog(onDismissRequest = { showResetDialog = false }) {
            Card(
                shape  = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
            ) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text  = stringResource(R.string.thermal_reset_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text  = stringResource(R.string.thermal_reset_confirm_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    ) {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text(stringResource(android.R.string.cancel))
                        }
                        FilledTonalButton(
                            onClick = {
                                runCatching {
                                    ThermalUtils.getInstance(context).resetProfiles()
                                    appList = appList.map {
                                        it.copy(profileId = ThermalUtils.ThermalState.DEFAULT.id)
                                    }
                                }
                                showResetDialog = false
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor   = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                        ) {
                            Text(stringResource(R.string.thermal_reset_confirm))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppThermalCard(
    entry:    AppThermalEntry,
    enabled:  Boolean,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context   = LocalContext.current
    val icon      = appIcon(entry.packageName)
    val isDefault = entry.profileId == ThermalUtils.ThermalState.DEFAULT.id

    val cardColor by animateColorAsState(
        targetValue   = if (!isDefault && enabled)
            MaterialTheme.colorScheme.tertiaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "cardColor",
    )
    val profileLabelColor by animateColorAsState(
        targetValue   = if (!isDefault && enabled)
            MaterialTheme.colorScheme.onTertiaryContainer
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "profileLabelColor",
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .alpha(if (enabled) 1f else 0.38f),
        shape   = MaterialTheme.shapes.large,
        colors  = CardDefaults.cardColors(containerColor = cardColor),
        onClick = onClick,
        enabled = enabled,
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier              = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (icon != null) {
                    Image(
                        bitmap             = icon,
                        contentDescription = null,
                        modifier           = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text     = entry.label,
                        style    = MaterialTheme.typography.titleMedium,
                        color    = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text     = entry.packageName,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            VerticalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            Box(
                modifier         = Modifier
                    .width(96.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text     = ThermalService.profileLabel(context, entry.profileId),
                    style    = MaterialTheme.typography.labelLarge,
                    color    = profileLabelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ProfilePickerDialog(
    title:      String,
    profiles:   List<ThermalUtils.ThermalState>,
    selectedId: Int,
    onSelect:   (Int) -> Unit,
    onDismiss:  () -> Unit,
    onConfirm:  () -> Unit,
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Text(
                    text     = title,
                    style    = MaterialTheme.typography.headlineSmall,
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                ) {
                    profiles.forEach { profile ->
                        val selected = profile.id == selectedId
                        val rowBg by animateColorAsState(
                            targetValue   = if (selected)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else
                                Color.Transparent,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label         = "profileRowBg",
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.large)
                                .background(rowBg)
                                .clickable(role = Role.RadioButton) { onSelect(profile.id) }
                                .padding(vertical = 14.dp, horizontal = 24.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            RadioButton(selected = selected, onClick = null)
                            Text(
                                text  = context.getString(profile.label),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selected)
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(android.R.string.cancel))
                    }
                    TextButton(onClick = onConfirm) {
                        Text(stringResource(R.string.thermal_apply))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChargingBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .padding(top = 12.dp),
        shape  = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector        = Icons.Filled.BatteryChargingFull,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier           = Modifier.size(24.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text  = stringResource(R.string.thermal_charging_toast_connected),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text  = stringResource(R.string.thermal_charging_banner_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}
