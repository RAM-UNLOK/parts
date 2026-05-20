/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * ChargingMonitor — reliable charging-state + battery-detail observer
 *                    for system apps running as android.uid.system.
 *
 * ┌───────────────────────────────────────────────────────────────────────────────┐
 * │ Why not use BroadcastReceiver for battery events?                       │
 * ├───────────────────────────────────────────────────────────────────────────────┤
 * │ A non-system app (e.g. BatteryGuru) MUST use broadcast receivers        │
 * │ because it lacks direct BatteryManager access and must be woken from    │
 * │ the background. That forces it to:                                      │
 * │   • Declare android.permission.BATTERY_STATS (needs ADB/root grant)     │
 * │   • Use static manifest receivers for POWER_CONNECTED/DISCONNECTED      │
 * │   • Declare a custom signature permission to workaround the Android 8+  │
 * │     dynamic receiver export requirement (DYNAMIC_RECEIVER_NOT_EXPORTED) │
 * │   • Run a foreground service + WAKE_LOCK to stay alive                  │
 * │                                                                          │
 * │ XiaomiParts runs as android.uid.system. That means:                     │
 * │   • BatteryManager.isCharging() is available directly — no permission  │
 * │   • BATTERY_STATS is implicitly granted by the system UID               │
 * │   • registerReceiver(null, filter) sticky read works without any flag   │
 * │   • No foreground service or WAKE_LOCK needed                           │
 * │   • No DYNAMIC_RECEIVER_NOT_EXPORTED workaround needed                  │
 * └───────────────────────────────────────────────────────────────────────────────┘
 *
 * What we borrow from the BatteryGuru pattern:
 *   • The sticky ACTION_BATTERY_CHANGED one-shot read to get a rich
 *     BatteryInfo snapshot (level, temperature, plug type) all at once —
 *     the same technique as BatteryGuru’s BatteryInfoService.
 *   • Plug-type awareness (AC / USB / WIRELESS / DOCK) so ThermalService
 *     can apply different sconfigss per charger type in the future.
 *
 * Strategy:
 *   1. On start(): one-shot sticky read via readBatteryInfo() — populates
 *      all fields atomically with zero race window.
 *   2. Poll loop (every POLL_INTERVAL_MS while screen is on) re-reads only
 *      BatteryManager.isCharging() (cheap cached Binder call) and does a
 *      full sticky re-read only when the charging state flips.
 *   3. stop() removes the poller; state remains readable synchronously.
 *
 * Usage:
 *   val monitor = ChargingMonitor(context) { info -> applyProfile(info) }
 *   monitor.start()          // call from onStartCommand / screen-on
 *   monitor.stop()           // call when screen turns off or service stops
 *   monitor.batteryInfo      // last known BatteryInfo snapshot
 *   monitor.batteryInfo.isCharging
 *   monitor.batteryInfo.plugType
 */

package com.xiaomi.settings.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper

