/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * Targeted launchers for the two CIT calibration screens used in
 * Xiaomi Parts. These fire explicit intents directly at the individual
 * Activity rather than opening the full CIT hub.
 *
 * Intent sources (from logcat):
 *
 *   Fingerprint calibration
 *     cmp=com.jiiov.fingerprint_factorytest/.xiaomi.XiaomiAfterSalesCalibrationActivity
 *     flags=FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_SINGLE_TOP  (LAUNCH_SINGLE_TASK)
 *
 *   Speaker calibration
 *     cmp=com.miui.cit/.auxiliary.CitAudioCaliSelfTest
 *     flags=FLAG_ACTIVITY_NEW_TASK  (LAUNCH_MULTIPLE)
 *
 * Each function returns true on success, false if the Activity is not
 * present on the device. The caller is responsible for showing a Toast.
 */

package com.xiaomi.settings.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log

object CitLauncher {

    private const val TAG = "CitLauncher"

    // ── Fingerprint calibration ──────────────────────────────────────────
    private const val FP_PACKAGE  = "com.jiiov.fingerprint_factorytest"
    private const val FP_ACTIVITY = "com.jiiov.fingerprint_factorytest.xiaomi.XiaomiAfterSalesCalibrationActivity"

    // ── Speaker calibration ──────────────────────────────────────────────
    private const val CIT_PACKAGE        = "com.miui.cit"
    private const val AUDIO_CALI_ACTIVITY = "com.miui.cit.auxiliary.CitAudioCaliSelfTest"

    /**
     * Launches the Jiiov fingerprint after-sales calibration screen.
     * Uses LAUNCH_SINGLE_TASK semantics (NEW_TASK | SINGLE_TOP).
     * @return true if the Activity was found and started.
     */
    fun launchFingerprintCalibration(context: Context): Boolean =
        launchActivity(
            context  = context,
            pkg      = FP_PACKAGE,
            activity = FP_ACTIVITY,
            flags    = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP,
            tag      = "FingerprintCalibration",
        )

    /**
     * Launches the CIT audio (speaker) self-test calibration screen.
     * Uses LAUNCH_MULTIPLE semantics (NEW_TASK only).
     * @return true if the Activity was found and started.
     */
    fun launchSpeakerCalibration(context: Context): Boolean =
        launchActivity(
            context  = context,
            pkg      = CIT_PACKAGE,
            activity = AUDIO_CALI_ACTIVITY,
            flags    = Intent.FLAG_ACTIVITY_NEW_TASK,
            tag      = "SpeakerCalibration",
        )

    // ─────────────────────────────────────────────────────────────────────

    private fun launchActivity(
        context:  Context,
        pkg:      String,
        activity: String,
        flags:    Int,
        tag:      String,
    ): Boolean {
        val intent = Intent().apply {
            component = ComponentName(pkg, activity)
            addFlags(flags)
        }
        // Resolve first so we can log a specific error if absent.
        val resolved = context.packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY,
        )
        if (resolved == null) {
            Log.w(TAG, "$tag not available: $pkg/$activity")
            return false
        }
        return runCatching {
            context.startActivity(intent)
            Log.d(TAG, "$tag launched")
            true
        }.onFailure { e ->
            Log.e(TAG, "$tag launch failed", e)
        }.getOrDefault(false)
    }
}
