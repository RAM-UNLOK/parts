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
 *   4. No foreground app yet → skip (TaskStackListener hasn't fired)
 *
 * Toast deduplication:
 *   A single `currentToast` field holds the last Toast issued by this service.
 *   Before showing any new toast, currentToast?.cancel() is called so the
 *   previous one is dismissed immediately. This prevents stacked/queued toasts
 *   from the charging edge-detection path.
 */

package com.xiaomi.settings.thermal

import android.app.ActivityTaskManager
import android.app.Service
import android.app.TaskStackListener
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.preference.PreferenceManager
import com.xiaomi.settings.R
import com.xiaomi.settings.utils.ChargingMonitor
import com.xiaomi.settings.utils.PartsToast
import com.xiaomi.settings.utils.dlog

/** Background service — monitors foreground app + charger state to apply thermal profiles. */
class ThermalService : Service() {

    private lateinit var thermalUtils: ThermalUtils
    private lateinit var chargingMonitor: ChargingMonitor
    private lateinit var mainHandler: Handler

    /**
     * Holds the last Toast shown by this service so we can cancel it before
     * showing a new one. Prevents stacked / queued toasts when charging events
     * fire in rapid succession (e.g. charger wiggle, USB handshake retries).
     * Always accessed on the main thread via mainHandler.post{}.
     */

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
                // Screen on: re-read charging state and restart the poller.
                // Catches USB connections made while the screen was off.
                chargingMonitor.start()
            } else {
                // Screen off: stop poller — no need to wake the CPU in the dark.
                chargingMonitor.stop()
            }
            applyProfile()
        }

    /**
     * Tracks the charging state as of the last applyProfile() call.
     *
     * Initialised to null so the very first applyProfile() always writes
     * the correct sconfig from a clean state but does NOT show a toast
     * (the user didn't just plug in — the service simply started).
     */
    private var wasCharging: Boolean? = null

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
    // system broadcasts and causes a StrictMode warning on AOSP 16.
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> screenOn = false
                Intent.ACTION_SCREEN_ON  -> screenOn = true
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        dlog(TAG, "onCreate")
        // Must be created on or after super.onCreate() so the main looper
        // is guaranteed to be attached to this process.
        mainHandler  = Handler(Looper.getMainLooper())
        thermalUtils = ThermalUtils.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        dlog(TAG, "onStartCommand")

        chargingMonitor = ChargingMonitor(this) { info ->
            dlog(TAG, "Charging changed: ${info.isCharging} via ${info.plugType} level=${info.level}% temp=${info.tempTenthsC / 10.0}°C")
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
        // Pass final = true so the ChargingMonitor HandlerThread is quit
        // cleanly and does not leak after the service is destroyed.
        chargingMonitor.stop(final = true)
        unregisterReceiver(screenReceiver)
        runCatching { ActivityTaskManager.getService().unregisterTaskStackListener(taskListener) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Applies the correct thermal sconfig based on current state.
     *
     * Priority (highest wins):
     *   1. Charging      → sconfig=27  + toast on plug-in / plug-out edge
     *   2. Screen off    → sconfig=0
     *   3. App known     → per-app sconfig via ThermalUtils
     *   4. No app yet    → skip; TaskStackListener will fire shortly
     *
     * Toast logic:
     *   wasCharging is null only on the very first call (service start).
     *   We write the correct sconfig but skip the toast so the user doesn't
     *   see a notification just because the service restarted while already
     *   on charge.
     *   On every subsequent call a toast fires only when isCharging flips
     *   (true→false or false→true). currentToast?.cancel() dismisses any
     *   queued toast before the new one shows, preventing visible stacking.
     */
    private fun applyProfile() {
        val isCharging   = chargingMonitor.isCharging
        val prevCharging = wasCharging

        runCatching {
            when {
                isCharging              -> thermalUtils.setChargingThermalProfile()
                !screenOn               -> thermalUtils.setDefaultThermalProfile()
                currentApp.isNotEmpty() -> thermalUtils.setThermalProfile(currentApp)
                else -> dlog(TAG, "applyProfile: skipped — no foreground app yet")
            }
        }.onFailure { e ->
            dlog(TAG, "applyProfile failed: ${e.message}")
        }

        // Update edge-tracking state AFTER applying, so wasCharging always
        // reflects what was just acted on.
        wasCharging = isCharging

        // Show a toast only on a real plug-in / plug-out transition.
        // prevCharging == null → first call (service start) — skip toast.
        if (prevCharging == null) return
        if (isCharging == prevCharging) return

        val msgRes = if (isCharging) {
            R.string.thermal_charging_toast_connected
        } else {
            R.string.thermal_charging_toast_disconnected
        }

        mainHandler.post { PartsToast.show(applicationContext, msgRes) }
    }

    companion object {
        private const val TAG                 = "ThermalService"
        private const val PREF_GLOBAL_PROFILE = "thermal_global_profile"

        fun profiles(): List<ThermalUtils.ThermalState> = ThermalUtils.ThermalState.entries

        fun profileLabel(context: Context, profileId: Int): String =
            ThermalUtils.ThermalState.entries
                .firstOrNull { it.id == profileId }
                ?.let { context.getString(it.label) }
                .orEmpty()

        fun getGlobalProfile(context: Context): Int =
            PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_GLOBAL_PROFILE, ThermalUtils.ThermalState.DEFAULT.id)

        fun setGlobalProfile(context: Context, profileId: Int) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_GLOBAL_PROFILE, profileId)
                .apply()
        }

        fun getAppList(context: Context): List<AppThermalEntry> {
            val utils = ThermalUtils.getInstance(context)
            val pm    = context.packageManager
            return pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .asSequence()
                .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                .map { appInfo ->
                    AppThermalEntry(
                        packageName = appInfo.packageName,
                        label       = pm.getApplicationLabel(appInfo).toString(),
                        profileId   = utils.getStateForPackage(appInfo.packageName).id,
                    )
                }
                .sortedBy { it.label.lowercase() }
                .toList()
        }

        fun setAppProfile(context: Context, packageName: String, profileId: Int) {
            ThermalUtils.getInstance(context).writePackage(packageName, profileId)
        }
    }
}
