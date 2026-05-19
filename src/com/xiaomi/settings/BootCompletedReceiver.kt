/*
 * SPDX-FileCopyrightText: 2018 The LineageOS Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * Refactored: GameBar boot receiver removed.
 * All three active subsystems are restored in onLockedBootCompleted
 * so they are available before the user unlocks the device.
 */

package com.xiaomi.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.UserHandle
import android.util.Log
import android.view.Display
import android.view.Display.HdrCapabilities
import com.xiaomi.settings.display.ColorService
import com.xiaomi.settings.thermal.ThermalUtils
import com.xiaomi.settings.touchsampling.TouchSamplingService

/** Restores all XiaomiParts feature states after device (re)boot. */
class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
        private val DEBUG = Log.isLoggable(TAG, Log.DEBUG)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (DEBUG) Log.d(TAG, "Boot intent received: ${intent.action}")
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED        -> onBootCompleted(context)
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> onLockedBootCompleted(context)
        }
    }

    /** Called after full user unlock — reserved for future use. */
    private fun onBootCompleted(context: Context) {
        // Nothing extra needed post-unlock; all services start at locked-boot.
    }

    /**
     * Called early at locked-boot — starts all background services so that
     * display, thermal, and touch settings are applied before the user sees
     * the lock screen.
     */
    private fun onLockedBootCompleted(context: Context) {
        // ── Display colour mode ──────────────────────────────────────────
        context.startServiceAsUser(
            Intent(context, ColorService::class.java),
            UserHandle.CURRENT,
        )

        // ── Touch boost / high touch sampling rate ───────────────────────
        context.startServiceAsUser(
            Intent(context, TouchSamplingService::class.java),
            UserHandle.CURRENT,
        )

        // ── Per-app thermal profiles ─────────────────────────────────────
        ThermalUtils.getInstance(context).startService()

        // ── Force-enable all HDR types (Dolby Vision, HDR10, HLG, HDR10+) ──
        // This is required on garnet to ensure Dolby Vision content renders
        // correctly regardless of whether the panel negotiated it at init.
        runCatching {
            context.getSystemService(DisplayManager::class.java)
                ?.overrideHdrTypes(
                    Display.DEFAULT_DISPLAY,
                    intArrayOf(
                        HdrCapabilities.HDR_TYPE_DOLBY_VISION,
                        HdrCapabilities.HDR_TYPE_HDR10,
                        HdrCapabilities.HDR_TYPE_HLG,
                        HdrCapabilities.HDR_TYPE_HDR10_PLUS,
                    ),
                )
        }.onFailure { e ->
            Log.e(TAG, "Failed to override HDR types", e)
        }
    }
}
