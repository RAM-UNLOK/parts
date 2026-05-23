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

/** Lightweight model for per-app thermal UI — lives in the data layer. */
data class AppThermalEntry(
    val packageName: String,
    val label:       String,
    val profileId:   Int,
)

class ThermalUtils private constructor(private val context: Context) {

    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val serviceIntent = Intent(context, ThermalService::class.java)

    var enabled: Boolean = sharedPrefs.getBoolean(THERMAL_ENABLED, true)
        set(value) {
            if (field == value) return
            field = value
            sharedPrefs.edit().putBoolean(THERMAL_ENABLED, value).apply()
            if (value) {
                startService()
            } else {
                // Stop the service first — its onDestroy() writes the default
                // profile as part of its own teardown. We then write it again
                // as a belt-and-suspenders guarantee in case the service was
                // not running at the time this setter was called.
                stopService()
                setDefaultThermalProfile()
            }
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
     * Apply charging thermal profile.
     * This is the highest-priority profile — overrides all app profiles.
     * Called by ThermalService whenever charger is connected.
     */
    fun setChargingThermalProfile() {
        writeSconfig(SCONFIG_CHARGE)
        dlog(TAG, "Charging profile applied (sconfig=$SCONFIG_CHARGE)")
    }

    /**
     * Reset to default thermal profile.
     * Used when: screen is off, thermal is disabled, or service is stopping.
     * References ThermalState.DEFAULT.sconfig as the single source of truth
     * so this stays correct if the default sconfig value ever changes.
     */
    fun setDefaultThermalProfile() {
        writeSconfig(ThermalState.DEFAULT.sconfig)
        dlog(TAG, "Default profile applied (sconfig=${ThermalState.DEFAULT.sconfig})")
    }

    private fun setThermalProfileInternal(packageName: String) {
        val state = getStateForPackage(packageName)
        writeSconfig(state.sconfig)
        dlog(TAG, "setThermalProfileInternal: $packageName -> $state (sconfig=${state.sconfig})")
    }

    private fun writeSconfig(sconfig: String) {
        runCatching { writeLine(THERMAL_SCONFIG, sconfig) }
            .onFailure { dlog(TAG, "writeSconfig($sconfig) failed: ${it.message}") }
    }

    fun getStateForPackage(packageName: String): ThermalState {
        val savedId = getPackagePreference(packageName)
        if (savedId != ThermalState.DEFAULT.id) {
            ThermalState.entries.find { it.id == savedId }?.let { return it }
        }
        return classifyApp(packageName)
    }

    private fun classifyApp(packageName: String): ThermalState {
        val pm = context.packageManager
        runCatching {
            val appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            return when {
                isGameApp(appInfo)              -> ThermalState.GAMING
                isCameraApp(packageName, pm)    -> ThermalState.CAMERA
                isBrowserApplication(packageName) -> ThermalState.BROWSER
                isDialerApp(packageName)        -> ThermalState.DIALER
                isVideoApp(packageName)         -> ThermalState.VIDEO
                isStreamingApp(packageName)     -> ThermalState.STREAMING
                isMusicApp(appInfo, pm)         -> ThermalState.MUSIC
                isSocialApp(appInfo)            -> ThermalState.SOCIAL
                else                            -> ThermalState.DEFAULT
            }
        }.onFailure { e ->
            dlog(TAG, "classifyApp($packageName) failed: ${e.message}")
        }
        return ThermalState.DEFAULT
    }

    private fun isGameApp(appInfo: ApplicationInfo): Boolean =
        appInfo.category == ApplicationInfo.CATEGORY_GAME

    private fun isCameraApp(packageName: String, pm: PackageManager): Boolean =
        pm.queryIntentActivities(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE), PackageManager.MATCH_DEFAULT_ONLY,
        ).any { it.activityInfo.packageName == packageName }

    @Suppress("RestrictedApi")
    private fun isBrowserApplication(packageName: String): Boolean =
        isBrowserApp(context, packageName, UserHandle.myUserId())

