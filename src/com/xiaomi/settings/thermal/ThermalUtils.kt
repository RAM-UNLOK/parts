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
                isBenchmarkApp(packageName)       -> ThermalState.BENCHMARK
                isCompetitiveGameApp(packageName) -> ThermalState.GAMING_COMPETITIVE
                isHeavyGameApp(packageName)       -> ThermalState.GAMING_HEAVY
                isHighFpsApp(packageName)         -> ThermalState.HIGH_FPS
                isGameApp(appInfo)                -> ThermalState.GAMING
                isCameraApp(packageName, pm)      -> ThermalState.CAMERA
                isBrowserApplication(packageName) -> ThermalState.BROWSER
                isDialerApp(packageName)          -> ThermalState.DIALER
                isVideoCallApp(packageName)       -> ThermalState.VIDEO_CALL
                isYouTubeApp(packageName)         -> ThermalState.YOUTUBE
                is4KVideoApp(packageName)         -> ThermalState.VIDEO_4K
                isVideoApp(packageName)           -> ThermalState.VIDEO
                isStreamingApp(packageName)       -> ThermalState.STREAMING
                isNavigationApp(packageName, pm)  -> ThermalState.NAVIGATION
                isMusicApp(appInfo, pm)           -> ThermalState.MUSIC
                isSocialApp(appInfo)              -> ThermalState.SOCIAL
                else                              -> ThermalState.DEFAULT
            }
        }.onFailure { e ->
            dlog(TAG, "classifyApp($packageName) failed: ${e.message}")
        }
        return ThermalState.DEFAULT
    }

    private fun isBenchmarkApp(packageName: String): Boolean =
        BENCHMARK_PKGS.any { packageName.startsWith(it) }

    private fun isCompetitiveGameApp(packageName: String): Boolean =
        COMPETITIVE_GAME_PKGS.any { packageName.startsWith(it) }

    private fun isHeavyGameApp(packageName: String): Boolean =
        HEAVY_GAME_PKGS.any { packageName.startsWith(it) }

    private fun isHighFpsApp(packageName: String): Boolean =
        HIGH_FPS_PKGS.any { packageName.startsWith(it) }

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

    private fun isVideoCallApp(packageName: String): Boolean =
        VIDEO_CALL_PKGS.any { packageName.startsWith(it) }

    private fun isYouTubeApp(packageName: String): Boolean =
        packageName == "com.google.android.youtube"

    private fun isVideoApp(packageName: String): Boolean =
        VIDEO_PKGS.any { packageName.startsWith(it) }

    private fun is4KVideoApp(packageName: String): Boolean =
        VIDEO_4K_PKGS.any { packageName.startsWith(it) }

    private fun isStreamingApp(packageName: String): Boolean =
        STREAMING_PKGS.any { packageName.startsWith(it) }

    private fun isNavigationApp(packageName: String, pm: PackageManager): Boolean =
        pm.queryIntentActivities(
            Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("geo:0,0")
            },
            PackageManager.MATCH_DEFAULT_ONLY,
        ).any { it.activityInfo.packageName == packageName }

    // Removed the redundant 'packageName: String' parameter. We extract it from appInfo.
    private fun isMusicApp(appInfo: ApplicationInfo, pm: PackageManager): Boolean =
        appInfo.category == ApplicationInfo.CATEGORY_AUDIO ||
            MUSIC_PKGS.any { appInfo.packageName.startsWith(it) } ||
            pm.queryIntentActivities(
                Intent(Intent.ACTION_VIEW).setType("audio/*"),
                PackageManager.MATCH_DEFAULT_ONLY,
            ).any { it.activityInfo.packageName == appInfo.packageName }

    // Removed the redundant 'packageName: String' parameter. We extract it from appInfo.
    private fun isSocialApp(appInfo: ApplicationInfo): Boolean =
        appInfo.category == ApplicationInfo.CATEGORY_SOCIAL ||
            SOCIAL_PKGS.any { appInfo.packageName.startsWith(it) }

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

    /**
     * Maps to the real sconfig node values from the device thermal map.
     *
     * Confirmed mapping (conf file → sconfig value):
     *   thermal-normal.conf        → 0   (DEFAULT)
     *   thermal-social / abnormal  → 2   (SOCIAL / ABNORMAL)
     *   thermal-navigation.conf    → 10  (NAVIGATION)
     *   thermal-phone.conf         → 5   (DIALER)
     *   thermal-videochat.conf     → 14  (VIDEO_CALL)  ← also SCONFIG_CHARGE alias
     *   thermal-camera.conf        → 15  (CAMERA)
     *   thermal-video.conf         → 11  (VIDEO)
     *   thermal-youtube.conf       → 8   (YOUTUBE)
     *   thermal-4k.conf            → 16  (VIDEO_4K)
     *   thermal-extravideo.conf    → 28  (STREAMING)
     *   thermal-mgame.conf         → 19  (GAMING / medium)
     *   thermal-tgame.conf         → 18  (GAMING_HEAVY / top-tier)
     *   thermal-cgame.conf         → 700 (GAMING_COMPETITIVE)
     *   thermal-highfps.conf       → 26  (HIGH_FPS)
     *   thermal-nolimits.conf      → 6   (BENCHMARK / no-limits)
     *   thermal-charge.conf        → 27  (CHARGE — internal, not user-selectable)
     *
     * [id] is an internal UI-only discriminator; [sconfig] is written to the kernel node.
     */
    enum class ThermalState(
        val id: Int,
        val sconfig: String,
        @param:StringRes val label: Int,
        val userSelectable: Boolean = true,
    ) {
        DEFAULT        (0,  "0",   R.string.thermal_default),
        SOCIAL         (1,  "2",   R.string.thermal_social),
        NAVIGATION     (2,  "10",  R.string.thermal_navigation),
        DIALER         (3,  "5",   R.string.thermal_dialer),
        VIDEO_CALL     (4,  "14",  R.string.thermal_video_call),
        CAMERA         (5,  "15",  R.string.thermal_camera),
        VIDEO          (6,  "11",  R.string.thermal_video),
        YOUTUBE        (7,  "8",   R.string.thermal_streaming),
        VIDEO_4K       (8,  "16",  R.string.thermal_video_4k),
        STREAMING      (9,  "28",  R.string.thermal_streaming_extra),
        GAMING         (10, "19",  R.string.thermal_gaming),
        GAMING_HEAVY   (11, "18",  R.string.thermal_gaming_heavy),
        GAMING_COMPETITIVE(12, "700", R.string.thermal_gaming_competitive),
        HIGH_FPS       (13, "26",  R.string.thermal_high_fps),
        BENCHMARK      (14, "6",   R.string.thermal_benchmark),
        MUSIC          (15, "0",   R.string.thermal_music),
        BROWSER        (16, "9",   R.string.thermal_browser),
        CHARGE         (17, "27",  R.string.thermal_charge, userSelectable = false),
    }

    companion object {
        private const val TAG                  = "ThermalUtils"
        const val THERMAL_ENABLED              = "thermal_enabled"
        const val THERMAL_PACKAGE_PREFIX       = "thermal_package_"
        const val THERMAL_SCONFIG              = "/sys/devices/virtual/thermal/thermal_message/sconfig"
        /** Charging profile — written directly by setChargingThermalProfile(), not via ThermalState. */
        const val SCONFIG_CHARGE               = "27"

        private val BENCHMARK_PKGS = listOf(
            "com.antutu",
            "com.futuremark",
            "com.primatelabs",
            "com.ludashi",
            "net.kishonti",
            "com.tencent.wetest"
        )
        private val COMPETITIVE_GAME_PKGS = listOf(
            "com.tencent.ig",
            "com.vng.pubgmobile",
            "com.pubg.krmobile",
            "com.rekoo.pubgm",
            "com.dts.freefireth",
            "com.dts.freefiremax",
            "com.mobile.legends",
            "com.riotgames.league.wildrift",
            "com.epicgames.fortnite",
            "com.tencent.tmgp.sgame"
        )
        private val HEAVY_GAME_PKGS = listOf(
            "com.miHoYo.Yuanshen",
            "com.miHoYo.GenshinImpact",
            "com.miHoYo.hkrpg",
            "com.kurogame.wutheringwaves",
            "com.kurogame.gplay.punishing.grayraven.en",
            "com.LevelInfinite.Hotta.tof",
            "com.tencent.tmgp.pubgmhd",
            "com.activision.callofduty",
            "com.ea.games",
            "com.pubg.imobile"
        )
        private val HIGH_FPS_PKGS = listOf(
            "com.madfingergames.legends",
            "com.netease.lztgglobal",
            "com.kiloo.subwaysurf",
            "com.imangi.templerun"
        )
        private val VIDEO_CALL_PKGS = listOf(
            "com.google.android.apps.meetings",
            "com.microsoft.teams",
            "us.zoom.videomeetings",
            "com.discord",
            "com.skype.raider"
        )
        private val VIDEO_PKGS = listOf(
            "com.netflix",
            "com.amazon.avod",
            "com.disney.disneyplus",
            "com.hotstar",
            "com.jio.jiocinema",
            "com.hbo.hbonow",
            "com.hulu.plus",
            "com.apple.atve.android.app",
            "com.plexapp.android"
        )
        private val VIDEO_4K_PKGS = listOf(
            "com.netflix.mediaclient",
        )
        private val STREAMING_PKGS = listOf(
            "tv.twitch.android.app",
            "com.google.android.youtube.creator",
            "com.facebook.katana",
            "com.instagram.android",
            "com.bilibili.app.in"
        )
        private val SOCIAL_PKGS = listOf(
            "com.whatsapp",
            "com.whatsapp.w4b",
            "org.telegram.messenger",
            "com.twitter.android",
            "com.snapchat.android",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.facebook.orca",
            "com.viber.voip",
            "com.vkontakte.android",
            "com.reddit.frontpage",
            "com.pinterest"
        )
        private val MUSIC_PKGS = listOf(
            "com.spotify.music",
            "com.apple.android.music",
            "com.soundcloud.android",
            "com.pandora.android",
            "com.amazon.mp3",
            "com.google.android.apps.youtube.music",
            "deezer.android.app"
        )

        @Volatile private var instance: ThermalUtils? = null

        fun getInstance(context: Context): ThermalUtils =
            instance ?: synchronized(this) {
                instance ?: ThermalUtils(context.applicationContext).also { instance = it }
            }
    }
}