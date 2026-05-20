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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.PartsCard
import com.xiaomi.settings.PartsCategory
import com.xiaomi.settings.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class AppEntry(
    val packageName : String,
    val label       : String,
    val icon        : Drawable,
    val state       : ThermalUtils.ThermalState,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalManagementScreen(onBack: () -> Unit) {
    val context      = LocalContext.current
    val thermalUtils = remember { ThermalUtils.getInstance(context) }

    var enabled         by remember { mutableStateOf(thermalUtils.enabled) }
    var appEntries      by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var isLoading       by remember { mutableStateOf(true) }
    var showResetDialog by remember { mutableStateOf(false) }

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
            shape   = MaterialTheme.shapes.extraLarge,
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState: LazyListState = rememberLazyListState()

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text  = stringResource(R.string.thermal_title),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(
                            imageVector        = Icons.Filled.RestartAlt,
                            contentDescription = stringResource(R.string.thermal_reset),
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
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
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            // Loading spinner as a list item so nestedScroll is never broken
            if (isLoading) {
                item(key = "loading") {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
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

            // Service toggle
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
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.Thermostat,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier           = Modifier.size(22.dp),
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
                                style = MaterialTheme.typography.bodySmall,
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

            // Per-app profiles
            if (!enabled) {
                item(key = "disabled-hint") {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp)
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
                item(key = "apps-label") {
                    PartsCategory(stringResource(R.string.thermal_apps_category))
                }
                appEntries.chunked(5).forEachIndexed { idx, chunk ->
                    item(key = "block-$idx") {
                        PartsCard {
                            chunk.forEach { entry ->
                                AppThermalRow(entry) { newStateId ->
                                    runCatching {
                                        thermalUtils.writePackage(entry.packageName, newStateId)
                                        val ns = ThermalUtils.ThermalState.entries
                                            .firstOrNull { it.id == newStateId }
                                            ?: ThermalUtils.ThermalState.DEFAULT
                                        appEntries = appEntries.map {
                                            if (it.packageName == entry.packageName) it.copy(state = ns) else it
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
                                }
                            }
                        }
                    }
                    item(key = "block-spacer-$idx") {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppThermalRow(
    entry        : AppEntry,
    onStateChange: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppIcon(entry.icon, Modifier.size(36.dp).clip(CircleShape))
        Text(
            text     = entry.label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        ExposedDropdownMenuBox(
            expanded         = expanded,
            onExpandedChange = { expanded = it },
            modifier         = Modifier.widthIn(min = 140.dp),
        ) {
            FilterChip(
                selected     = entry.state != ThermalUtils.ThermalState.DEFAULT,
                onClick      = { expanded = true },
                label        = {
                    Text(
                        text     = stringResource(entry.state.label),
                        style    = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier     = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .widthIn(min = 140.dp),
            )
            ExposedDropdownMenu(
                expanded         = expanded,
                onDismissRequest = { expanded = false },
                shape            = MaterialTheme.shapes.extraLarge,
                modifier         = Modifier.width(IntrinsicSize.Max),
            ) {
                ThermalUtils.ThermalState.entries.forEach { state ->
                    DropdownMenuItem(
                        text        = {
                            Text(
                                text     = stringResource(state.label),
                                style    = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                            )
                        },
                        onClick     = { onStateChange(state.id); expanded = false },
                        leadingIcon = if (state == entry.state) ({
                            RadioButton(selected = true, onClick = null, modifier = Modifier.size(18.dp))
                        }) else null,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppIcon(drawable: Drawable, modifier: Modifier = Modifier) {
    val bmp: ImageBitmap = remember(drawable) { drawable.toImageBitmap() }
    Image(bitmap = bmp, contentDescription = null, modifier = modifier)
}

private fun Drawable.toImageBitmap(): ImageBitmap {
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

private fun toggleService(
    context      : Context,
    thermalUtils : ThermalUtils,
    target       : Boolean,
    onResult     : (Boolean) -> Unit,
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
    context      : Context,
    thermalUtils : ThermalUtils,
): List<AppEntry> = withContext(Dispatchers.IO) {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    launcherApps.getActivityList(null, Process.myUserHandle())
        .mapNotNull { info ->
            runCatching {
                AppEntry(
                    packageName = info.applicationInfo.packageName,
                    label       = info.label.toString(),
                    icon        = info.getIcon(0) ?: return@runCatching null,
                    state       = thermalUtils.getStateForPackage(info.applicationInfo.packageName),
                )
            }.getOrNull()
        }
        .sortedBy { it.label.lowercase() }
}