    private fun isDialerApp(packageName: String): Boolean =
        runCatching {
            context.getSystemService(TelecomManager::class.java)
                ?.defaultDialerPackage == packageName
        }.getOrDefault(false)

    private fun isVideoApp(packageName: String): Boolean =
        VIDEO_PKGS.any { packageName.startsWith(it) }

    private fun isStreamingApp(packageName: String): Boolean =
        STREAMING_PKGS.any { packageName.startsWith(it) }

    private fun isMusicApp(appInfo: ApplicationInfo, pm: PackageManager): Boolean =
        appInfo.category == ApplicationInfo.CATEGORY_AUDIO ||
            pm.queryIntentActivities(
                Intent(Intent.ACTION_VIEW).setType("audio/*"),
                PackageManager.MATCH_DEFAULT_ONLY,
            ).any { it.activityInfo.packageName == appInfo.packageName }

    private fun isSocialApp(appInfo: ApplicationInfo): Boolean =
        appInfo.category == ApplicationInfo.CATEGORY_SOCIAL

    fun writePackage(packageName: String, stateId: Int) {
        val key = "$THERMAL_PACKAGE_PREFIX$packageName"
        sharedPrefs.edit().putInt(key, stateId).apply()
    }

    fun resetProfiles() {
        sharedPrefs.edit()
            .also { editor ->
                sharedPrefs.all.keys
                    .filter { it.startsWith(THERMAL_PACKAGE_PREFIX) }
                    .forEach { editor.remove(it) }
            }
            .apply()
    }

    private fun getPackagePreference(packageName: String): Int =
        sharedPrefs.getInt(
            "$THERMAL_PACKAGE_PREFIX$packageName",
            ThermalState.DEFAULT.id,
        )

    // DEFAULT is declared first so ThermalState.entries returns it at index 0,
    // which places it at the top of the per-app profile dropdown in the UI.
    // All numeric IDs are stable — reordering the declaration does not change
    // any stored SharedPreferences values.
    enum class ThermalState(
        val id: Int,
        val sconfig: String,
        @param:StringRes val label: Int,
    ) {
        DEFAULT(11,  "0",  R.string.thermal_default),
        BENCHMARK(0, "10", R.string.thermal_benchmark),
        BROWSER(1,   "6",  R.string.thermal_browser),
        CAMERA(2,    "4",  R.string.thermal_camera),
        DIALER(3,    "7",  R.string.thermal_dialer),
        GAMING(4,    "11", R.string.thermal_gaming),
        NAVIGATION(5,"2",  R.string.thermal_navigation),
        VIDEO_CALL(6,"5",  R.string.thermal_video_call),
        MUSIC(7,     "8",  R.string.thermal_music),
        VIDEO(8,     "3",  R.string.thermal_video),
        STREAMING(9, "9",  R.string.thermal_streaming),
        SOCIAL(10,   "1",  R.string.thermal_social);
    }

    companion object {
        private const val TAG                  = "ThermalUtils"
        const val THERMAL_ENABLED              = "thermal_enabled"
        const val THERMAL_PACKAGE_PREFIX       = "thermal_package_"
        const val THERMAL_SCONFIG              = "/sys/devices/virtual/thermal/thermal_message/sconfig"
        const val SCONFIG_CHARGE               = "27"

        private val VIDEO_PKGS = listOf(
            "com.google.android.youtube",
            "com.netflix",
            "com.amazon.avod",
            "com.disney.disneyplus",
        )
        private val STREAMING_PKGS = listOf(
            "tv.twitch",
            "tv.twitch.android.app"
            "com.twitch",
            "com.google.android.youtube.creator",
        )

        @Volatile private var instance: ThermalUtils? = null

        fun getInstance(context: Context): ThermalUtils =
            instance ?: synchronized(this) {
                instance ?: ThermalUtils(context.applicationContext).also { instance = it }
            }
    }
}
