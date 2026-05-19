/*
 * SPDX-FileCopyrightText: 2020 The LineageOS Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * ThermalService — background service that applies thermal profiles.
 *
 * ── Charging detection (correct approach) ────────────────────────────────
 *
 * ACTION_POWER_CONNECTED / ACTION_POWER_DISCONNECTED are NOT in the implicit
 * broadcast exemption list for Android 8+. Static manifest receivers will
 * NEVER receive them. Dynamic receivers (registered at runtime inside a running
 * service) DO receive them fine.
 *
 * However, the most robust approach is ACTION_BATTERY_CHANGED because:
 *   1. It is a STICKY broadcast — the last value is always available even if
 *      the service starts after the charger was connected.
 *   2. EXTRA_PLUGGED tells us the charger TYPE (AC, USB, Wireless).
 *   3. It works on every Android version without exemption concerns.
 *
 * Strategy used here:
 *   • On service start, read initial charging state from the sticky
 *     ACTION_BATTERY_CHANGED intent via registerReceiver(null, ...).
 *   • Register a dynamic receiver for ACTION_BATTERY_CHANGED at runtime.
 *     On each broadcast, check EXTRA_PLUGGED > 0 to determine charger state.
 *   • On charger connect: write sconfig=27, show Toast.
 *   • On charger disconnect: resume per-app profile, show Toast.
 *   • All sysfs writes are wrapped in runCatching; failures show a Toast.
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
import android.widget.Toast
import com.xiaomi.settings.R
import com.xiaomi.settings.utils.dlog

/** Background service — monitors foreground app + charger state to apply thermal profiles. */
class ThermalService : Service() {

    private lateinit var thermalUtils: ThermalUtils
    private val mainHandler = Handler(Looper.getMainLooper())

    // Track foreground app
    private var currentApp = ""
        set(value) {
            if (field == value) return
            field = value
            dlog(TAG, "Top app: $value")
            applyProfile()
        }

    // Track screen on/off
    private var screenOn = true
        set(value) {
            if (field == value) return
            field = value
            dlog(TAG, "Screen: $value")
            applyProfile()
        }

    /**
     * True when any charger is connected (AC, USB, or Wireless).
     * Detected via ACTION_BATTERY_CHANGED + EXTRA_PLUGGED — the only
     * reliable way on Android 8+ without a foreground service exemption.
     */
    private var isCharging = false
        set(value) {
            if (field == value) return
            field = value
            dlog(TAG, "Charging: $value")
            applyProfile()
            showChargerToast(value)
        }

    // ── Task stack listener ───────────────────────────────────────────────

    private val taskListener = object : TaskStackListener() {
        override fun onTaskStackChanged() {
            runCatching {
                val focusedTask = ActivityTaskManager.getService().focusedRootTaskInfo
                focusedTask?.topActivity?.let { currentApp = it.packageName }
            }
        }
    }

    // ── Dynamic broadcast receiver ────────────────────────────────────────

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                // Screen state
                Intent.ACTION_SCREEN_OFF -> screenOn = false
                Intent.ACTION_SCREEN_ON  -> screenOn = true

                // Charging state — EXTRA_PLUGGED is 0 when on battery,
                // non-zero (AC=1, USB=2, Wireless=4) when charging.
                Intent.ACTION_BATTERY_CHANGED -> {
                    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                    isCharging = plugged != 0 && plugged != -1
                }
            }
        }
    }

    // ── Service lifecycle ─────────────────────────────────────────────────

    override fun onCreate() {
        dlog(TAG, "onCreate")
        thermalUtils = ThermalUtils.getInstance(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        dlog(TAG, "onStartCommand")

        // ── Read INITIAL charging state from sticky BATTERY_CHANGED ───────
        // registerReceiver(null, filter) returns the last sticky broadcast
        // immediately without registering a persistent receiver.
        val stickIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = stickIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val initialCharging = plugged != 0 && plugged != -1
        dlog(TAG, "Initial charging state from sticky: $initialCharging (plugged=$plugged)")
        // Set directly to avoid triggering the setter side-effects before
        // the receiver is registered — we'll call applyProfile() below.
        val field = ThermalService::class.java.getDeclaredField("isCharging")
        field.isAccessible = true
        field.setBoolean(this, initialCharging)

        // ── Register dynamic receiver ─────────────────────────────────────
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        registerReceiver(broadcastReceiver, filter)

        // ── Register task listener ────────────────────────────────────────
        runCatching {
            ActivityTaskManager.getService().registerTaskStackListener(taskListener)
        }

        // Apply initial profile (respects initialCharging state set above)
        applyProfile()
        if (initialCharging) showChargerToast(true)

        return START_STICKY
    }

    override fun onDestroy() {
        dlog(TAG, "onDestroy")
        unregisterReceiver(broadcastReceiver)
        runCatching { ActivityTaskManager.getService().unregisterTaskStackListener(taskListener) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Profile application ───────────────────────────────────────────────

    /**
     * Decides which thermal config to write.
     *
     * Priority (highest first):
     *   1. Charging  → thermal-charge.conf  (sconfig 27)
     *   2. Screen off → thermal-normal.conf  (sconfig  0)
     *   3. Per-app   → ThermalUtils resolves from package map
     */
    private fun applyProfile() {
        runCatching {
            when {
                isCharging -> {
                    val ok = thermalUtils.setChargingThermalProfile()
                    if (!ok) showToast(getString(R.string.thermal_apply_failed))
                }
                !screenOn  -> thermalUtils.setDefaultThermalProfile()
                else       -> thermalUtils.setThermalProfile(currentApp)
            }
        }.onFailure { e ->
            dlog(TAG, "applyProfile failed: ${e.message}")
            showToast(getString(R.string.thermal_apply_failed))
        }
    }

    // ── Toast helpers ─────────────────────────────────────────────────────

    /**
     * Posts a charger-connect or charger-disconnect Toast on the main thread.
     * Includes the active thermal config name so the user knows what changed.
     */
    private fun showChargerToast(pluggedIn: Boolean) {
        val msg = if (pluggedIn)
            getString(R.string.thermal_charging_toast_connected)
        else
            getString(R.string.thermal_charging_toast_disconnected)
        showToast(msg)
    }

    /** Posts any Toast on the main thread; safe to call from any thread. */
    private fun showToast(message: String) {
        mainHandler.post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val TAG = "ThermalService"
    }
}
