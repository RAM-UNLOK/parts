/*
 * SPDX-FileCopyrightText: 2020 The LineageOS Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * ThermalService — background service that applies thermal profiles.
 *
 * Charging detection strategy (system-app pattern):
 * ────────────────────────────────────────
 * This app runs as android.uid.system, so it has direct access to
 * BatteryManager.isCharging() without needing a BroadcastReceiver.
 *
 * ChargingMonitor wraps BatteryManager.isCharging() with a low-frequency
 * Handler poller (every 3 s, only while screen is on). This:
 *   • Eliminates the RECEIVER_NOT_EXPORTED / StrictMode issue on AOSP 16.
 *   • Catches USB-only connections that do not fire ACTION_POWER_CONNECTED
 *     on some Xiaomi kernels (MTP / ADB mode).
 *   • Has no race window — state is read synchronously on start().
 *
 * applyProfile() priority (highest → lowest):
 *   1. Charging      → sconfig=27  (Xiaomi charging thermal)
 *   2. Screen off    → sconfig=0   (default, low-power)
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
import android.os.IBinder
import com.xiaomi.settings.utils.ChargingMonitor
import com.xiaomi.settings.utils.dlog

/** Background service — monitors foreground app + charger state to apply thermal profiles. */
class ThermalService : Service() {

    private lateinit var thermalUtils    : ThermalUtils
    private lateinit var chargingMonitor : ChargingMonitor

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
            if (value) {
                // Screen turned on: immediately re-read charging state and
                // restart the poller so USB connections during screen-off
                // are picked up within one poll cycle.
                chargingMonitor.start()
            } else {
                // Screen turned off: stop the poller to avoid waking the
                // CPU every 3 s while the display is dark.
                chargingMonitor.stop()
            }
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

    // Only ACTION_SCREEN_ON/OFF remain here — charging is handled by
    // ChargingMonitor. These two are genuine system-only broadcasts that
    // cannot originate from third-party apps.
    // @Suppress is intentional: system broadcasts must NOT use
    // RECEIVER_NOT_EXPORTED — that flag is for app-to-app broadcasts only.
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> screenOn = false
                Intent.ACTION_SCREEN_ON  -> screenOn = true
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

        // Initialise ChargingMonitor. The onChange callback fires on the main
        // thread whenever the charging state flips, which calls applyProfile().
        chargingMonitor = ChargingMonitor(this) { isCharging ->
            dlog(TAG, "Charging state changed: $isCharging")
            applyProfile()
        }
        chargingMonitor.start()

        // Register for screen on/off only — no battery receivers needed.
        @Suppress("UnspecifiedRegisterReceiverFlag")
        registerReceiver(
            screenReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            },
        )

        runCatching {
            ActivityTaskManager.getService().registerTaskStackListener(taskListener)
        }

        applyProfile()

        return START_STICKY
    }

    override fun onDestroy() {
        dlog(TAG, "onDestroy")
        thermalUtils.setDefaultThermalProfile()
        chargingMonitor.stop()
        unregisterReceiver(screenReceiver)
        runCatching { ActivityTaskManager.getService().unregisterTaskStackListener(taskListener) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Applies the correct thermal sconfig based on current state.
     *
     * Priority (highest wins):
     *   1. Charging      → sconfig=27
     *   2. Screen off    → sconfig=0
     *   3. App known     → per-app sconfig via ThermalUtils
     *   4. No app yet    → skip; TaskStackListener will fire shortly
     */
    private fun applyProfile() {
        runCatching {
            when {
                chargingMonitor.isCharging  -> thermalUtils.setChargingThermalProfile()
                !screenOn                   -> thermalUtils.setDefaultThermalProfile()
                currentApp.isNotEmpty()     -> thermalUtils.setThermalProfile(currentApp)
                else -> dlog(TAG, "applyProfile: skipped — no foreground app yet")
            }
        }.onFailure { e ->
            dlog(TAG, "applyProfile failed: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "ThermalService"
    }
}
