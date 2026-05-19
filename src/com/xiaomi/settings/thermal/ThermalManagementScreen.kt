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
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import com.xiaomi.settings.R
import com.xiaomi.settings.SettingsBlock
import com.xiaomi.settings.SettingsCategoryLabel
import com.xiaomi.settings.SettingsDivider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class AppEntry(
    val packageName : String,
    val label       : String,
    val icon        : Drawable,
    val state       : ThermalUtils.ThermalState,
)

/** FIX: proper data class replaces fragile Triple for AnimatedContent state diffing. */
private data class ThermalScreenState(
    val isLoading      : Boolean,
    val serviceEnabled : Boolean,
    val entries        : List<AppEntry>,
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
            icon    = { Icon(Icons.Filled.RestartAlt, null) },
            title   = { Text(stringResource(R.string.thermal_reset)) },
            text    = { Text(stringResource(R.string.thermal_reset_confirm)) },
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

    Scaffold(
        modifier       = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.thermal_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Filled.RestartAlt, stringResource(R.string.thermal_reset))
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
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
            SettingsCategoryLabel(stringResource(R.string.thermal_title))

            SettingsBlock {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Switch) {
                            toggleService(context, thermalUtils, !enabled) { enabled = it }
                        }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // FIX: CircleShape — consistent with SettingsRow
                    Box(
                        modifier         = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color    = MaterialTheme.colorScheme.secondaryContainer,
                        ) {}
                        Icon(
                            imageVector        = Icons.Filled.Thermostat,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier           = Modifier.size(24.dp),
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

            // FIX: proper data class instead of Triple for correct Compose state diffing
            val screenState = ThermalScreenState(
                isLoading      = isLoading,
                serviceEnabled = enabled,
                entries        = appEntries,
            )

            AnimatedContent(
                targetState    = screenState,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                label          = "thermal-app-list",
                modifier       = Modifier.fillMaxSize(),
            ) { state ->
                when {
                    state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
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

                    !state.serviceEnabled -> Box(
                        modifier         = Modifier.fillMaxSize().alpha(0.45f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text  = stringResource(R.string.thermal_disabled_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier       = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp),
                        ) {
                            item {
                                SettingsCategoryLabel(stringResource(R.string.thermal_apps_category))
                            }

                            val chunks = state.entries.chunked(5)
                            chunks.forEachIndexed { chunkIdx, chunk ->
                                item(key = "block-$chunkIdx") {
                                    SettingsBlock(
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        chunk.forEachIndexed { rowIdx, entry ->
                                            val globalIdx = chunkIdx * 5 + rowIdx
                                            AppThermalRow(
                                                entry         = entry,
                                                onStateChange = { newStateId ->
                                                    runCatching {
                                                        thermalUtils.writePackage(entry.packageName, newStateId)
                                                        // FIX: entries instead of deprecated values()
                                                        val newState = ThermalUtils.ThermalState.entries
                                                            .firstOrNull { it.id == newStateId }
                                                            ?: ThermalUtils.ThermalState.DEFAULT
                                                        appEntries = appEntries.toMutableList().also {
                                                            it[globalIdx] = entry.copy(state = newState)
                                                        }
                                                        Toast.makeText(
                                                            context,
                                                            context.getString(
                                                                R.string.thermal_profile_applied,
                                                                entry.label,
                                                                context.getString(newState.label),
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
                                            if (rowIdx < chunk.lastIndex) {
                                                SettingsDivider()
                                            }
                                        }
                                    }
                                }
                            }
                        }
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppIcon(
            drawable = entry.icon,
            modifier = Modifier.size(36.dp).clip(CircleShape),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = entry.label,
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }

        ExposedDropdownMenuBox(
            expanded         = expanded,
            onExpandedChange = { expanded = it },
        ) {
            FilterChip(
                selected = entry.state != ThermalUtils.ThermalState.DEFAULT,
                onClick  = { expanded = true },
                label    = {
                    Text(
                        text     = stringResource(entry.state.label),
                        style    = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier     = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            )

            ExposedDropdownMenu(
                expanded         = expanded,
                onDismissRequest = { expanded = false },
                shape            = MaterialTheme.shapes.extraLarge,
            ) {
                // FIX: entries instead of deprecated values()
                ThermalUtils.ThermalState.entries.forEach { state ->
                    DropdownMenuItem(
                        text        = { Text(stringResource(state.label), style = MaterialTheme.typography.bodyMedium) },
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
