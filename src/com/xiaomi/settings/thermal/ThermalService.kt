/*
 * SPDX-FileCopyrightText: 2020 The LineageOS Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * ThermalService — background service that applies thermal profiles.
 *
 * Charging detection strategy:
 *   The only 100 % reliable way to detect charger state across all Xiaomi
 *   kernels and Android versions is to re-read BatteryManager.EXTRA_PLUGGED
 *   from the ACTION_BATTERY_CHANGED sticky broadcast on every receipt.
 *
 *   ACTION_POWER_CONNECTED / ACTION_POWER_DISCONNECTED are NOT sent reliably
 *   on all devices when connecting USB in file-transfer or debug mode (the
 *   plugged value can change without a POWER_CONNECTED event). Therefore we:
 *     1. Register for ACTION_BATTERY_CHANGED (dynamic, not just sticky read)
 *        so we get re-notified on any battery-state change.
 *     2. Also listen to POWER_CONNECTED / POWER_DISCONNECTED as fast-path
 *        signals to apply the profile immediately on plug events.
 *   Both paths update _isCharging via the same setter, which is idempotent.
 *
 * applyProfile() priority (highest → lowest):
 *   1. Charging      → sconfig=27  (Xiaomi charging thermal)
 *   2. Screen off    → sconfig=0   (default, low-power)
 *   3. Foreground app known → per-app sconfig from ThermalUtils
 *   4. No foreground app yet → skip (TaskStackListener hasn't fired)
 *
 * RECEIVER_NOT_EXPORTED must NOT be used here: ACTION_SCREEN_ON/OFF,
 * ACTION_POWER_CONNECTED/DISCONNECTED, and ACTION_BATTERY_CHANGED are all
 * system-protected broadcasts delivered exclusively by the OS. Passing
 * RECEIVER_NOT_EXPORTED to registerReceiver() for system broadcasts triggers
 * a StrictMode warning on AOSP 16 and can silently drop the registration on
 * some Xiaomi kernels.
 */

package com.xiaomi.settings.thermal

import android.app.ActivityTaskManager
import android.app.Service
import android.app.TaskStackListener
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.xiaomi.settings.utils.dlog

/** Background service — monitors foreground app + charger state to apply thermal profiles. */
class ThermalService : Service() {

    private lateinit var thermalUtils: ThermalUtils
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentApp = ""
        set(value) {
            if (field == value) return
            field = value
            dlog(TAG, "Top app: $value")
            applyProfile()
        }

    private var screenOn = true
        set(value) {
            if (field == value) return
            field = value
            dlog(TAG, "Screen: $value")
            applyProfile()
        }

    /**
     * Backing field — allows setting the initial charging state at startup
     * without triggering applyProfile() before receivers are registered.
     * All runtime changes go through the [isCharging] property setter.
     */
    private var _isCharging = false

    private var isCharging: Boolean
        get() = _isCharging
        set(value) {
            if (_isCharging == value) return
            _isCharging = value
            dlog(TAG, "Charging: $value")
            applyProfile()
        }

    private val taskListener = object : TaskStackListener() {
        override fun onTaskStackChanged() {
            runCatching {
                val focusedTask = ActivityTaskManager.getService().focusedRootTaskInfo
                focusedTask?.topActivity?.let { currentApp = it.packageName }
            }
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF         -> screenOn = false
                Intent.ACTION_SCREEN_ON          -> screenOn = true

                // Fast-path: explicit plug/unplug events.
                Intent.ACTION_POWER_CONNECTED    -> isCharging = true
                Intent.ACTION_POWER_DISCONNECTED -> isCharging = false

                // Reliable path: re-read the actual plugged value from the
                // battery changed broadcast. This catches USB connections
                // that don't fire POWER_CONNECTED (e.g. USB debug / MTP mode
                // on some Xiaomi kernels where charging begins slightly later).
                Intent.ACTION_BATTERY_CHANGED    -> {
                    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                    isCharging  = plugged != 0
                }
            }
        }
    }

    override fun onCreate() {
        dlog(TAG, "onCreate")
        thermalUtils = ThermalUtils.getInstance(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        dlog(TAG, "onStartCommand")

        // Read initial charging state from the sticky broadcast synchronously.
        val stickyIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged      = stickyIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        // Write directly to the backing field to skip applyProfile() during init.
        _isCharging = plugged != 0
        dlog(TAG, "Initial charging state: $_isCharging (plugged=$plugged)")

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            // Register for battery changes so USB-only connections (where
            // POWER_CONNECTED may not fire immediately) are also detected.
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        // System-protected broadcasts must be registered WITHOUT
        // RECEIVER_NOT_EXPORTED — the OS sends them to all registered
        // receivers regardless of the export flag, and NOT_EXPORTED causes
        // a StrictMode violation on AOSP 16 for system broadcast actions.
        @Suppress("UnspecifiedRegisterReceiverFlag")
        registerReceiver(broadcastReceiver, filter)

        runCatching {
            ActivityTaskManager.getService().registerTaskStackListener(taskListener)
        }

        // Apply the correct profile now that initial state is known.
        applyProfile()

        return START_STICKY
    }

    override fun onDestroy() {
        dlog(TAG, "onDestroy")
        // Reset to sconfig=0 (default/normal) so no charging or per-app
        // profile lingers after the service is stopped or killed.
        thermalUtils.setDefaultThermalProfile()
        unregisterReceiver(broadcastReceiver)
        runCatching { ActivityTaskManager.getService().unregisterTaskStackListener(taskListener) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Applies the correct thermal sconfig based on current state.
     *
     * Priority (highest wins):
     *   1. Charging      → sconfig=27 (Xiaomi charging thermal config)
     *   2. Screen off    → sconfig=0  (default, low-power)
     *   3. App known     → per-app sconfig via ThermalUtils
     *   4. No app yet    → skip; TaskStackListener will fire shortly
     */
    private fun applyProfile() {
        runCatching {
            when {
                isCharging             -> thermalUtils.setChargingThermalProfile()
                !screenOn              -> thermalUtils.setDefaultThermalProfile()
                currentApp.isNotEmpty() -> thermalUtils.setThermalProfile(currentApp)
                else                   -> dlog(TAG, "applyProfile: skipped — no foreground app yet")
            }
        }.onFailure { e ->
            dlog(TAG, "applyProfile failed: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "ThermalService"
    }
}
