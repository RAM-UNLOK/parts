/*
 * SPDX-FileCopyrightText: 2020 The LineageOS Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
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
        private const val TAG = "ThermalService"

        fun profiles(): List<ThermalUtils.ThermalState> =
            ThermalUtils.ThermalState.entries.filter { it.userSelectable }

        fun profileLabel(context: Context, profileId: Int): String =
            ThermalUtils.ThermalState.entries
                .firstOrNull { it.id == profileId }
                ?.let { context.getString(it.label) }
                .orEmpty()

        /**
         * Returns apps that have an explicit non-DEFAULT override saved in prefs.
         * Uses sharedPrefs.contains() via ThermalUtils.getStateForPackage logic —
         * but here we just scan keys directly since DEFAULT overrides are never
         * stored (writePackage removes the key for DEFAULT).
         */
        fun getAppList(context: Context): List<AppThermalEntry> {
            val pm    = context.packageManager
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.all.keys
                .filter { it.startsWith(ThermalUtils.THERMAL_PACKAGE_PREFIX) }
                .mapNotNull { key ->
                    val pkg       = key.removePrefix(ThermalUtils.THERMAL_PACKAGE_PREFIX)
                    val profileId = prefs.getInt(key, ThermalUtils.ThermalState.DEFAULT.id)
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
