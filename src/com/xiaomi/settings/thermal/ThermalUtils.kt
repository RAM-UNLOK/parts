/*
 * SPDX-FileCopyrightText: 2020 The LineageOS Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.thermal

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.UserHandle
import android.provider.MediaStore
import android.telecom.TelecomManager
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.android.settingslib.applications.AppUtils.isBrowserApp
import com.xiaomi.settings.R
import com.xiaomi.settings.utils.dlog
import com.xiaomi.settings.utils.writeLine

class ThermalUtils private constructor(private val context: Context) {

    private val sharedPrefs   = PreferenceManager.getDefaultSharedPreferences(context)
    private val serviceIntent = Intent(context, ThermalService::class.java)

    var enabled: Boolean = sharedPrefs.getBoolean(THERMAL_ENABLED, true)
        set(value) {
            if (field == value) return
            field = value
            sharedPrefs.edit().putBoolean(THERMAL_ENABLED, value).apply()
            if (value) startService() else { setDefaultThermalProfile(); stopService() }
        }

    fun startService() {
        if (!enabled) return
        runCatching {
            context.startServiceAsUser(serviceIntent, UserHandle.CURRENT)
        }
    }

    fun stopService() {
        runCatching {
            context.stopServiceAsUser(serviceIntent, UserHandle.CURRENT)
        }
    }

    fun setThermalProfile(packageName: String) {
        setThermalProfileInternal(packageName)
    }

    /**
     * Applies the thermal-charge.conf (sconfig=27) for charging state.
     * Returns true on success.
     */
    fun setChargingThermalProfile(): Boolean {
        return runCatching {
            writeLine(THERMAL_SCONFIG, SCONFIG_CHARGE)
            dlog(TAG, "Charging profile applied (sconfig=$SCONFIG_CHARGE)")
        }.isSuccess
    }

    fun setDefaultThermalProfile() {
        writeSconfig(DEFAULT_SCONFIG)
    }

    private fun setThermalProfileInternal(packageName: String) {
        val state = getStateForPackage(packageName)
        writeSconfig(state.sconfig)
        dlog(TAG, "setThermalProfileInternal: $packageName -> $state (sconfig=${state.sconfig})")
    }

    private fun writeSconfig(sconfig: String) {
        runCatching { writeLine(THERMAL_SCONFIG, sconfig) }
            .onFailure { dlog(TAG, "writeSconfig failed: ${it.message}") }
    }

    fun getStateForPackage(packageName: String): ThermalState {
        // 1. Explicit user preference
        val savedId = getPackagePreference(packageName)
        if (savedId != ThermalState.DEFAULT.id) {
            ThermalState.entries.find { it.id == savedId }?.let { return it }
        }
        // 2. Automatic classification
        return classifyApp(packageName)
    }

    private fun classifyApp(packageName: String): ThermalState {
        val pm = context.packageManager
        runCatching {
            val appInfo = pm.getApplicationInfo(
                packageName, PackageManager.GET_META_DATA
            )
            return when {
                isGameApp(appInfo)               -> ThermalState.GAMING
                isCameraApp(packageName, pm)     -> ThermalState.CAMERA
                isBrowserApp(context, packageName) -> ThermalState.BROWSER
                isDialerApp(packageName)         -> ThermalState.DIALER
                isVideoApp(packageName)          -> ThermalState.VIDEO
                isStreamingApp(packageName)      -> ThermalState.STREAMING
                isMusicApp(appInfo, pm)          -> ThermalState.MUSIC
                isSocialApp(appInfo)             -> ThermalState.SOCIAL
                else                             -> ThermalState.DEFAULT
            }
        }
        return ThermalState.DEFAULT
    }

    private fun isGameApp(appInfo: ApplicationInfo): Boolean {
        return (appInfo.category == ApplicationInfo.CATEGORY_GAME) ||
               (appInfo.flags and ApplicationInfo.FLAG_IS_GAME != 0)
    }

    private fun isCameraApp(packageName: String, pm: PackageManager): Boolean {
        return pm.queryIntentActivities(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE), PackageManager.MATCH_DEFAULT_ONLY
        ).any { it.activityInfo.packageName == packageName }
    }

    private fun isDialerApp(packageName: String): Boolean {
        return runCatching {
            context.getSystemService(TelecomManager::class.java)
                ?.defaultDialerPackage == packageName
        }.getOrDefault(false)
    }

    private fun isVideoApp(packageName: String): Boolean {
        return VIDEO_PKGS.any { packageName.startsWith(it) }
    }

    private fun isStreamingApp(packageName: String): Boolean {
        return STREAMING_PKGS.any { packageName.startsWith(it) }
    }

    private fun isMusicApp(appInfo: ApplicationInfo, pm: PackageManager): Boolean {
        return appInfo.category == ApplicationInfo.CATEGORY_AUDIO ||
               pm.queryIntentActivities(
                   Intent(Intent.ACTION_VIEW).setType("audio/*"),
                   PackageManager.MATCH_DEFAULT_ONLY,
               ).any { it.activityInfo.packageName == appInfo.packageName }
    }

    private fun isSocialApp(appInfo: ApplicationInfo): Boolean {
        return appInfo.category == ApplicationInfo.CATEGORY_SOCIAL
    }

    fun writePackage(packageName: String, stateId: Int) {
        val key = "$THERMAL_PACKAGE_PREFIX$packageName"
        sharedPrefs.edit().putInt(key, stateId).apply()
    }

    fun resetProfiles() {
        val editor = sharedPrefs.edit()
        sharedPrefs.all.keys
            .filter { it.startsWith(THERMAL_PACKAGE_PREFIX) }
            .forEach { editor.remove(it) }
        editor.apply()
    }

    private fun getPackagePreference(packageName: String): Int {
        return sharedPrefs.getInt(
            "$THERMAL_PACKAGE_PREFIX$packageName",
            ThermalState.DEFAULT.id,
        )
    }

    enum class ThermalState(
        val id     : Int,
        val sconfig: String,
        @StringRes val label: Int,
    ) {
        BENCHMARK      (0,  "10", R.string.thermal_benchmark),
        BROWSER        (1,  "6",  R.string.thermal_browser),
        CAMERA         (2,  "4",  R.string.thermal_camera),
        DIALER         (3,  "7",  R.string.thermal_dialer),
        GAMING         (4,  "11", R.string.thermal_gaming),
        NAVIGATION     (5,  "2",  R.string.thermal_navigation),
        VIDEO_CALL     (6,  "14", R.string.thermal_video_call),
        VIDEO_STREAMING(7,  "15", R.string.thermal_video_streaming),
        VIDEO          (8,  "12", R.string.thermal_video),
        SOCIAL         (9,  "20", R.string.thermal_social),
        MUSIC          (10, "5",  R.string.thermal_music),
        DEFAULT        (11, "0",  R.string.thermal_default),
        STREAMING      (12, "9",  R.string.thermal_streaming),
    }

    companion object {
        private const val TAG = "ThermalUtils"

        private const val THERMAL_ENABLED        = "thermal_enabled"
        private const val THERMAL_PACKAGE_PREFIX = "thermal_package_"
        private const val THERMAL_SCONFIG        = "/sys/devices/virtual/thermal/thermal_message/sconfig"
        private const val DEFAULT_SCONFIG        = "0"
        private const val SCONFIG_CHARGE         = "27"

        private val VIDEO_PKGS = listOf(
            "com.google.android.youtube",
            "com.netflix",
            "com.amazon.avod",
            "com.disney.disneyplus",
        )
        private val STREAMING_PKGS = listOf(
            "tv.twitch",
            "com.google.android.youtube.creator",
        )

        @Volatile private var instance: ThermalUtils? = null

        fun getInstance(context: Context): ThermalUtils =
            instance ?: synchronized(this) {
                instance ?: ThermalUtils(context.applicationContext).also { instance = it }
            }
    }
}
