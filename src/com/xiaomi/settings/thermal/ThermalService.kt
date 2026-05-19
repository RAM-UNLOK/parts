/*
 * SPDX-FileCopyrightText: 2020 The LineageOS Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * ThermalService — background service that applies thermal profiles.
 *
 * Charging detection:
 *   ACTION_BATTERY_CHANGED is sticky — initial state is read immediately via
 *   registerReceiver(null, filter) without registering a persistent receiver.
 *   Live updates use a dynamically registered receiver.
 *   EXTRA_PLUGGED > 0 means any charger (AC=1, USB=2, Wireless=4) is connected.
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
     * Backing field — allows setting initial value at startup without triggering
     * side-effects (applyProfile + toast) before receivers are registered.
     */
    private var _isCharging = false

    private var isCharging: Boolean
        get() = _isCharging
        set(value) {
            if (_isCharging == value) return
            _isCharging = value
            dlog(TAG, "Charging: $value")
            applyProfile()
            showChargerToast(value)
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
                Intent.ACTION_SCREEN_OFF      -> screenOn = false
                Intent.ACTION_SCREEN_ON       -> screenOn = true
                Intent.ACTION_BATTERY_CHANGED -> {
                    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                    isCharging = plugged != 0
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

        // Read INITIAL charging state from sticky broadcast — no reflection needed.
        val stickyIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = stickyIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        // Write directly to backing field to skip setter side-effects during init.
        _isCharging = plugged != 0
        dlog(TAG, "Initial charging state: $_isCharging (plugged=$plugged)")

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        registerReceiver(broadcastReceiver, filter)

        runCatching {
            ActivityTaskManager.getService().registerTaskStackListener(taskListener)
        }

        applyProfile()
        if (_isCharging) showChargerToast(true)

        return START_STICKY
    }

    override fun onDestroy() {
        dlog(TAG, "onDestroy")
        unregisterReceiver(broadcastReceiver)
        runCatching { ActivityTaskManager.getService().unregisterTaskStackListener(taskListener) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun applyProfile() {
        runCatching {
            when {
                _isCharging -> {
                    val ok = thermalUtils.setChargingThermalProfile()
                    if (!ok) showToast(getString(R.string.thermal_apply_failed))
                }
                !screenOn   -> thermalUtils.setDefaultThermalProfile()
                else        -> thermalUtils.setThermalProfile(currentApp)
            }
        }.onFailure { e ->
            dlog(TAG, "applyProfile failed: ${e.message}")
            showToast(getString(R.string.thermal_apply_failed))
        }
    }

    private fun showChargerToast(pluggedIn: Boolean) {
        val msg = if (pluggedIn)
            getString(R.string.thermal_charging_toast_connected)
        else
            getString(R.string.thermal_charging_toast_disconnected)
        showToast(msg)
    }

    private fun showToast(message: String) {
        mainHandler.post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val TAG = "ThermalService"
    }
}
