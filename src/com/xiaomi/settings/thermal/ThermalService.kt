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
                chargingMonitor.start()
            } else {
                chargingMonitor.stop()
            }
            applyProfile()
        }

    private var wasCharging: Boolean? = null

    private val taskListener = object : TaskStackListener() {
        override fun onTaskStackChanged() {
            runCatching {
                val focusedTask = ActivityTaskManager.getService().focusedRootTaskInfo
                focusedTask?.topActivity?.let { currentApp = it.packageName }
            }
        }
    }

    @Suppress("UnspecifiedRegisterReceiverFlag")
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
        chargingMonitor.stop(final = true)
        unregisterReceiver(screenReceiver)
        runCatching { ActivityTaskManager.getService().unregisterTaskStackListener(taskListener) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

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

        wasCharging = isCharging

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

        fun profiles(): List<ThermalUtils.ThermalState> =
            ThermalUtils.ThermalState.entries.filter { it.userSelectable }

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

        /**
         * Returns only apps that have a saved non-DEFAULT override.
         * Auto-classified apps are not shown — the list is for manual overrides only.
         */
        fun getAppList(context: Context): List<AppThermalEntry> {
            val utils  = ThermalUtils.getInstance(context)
            val pm     = context.packageManager
            val prefs  = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.all.keys
                .filter { it.startsWith(ThermalUtils.THERMAL_PACKAGE_PREFIX) }
                .mapNotNull { key ->
                    val pkg       = key.removePrefix(ThermalUtils.THERMAL_PACKAGE_PREFIX)
                    val profileId = prefs.getInt(key, ThermalUtils.ThermalState.DEFAULT.id)
                    if (profileId == ThermalUtils.ThermalState.DEFAULT.id) return@mapNotNull null
                    val appInfo   = runCatching {
                        pm.getApplicationInfo(pkg, 0)
                    }.getOrNull() ?: return@mapNotNull null
                    AppThermalEntry(
                        packageName = pkg,
                        label       = pm.getApplicationLabel(appInfo).toString(),
                        profileId   = profileId,
                    )
                }
                .sortedBy { it.label.lowercase() }
        }

        fun setAppProfile(context: Context, packageName: String, profileId: Int) {
            ThermalUtils.getInstance(context).writePackage(packageName, profileId)
        }
    }
}
