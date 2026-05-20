/*
 * SPDX-FileCopyrightText: 2020 The LineageOS Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * ThermalService — background service that applies thermal profiles.
 *
 * Charging detection (system-app pattern):
 * ───────────────────────────────────
 * Uses ChargingMonitor which wraps BatteryManager + the ACTION_BATTERY_CHANGED
 * sticky broadcast. No dynamic BroadcastReceiver for battery events: eliminates
 * the RECEIVER_NOT_EXPORTED / StrictMode issue and the Xiaomi MTP/ADB missed-
 * event problem.
 *
 * ChargingMonitor.BatteryInfo exposes plugType (AC / USB / WIRELESS / DOCK)
 * for future per-charger-type sconfig differentiation.
 *
 * applyProfile() priority (highest → lowest):
 *   1. Charging      → sconfig=27  (Xiaomi charging thermal)
 *   2. Screen off    → sconfig=0   (default, low-power)
 *   3. Foreground app known → per-app sconfig from ThermalUtils
 *   4. No foreground app yet → skip (TaskStackListener hasn’t fired)
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
                // Screen on: immediately re-read charging state and restart
                // the poller. Catches USB connections made during screen-off.
                chargingMonitor.start()
            } else {
                // Screen off: stop poller — no reason to wake CPU in the dark.
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

    // Only ACTION_SCREEN_ON/OFF remain here. Charging is handled by
    // ChargingMonitor with no BroadcastReceiver at all.
    // @Suppress is intentional: these are genuine system-only broadcasts that
    // cannot originate from third-party apps. NOT_EXPORTED is incorrect for
    // system broadcasts and causes StrictMode on AOSP 16.
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

        // Initialise ChargingMonitor. The onChange lambda receives a full
        // BatteryInfo snapshot (isCharging + plugType + level + temp).
        // Currently we branch only on isCharging; plugType is available for
        // future per-charger-type thermal configuration.
        chargingMonitor = ChargingMonitor(this) { info ->
            dlog(TAG, "Charging changed: ${info.isCharging} via ${info.plugType} " +
                      "level=${info.level}% temp=${info.tempTenthsC / 10.0}°C")
            applyProfile()
        }
        chargingMonitor.start()

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
     *
     * Future extension: branch on chargingMonitor.batteryInfo.plugType to
     * apply different sconfigss for wireless vs wired charging.
     */
    private fun applyProfile() {
        runCatching {
            when {
                chargingMonitor.isCharging   -> thermalUtils.setChargingThermalProfile()
                !screenOn                    -> thermalUtils.setDefaultThermalProfile()
                currentApp.isNotEmpty()      -> thermalUtils.setThermalProfile(currentApp)
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
