/*
 * SPDX-FileCopyrightText: 2020 The LineageOS Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * ThermalService — background service that applies thermal profiles.
 *
 * Charging detection:
 *   Initial state: ACTION_BATTERY_CHANGED sticky broadcast read once via
 *     registerReceiver(null, filter) — reliable on all API levels.
 *   Live updates: ACTION_POWER_CONNECTED / ACTION_POWER_DISCONNECTED —
 *     these are the correct intents for plug/unplug events. Do NOT use
 *     ACTION_BATTERY_CHANGED for live dynamic receiver updates; it is only
 *     guaranteed to deliver the last sticky value at registration, not on
 *     subsequent changes.
 *
 * applyProfile() priority (highest → lowest):
 *   1. Charging  → sconfig=27  (cannot be overridden by any app)
 *   2. Screen off → sconfig=0  (default, low-power)
 *   3. Foreground app known → per-app sconfig from ThermalUtils
 *   4. No foreground app yet → skip (TaskStackListener hasn't fired)
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
import com.xiaomi.settings.R
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
     * Backing field — allows setting initial charging state at startup without
     * triggering side-effects (applyProfile) before receivers are registered.
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
                Intent.ACTION_SCREEN_OFF        -> screenOn    = false
                Intent.ACTION_SCREEN_ON         -> screenOn    = true
                Intent.ACTION_POWER_CONNECTED   -> isCharging  = true
                Intent.ACTION_POWER_DISCONNECTED -> isCharging = false
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

        // Read INITIAL charging state from sticky broadcast — reliable on all APIs.
        val stickyIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = stickyIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        // Write directly to backing field to skip setter side-effects during init.
        _isCharging = plugged != 0
        dlog(TAG, "Initial charging state: $_isCharging (plugged=$plugged)")

        // Use POWER_CONNECTED/DISCONNECTED for live updates — ACTION_BATTERY_CHANGED
        // is a sticky broadcast and does NOT reliably fire on re-plug/unplug via a
        // dynamic receiver on modern Android.
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        // RECEIVER_NOT_EXPORTED required on API 33+ for non-system-protected broadcasts.
        registerReceiver(broadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)

        runCatching {
            ActivityTaskManager.getService().registerTaskStackListener(taskListener)
        }

        applyProfile()

        return START_STICKY
    }

    override fun onDestroy() {
        dlog(TAG, "onDestroy")
        unregisterReceiver(broadcastReceiver)
        runCatching { ActivityTaskManager.getService().unregisterTaskStackListener(taskListener) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Applies the correct thermal profile based on current state.
     * Priority order (highest wins):
     *   1. Charging      → sconfig=27, cannot be overridden by any app switch
     *   2. Screen off    → sconfig=0  (default low-power)
     *   3. App known     → per-app sconfig via ThermalUtils
     *   4. No app yet    → skip (TaskStackListener fires shortly after boot)
     */
    private fun applyProfile() {
        runCatching {
            when {
                _isCharging             -> {
                    val ok = thermalUtils.setChargingThermalProfile()
                    if (!ok) dlog(TAG, "setChargingThermalProfile failed")
                }
                !screenOn               -> thermalUtils.setDefaultThermalProfile()
                currentApp.isNotEmpty() -> thermalUtils.setThermalProfile(currentApp)
                else                    -> dlog(TAG, "applyProfile: skipped — no foreground app yet")
            }
        }.onFailure { e ->
            dlog(TAG, "applyProfile failed: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "ThermalService"
    }
}