class ChargingMonitor(
    private val context : Context,
    private val onChange: (BatteryInfo) -> Unit,
) {
    private val batteryManager: BatteryManager =
        context.getSystemService(BatteryManager::class.java)

    private val handler = Handler(Looper.getMainLooper())

    /**
     * Plug type reported by the platform.
     * Mirrors [BatteryManager] BATTERY_PLUGGED_* constants.
     */
    enum class PlugType {
        /** Powered via standard AC/DC wall charger. */
        AC,
        /** Powered via USB port (any speed). */
        USB,
        /** Powered via Qi or similar wireless pad. */
        WIRELESS,
        /** Powered via a dock connector. */
        DOCK,
        /** Not charging — running on battery. */
        NONE;

        companion object {
            fun from(plugged: Int): PlugType = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC       -> AC
                BatteryManager.BATTERY_PLUGGED_USB      -> USB
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> WIRELESS
                BatteryManager.BATTERY_PLUGGED_DOCK     -> DOCK
                else                                     -> NONE
            }
        }
    }

    /**
     * A snapshot of battery state read from the ACTION_BATTERY_CHANGED
     * sticky broadcast.
     *
     * All fields are read atomically from a single broadcast intent so
     * they are always internally consistent.
     *
     * @param isCharging   true if the device is currently charging.
     * @param plugType     how the device is being charged (or [PlugType.NONE]).
     * @param level        battery level in the range [0, 100].
     * @param tempTenthsC  battery temperature in tenths of a degree Celsius
     *                     (e.g. 285 → 28.5 °C). Divide by 10 for display.
     */
    data class BatteryInfo(
        val isCharging  : Boolean,
        val plugType    : PlugType,
        val level       : Int,
        val tempTenthsC : Int,
    ) {
        companion object {
            /** Neutral sentinel — used before the first real read. */
            val UNKNOWN = BatteryInfo(
                isCharging  = false,
                plugType    = PlugType.NONE,
                level       = -1,
                tempTenthsC = 0,
            )
        }
    }

    /** Most-recently read battery information. Updated on every state change. */
    var batteryInfo: BatteryInfo = BatteryInfo.UNKNOWN
        private set

    /**
     * Convenience accessor — avoids boilerplate in callers that only care
     * about charging state.
     */
    val isCharging: Boolean get() = batteryInfo.isCharging

    // ────────────────────────────────────────────────────────────────────────
    // Poll loop
    // ────────────────────────────────────────────────────────────────────────

    private val pollRunnable = object : Runnable {
        override fun run() {
            // Cheap fast path: BatteryManager.isCharging() reads a cached
            // in-memory value from BatteryService — no I/O, no full sticky read.
            val nowCharging = batteryManager.isCharging
            if (nowCharging != batteryInfo.isCharging) {
                // State flipped: do a full sticky read so level / plugType /
                // temp are also updated, then notify the caller.
                val info = readBatteryInfo()
                batteryInfo = info
                onChange(info)
            }
            handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Public API
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Start (or restart) the monitor.
     *
     * Performs an immediate full sticky read so [batteryInfo] is populated
     * before this call returns — no race window, no wait for first broadcast.
     * Safe to call multiple times; removes pending poll callbacks first.
     */
    fun start() {
        handler.removeCallbacks(pollRunnable)
        val info = readBatteryInfo()
        val changed = info.isCharging != batteryInfo.isCharging
        batteryInfo = info
        if (changed) onChange(info)
        handler.postDelayed(pollRunnable, POLL_INTERVAL_MS)
    }

    /**
     * Stop the poll loop. [batteryInfo] remains readable as the last known
     * state. Call [start] to resume monitoring.
     */
    fun stop() {
        handler.removeCallbacks(pollRunnable)
    }

    // ────────────────────────────────────────────────────────────────────────
    // Sticky broadcast read — the BatteryGuru pattern adapted for system apps
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Reads all battery fields in one atomic sticky-broadcast call.
     *
     * [Context.registerReceiver] with a null receiver returns the last
     * ACTION_BATTERY_CHANGED sticky intent synchronously without registering
     * a persistent listener. This is the same technique used by BatteryGuru’s
     * BatteryInfoService, but legal here without any permission grant because
     * XiaomiParts runs as android.uid.system (BATTERY_STATS is implicit).
     *
     * Returns [BatteryInfo.UNKNOWN] if the sticky broadcast is not yet
     * available (should not happen after boot, but handled defensively).
     */
    private fun readBatteryInfo(): BatteryInfo {
        val intent: Intent? = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        )
        if (intent == null) return BatteryInfo.UNKNOWN

        val plugged     = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,      0)
        val rawLevel    = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,        -1)
        val scale       = intent.getIntExtra(BatteryManager.EXTRA_SCALE,        100)
        val temp        = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,  0)
        val statusInt   = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                              BatteryManager.BATTERY_STATUS_UNKNOWN)

        val isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING ||
                         statusInt == BatteryManager.BATTERY_STATUS_FULL
        val level = if (scale > 0 && rawLevel >= 0) (rawLevel * 100 / scale) else rawLevel

        return BatteryInfo(
            isCharging  = isCharging,
            plugType    = PlugType.from(plugged),
            level       = level,
            tempTenthsC = temp,
        )
    }

    companion object {
        /**
         * Poll interval while screen is on.
         *
         * 3 seconds balances responsiveness (charging sconfig applied within
         * 3 s of plug-in) against CPU cost. BatteryManager.isCharging() is a
         * trivially cheap cached Binder call; the more expensive sticky read
         * only fires when the state actually changes.
         */
        private const val POLL_INTERVAL_MS = 3_000L
    }
}
