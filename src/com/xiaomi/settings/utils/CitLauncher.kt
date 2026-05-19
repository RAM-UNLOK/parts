/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * Utility to launch the Xiaomi CIT (Component/Hardware Integration Test)
 * application. CIT is a factory test tool preinstalled on Xiaomi devices
 * that allows manual hardware validation (vibrator, sensors, display,
 * camera, speaker, etc.).
 *
 * Based on the implementation in:
 *   MyDeviceInfoFragment.java (Settings app patch by Omkar Parte)
 *
 * Returns true if the app was launched, false if it is not installed.
 * The caller is responsible for showing a Toast on false.
 */

package com.xiaomi.settings.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

object CitLauncher {

    private const val TAG          = "CitLauncher"
    private const val CIT_PACKAGE  = "com.miui.cit"
    private const val CIT_ACTIVITY = "com.miui.cit.home.HomeActivity"

    /**
     * Attempts to start the Xiaomi CIT HomeActivity.
     * @return true if the intent resolved and was fired, false otherwise.
     */
    fun launch(context: Context): Boolean {
        return runCatching {
            val intent = Intent().apply {
                component = ComponentName(CIT_PACKAGE, CIT_ACTIVITY)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.d(TAG, "CIT app launched successfully")
            true
        }.onFailure { e ->
            Log.e(TAG, "Unable to launch CIT app — is it installed?", e)
        }.getOrDefault(false)
    }

    /** Returns true if the CIT app is installed on this device. */
    fun isInstalled(context: Context): Boolean = runCatching {
        context.packageManager.getPackageInfo(CIT_PACKAGE, 0)
        true
    }.getOrDefault(false)
}
