/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * ChargingMonitor — reliable charging-state observer for system apps.
 *
 * Why not ACTION_POWER_CONNECTED / ACTION_BATTERY_CHANGED?
 * ─────────────────────────────────────────────────
 * Dynamic BroadcastReceivers for battery events introduce:
 *   • A race window between registration and first delivery.
 *   • The RECEIVER_NOT_EXPORTED / StrictMode problem on AOSP 16 because
 *     ACTION_POWER_CONNECTED is a system broadcast and the flag is wrong for it.
 *   • Missed events on Xiaomi kernels that change the plugged value without
 *     firing ACTION_POWER_CONNECTED (USB debug / MTP mode).
 *
 * This class uses BatteryManager.isCharging() directly — the correct API for
 * a system app running as android.uid.system. It supplements the synchronous
 * read with a low-frequency Handler poller so USB-only connections that produce
 * no broadcast are caught within POLL_INTERVAL_MS.
 *
 * Usage:
 *   val monitor = ChargingMonitor(context) { isCharging -> applyProfile() }
 *   monitor.start()   // call from onStartCommand / screen-on
 *   monitor.stop()    // call when screen turns off or service is stopping
 *   monitor.isCharging  // current state, always readable synchronously
 */

package com.xiaomi.settings.utils

import android.content.Context
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper

class ChargingMonitor(
    private val context : Context,
    private val onChange: (Boolean) -> Unit,
) {
    private val batteryManager: BatteryManager =
        context.getSystemService(BatteryManager::class.java)

    private val handler = Handler(Looper.getMainLooper())

    /** Current charging state. Always reflects the last polled value. */
    var isCharging: Boolean = batteryManager.isCharging
        private set(value) {
            if (field == value) return
            field = value
            onChange(value)
        }

    private val pollRunnable = object : Runnable {
        override fun run() {
            isCharging = batteryManager.isCharging
            handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    /**
     * Start (or restart) the poller and do an immediate synchronous read.
     * Safe to call multiple times — removes any pending callbacks first.
     */
    fun start() {
        handler.removeCallbacks(pollRunnable)
        isCharging = batteryManager.isCharging   // immediate read, no wait
        handler.postDelayed(pollRunnable, POLL_INTERVAL_MS)
    }

    /** Stop the poller. State remains readable via [isCharging]. */
    fun stop() {
        handler.removeCallbacks(pollRunnable)
    }

    companion object {
        /**
         * Poll every 3 seconds while the screen is on.
         * Balances responsiveness (charging banner appears within 3 s of plug)
         * against battery/CPU cost (BatteryManager.isCharging() is a cheap
         * Binder call that reads a cached value — no I/O).
         */
        private const val POLL_INTERVAL_MS = 3_000L
    }
}
