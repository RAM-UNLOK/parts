/*
 * SPDX-FileCopyrightText: 2020 The LineageOS Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * Refactored:
 *  - ThermalState enum expanded with SOCIAL and MUSIC categories
 *  - All app package lists greatly expanded for IN/US/global coverage
 *  - sconfig values remapped to best-fit .conf from the device thermal map
 *  - Every external call wrapped in runCatching for crash safety
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

/**
 * Core thermal profile utility.
 *
 * Profiles are stored as a colon-separated string in SharedPreferences.
 * Each segment corresponds to one ThermalState and holds a comma-separated
 * list of package names assigned to that state by the user.
 *
 * Example:  "com.antutu.ABenchMark,:com.android.chrome,:..."
 *            ^BENCHMARK segment      ^BROWSER segment
 *
 * On every foreground app change ThermalService calls [setThermalProfile],
 * which writes the matching sconfig value to the kernel thermal node.
 */
class ThermalUtils private constructor(private val context: Context) {

    private val sharedPrefs   = PreferenceManager.getDefaultSharedPreferences(context)
    private val serviceIntent = Intent(context, ThermalService::class.java)

    /** Whether per-app thermal profiling is active. Persisted to SharedPrefs. */
    var enabled: Boolean = sharedPrefs.getBoolean(THERMAL_ENABLED, true)
        set(value) {
            if (field == value) return
            field = value
            sharedPrefs.edit().putBoolean(THERMAL_ENABLED, value).apply()
            if (value) startService() else { setDefaultThermalProfile(); stopService() }
        }

    /** The raw colon-separated profile string. Auto-persisted on change. */
    private var value: String = readValue()
        set(value) {
            if (field == value) return
            field = value
            sharedPrefs.edit().putString(THERMAL_CONTROL, value).apply()
        }

    /**
     * Set to true by [ChargingThermalReceiver] when a charger is connected.
     * While true, [setThermalProfile] and [setDefaultThermalProfile] are
     * suppressed so thermal-charge.conf (sconfig=27) stays active for the
     * entire charging session regardless of foreground app changes.
     *
     * Written from a BroadcastReceiver (main thread), read from the
     * ThermalService worker thread — @Volatile ensures visibility.
     */
    @Volatile
    var isCharging: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            dlog(TAG, "isCharging changed to $value")
            if (!value) {
                // Charger removed — immediately apply the correct per-app profile
                // so there is no window where normal.conf lingers after unplug.
                setThermalProfileInternal(currentApp)
            }
        }

    /** Package name of the current foreground app — kept in sync by ThermalService. */
    var currentApp: String = ""

    // ── Service lifecycle ────────────────────────────────────────────────

    fun startService() {
        if (!enabled) return
        dlog(TAG, "startService")
        runCatching {
            context.startServiceAsUser(serviceIntent, UserHandle.CURRENT)
        }.onFailure { e -> dlog(TAG, "startService failed: $e") }
    }

    fun stopService() {
        dlog(TAG, "stopService")
        runCatching { context.stopService(serviceIntent) }
            .onFailure { e -> dlog(TAG, "stopService failed: $e") }
    }

    // ── Profile read/write ───────────────────────────────────────────────

    private fun readValue(): String =
        sharedPrefs.getString(THERMAL_CONTROL, null) ?: DEFAULT_VALUE

    /**
     * Assigns [packageName] to the thermal profile at index [mode].
     * Removes the package from whatever segment it was in before.
     */
    fun writePackage(packageName: String, mode: Int) {
        dlog(TAG, "writePackage: $packageName -> mode=$mode")
        runCatching {
            var newValue = value.replace("$packageName,", "")
            val modes = newValue.split(":").toMutableList()
            if (mode in modes.indices) modes[mode] += "$packageName,"
            value = modes.joinToString(":")
        }.onFailure { e -> dlog(TAG, "writePackage failed: $e") }
    }

    /** Returns the [ThermalState] currently assigned to [packageName]. */
    fun getStateForPackage(packageName: String): ThermalState {
        return runCatching {
            val modes = value.split(":")
            ThermalState.values().find { state ->
                state.id < modes.size && modes[state.id].contains("$packageName,")
            } ?: getDefaultStateForPackage(packageName)
        }.getOrDefault(ThermalState.DEFAULT)
    }

    /** Clears all per-app overrides, restoring automatic defaults. */
    fun resetProfiles() {
        dlog(TAG, "resetProfiles")
        value = DEFAULT_VALUE
    }

    // ── Hardware writes ──────────────────────────────────────────────────

        /**
     * Applies thermal-charge.conf (sconfig=27) when the charger is plugged in.
     * Returns true on success, false if the sysfs write failed.
     */
    fun setChargingThermalProfile(): Boolean {
        dlog(TAG, "setChargingThermalProfile -> $THERMAL_CHARGE_VALUE")
        return runCatching {
            writeLine(THERMAL_SCONFIG, THERMAL_CHARGE_VALUE)
            true
        }.getOrElse { e ->
            dlog(TAG, "setChargingThermalProfile failed: ${e.message}")
            false
        }
    }

    /** Writes thermal-normal.conf (sconfig=0) — used when screen is off.
     *  No-op while charging to protect thermal-charge.conf. */
    fun setDefaultThermalProfile() {
        if (isCharging) {
            dlog(TAG, "setDefaultThermalProfile: suppressed — charging active")
            return
        }
        dlog(TAG, "setDefaultThermalProfile")
        runCatching { writeLine(THERMAL_SCONFIG, THERMAL_STATE_OFF) }
            .onFailure { e -> dlog(TAG, "setDefaultThermalProfile failed: $e") }
    }

    /**
     * Looks up the profile for [packageName] and writes the sconfig value.
     * Called by [ThermalService] on every foreground app change.
     *
     * Suppressed while [isCharging] is true — thermal-charge.conf must not
     * be overridden during a charging session.
     */
    fun setThermalProfile(packageName: String) {
        currentApp = packageName   // Always track current app even while charging
        if (isCharging) {
            dlog(TAG, "setThermalProfile: suppressed for $packageName — charging active")
            return
        }
        setThermalProfileInternal(packageName)
    }

    /** Unconditional internal write — used after charger disconnect to restore state. */
    private fun setThermalProfileInternal(packageName: String) {
        if (packageName.isEmpty()) {
            dlog(TAG, "setThermalProfileInternal: empty package — skipping")
            return
        }
        runCatching {
            val state = getStateForPackage(packageName)
            dlog(TAG, "setThermalProfile: $packageName -> $state (sconfig=${state.config})")
            writeLine(THERMAL_SCONFIG, state.config)
        }.onFailure { e -> dlog(TAG, "setThermalProfile failed for $packageName: $e") }
    }

    // ── Default state heuristics ─────────────────────────────────────────

    /**
     * Determines the best-fit [ThermalState] for [packageName] without
     * any user override, using:
     *  1. Hard-coded benchmark/gaming/navigation/social/music package lists
     *  2. ApplicationInfo.category from PackageManager
     *  3. Camera intent query
     *  4. Default dialer detection
     *  5. Browser detection via settingslib
     */
    private fun getDefaultStateForPackage(packageName: String): ThermalState {
        if (BENCHMARKING_APPS.contains(packageName))        return ThermalState.BENCHMARK
        if (NAVIGATION_PACKAGES.contains(packageName))      return ThermalState.NAVIGATION
        if (VIDEO_CALL_PACKAGES.contains(packageName))      return ThermalState.VIDEO_CALL
        if (VIDEO_STREAMING_PACKAGES.contains(packageName)) return ThermalState.STREAMING
        if (SOCIAL_PACKAGES.contains(packageName))          return ThermalState.SOCIAL
        if (MUSIC_PACKAGES.contains(packageName))           return ThermalState.MUSIC
        if (GAMING_PACKAGES.contains(packageName))          return ThermalState.GAMING

        runCatching { context.packageManager.getApplicationInfo(packageName, 0) }
            .onSuccess { info ->
                when (info.category) {
                    ApplicationInfo.CATEGORY_GAME  -> return ThermalState.GAMING
                    ApplicationInfo.CATEGORY_VIDEO -> return ThermalState.VIDEO
                    ApplicationInfo.CATEGORY_MAPS  -> return ThermalState.NAVIGATION
                }
            }
            .onFailure { return ThermalState.DEFAULT }

        if (isCameraApp(packageName))                       return ThermalState.CAMERA
        if (isDefaultDialer(packageName))                   return ThermalState.DIALER
        if (isBrowserApp(context, packageName, UserHandle.myUserId())) return ThermalState.BROWSER

        return ThermalState.DEFAULT
    }

    private fun isCameraApp(packageName: String): Boolean = runCatching {
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).setPackage(packageName)
        context.packageManager.queryIntentActivitiesAsUser(
            intent, PackageManager.MATCH_ALL, UserHandle.myUserId(),
        ).any { it.activityInfo != null }
    }.getOrDefault(false)

    private fun isDefaultDialer(packageName: String): Boolean = runCatching {
        context.getSystemService(TelecomManager::class.java)
            ?.defaultDialerPackage == packageName
    }.getOrDefault(false)

    // ════════════════════════════════════════════════════════════════════
    // ThermalState enum
    //
    // Maps each profile category to a sconfig value (the index written to
    // /sys/devices/virtual/thermal/thermal_message/sconfig).
    //
    // sconfig → .conf mapping (from device thermal map):
    //   0  thermal-normal.conf       (DEFAULT / screen-off baseline)
    //   5  thermal-phone.conf        (DIALER)
    //   6  thermal-nolimits.conf     (BENCHMARK)
    //   7  thermal-class0.conf       (BROWSER)
    //   8  thermal-youtube.conf      (STREAMING — YouTube, video streaming)
    //   10 thermal-navigation.conf   (NAVIGATION)
    //   11 thermal-video.conf        (VIDEO — local media players)
    //   14 thermal-videochat.conf    (VIDEO_CALL — Meet, Zoom, Teams etc.)
    //   15 thermal-camera.conf       (CAMERA)
    //   18 thermal-tgame.conf        (GAMING — touch-intensive games)
    //   19 thermal-mgame.conf        (SOCIAL — moderate sustained load)
    //   26 thermal-highfps.conf      (MUSIC — low thermal, smooth UI)
    //
    // id must match the segment index in the colon-separated prefs string.
    // ════════════════════════════════════════════════════════════════════
    enum class ThermalState(
        val id     : Int,
        val config : String,   // sconfig value written to the kernel node
        val prefix : String,   // SharedPrefs key prefix
        @StringRes val label: Int,
    ) {
        BENCHMARK  (0,  "6",  "thermal.benchmark=",  R.string.thermal_benchmark),
        BROWSER    (1,  "7",  "thermal.browser=",    R.string.thermal_browser),
        CAMERA     (2,  "15", "thermal.camera=",     R.string.thermal_camera),
        DIALER     (3,  "5",  "thermal.dialer=",     R.string.thermal_dialer),
        GAMING     (4,  "18", "thermal.gaming=",     R.string.thermal_gaming),
        NAVIGATION (5,  "10", "thermal.navigation=", R.string.thermal_navigation),
        VIDEO_CALL (6,  "14", "thermal.videocall=",  R.string.thermal_streaming),
        STREAMING  (7,  "8",  "thermal.streaming=",  R.string.thermal_video_streaming),
        VIDEO      (8,  "11", "thermal.video=",      R.string.thermal_video),
        SOCIAL     (9,  "19", "thermal.social=",     R.string.thermal_social),
        MUSIC      (10, "26", "thermal.music=",      R.string.thermal_music),
        DEFAULT    (11, "0",  "thermal.default=",    R.string.thermal_default);
    }

    companion object {
        private const val TAG             = "ThermalUtils"
        private const val THERMAL_CONTROL  = "thermal_control_v2"
        private const val THERMAL_ENABLED  = "thermal_enabled"

        private const val THERMAL_SCONFIG   = "/sys/devices/virtual/thermal/thermal_message/sconfig"
        private const val THERMAL_STATE_OFF = "0"
        private const val THERMAL_CHARGE_VALUE = "27" // thermal-charge.conf

        /** Default prefs string — all segments empty, one per ThermalState. */
        private val DEFAULT_VALUE = ThermalState.values().joinToString(":") { it.prefix }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // BENCHMARKING — sconfig 6 (nolimits) — no thermal cap, max perf
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        private val BENCHMARKING_APPS = setOf(
            // Geekbench
            "com.primatelabs.geekbench5",
            "com.primatelabs.geekbench6",
            // AnTuTu
            "com.antutu.ABenchMark",
            "com.antutu.benchmark.full",
            "com.antutu.aibenchmark",
            // 3DMark / Futuremark
            "com.futuremark.dmandroid.application",
            "com.futuremark.pcmark.android.benchmark",
            "com.kishonti.gfxbench.gl.v50000.corporate",  // GFXBench
            // CPU / GPU stress
            "skynet.cputhrottlingtest",
            "com.texts.throttlebench",
            "com.cpuid.cpu_z",
            "com.aida64.aida64",
            "com.androbench2",
            // Misc benchmarks
            "jp.ne.hpcgi.n2works.benchmark.pbc",          // PassMark
            "com.passmark.performancetest_mobile",
            "com.ludashi.benchmark",                       // Master Lu (CN/IN)
            "com.tencent.gameassistant",                   // Tencent perf tool
        )

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // NAVIGATION — sconfig 10 (navigation) — sustained GPS + screen-on
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        private val NAVIGATION_PACKAGES = setOf(
            // Google
            "com.google.android.apps.maps",
            "com.google.android.apps.mapslite",
            "com.google.android.apps.automotive.maps",
            // Waze
            "com.waze",
            // HERE
            "com.here.app.maps",
            "com.here.app.maps.b2b",
            // Sygic
            "com.sygic.maps",
            "air.com.sygic.travel.android2",
            // TomTom
            "com.tomtom.gplay.speedcams",
            "com.tomtom.gplay.navapp",
            // OsmAnd
            "net.osmand",
            "net.osmand.plus",
            // MAPS.ME
            "com.mapswithme.maps.pro",
            "com.mapswithme.maps",
            // Apple Maps (sideloaded via emulation — edge case)
            // India ride-hailing
            "com.olacabs.customer",              // Ola
            "com.rapido.passenger",              // Rapido
            "in.juspay.nammayatri",              // Namma Yatri
            "com.blowhorn.rider",                // Blowhorn
            "com.jugnoo.rider",                  // Jugnoo
            // Global ride-hailing
            "com.ubercab",                       // Uber
            "com.lyft.android",                  // Lyft (US)
            "com.grab.passenger",                // Grab (SEA)
            "com.gojek.app",                     // Gojek (SEA/IN)
            "com.careem.acma",                   // Careem (ME)
            "com.indrive.passenger",             // inDrive (global)
            // Delivery / logistics
            "com.zomato.android",                // Zomato (has maps)
            "com.swiggy.android",                // Swiggy (has maps)
            "com.dunzo.user",                    // Dunzo (IN)
            "in.blinkit.consumer",               // Blinkit (IN)
        )

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // VIDEO CALL — sconfig 14 (videochat) — camera + mic + codec load
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        private val VIDEO_CALL_PACKAGES = setOf(
            // Google
            "com.google.android.apps.tachyon",   // Google Meet
            "com.google.android.talk",            // Hangouts legacy
            // Zoom
            "us.zoom.videomeetings",
            // Microsoft
            "com.microsoft.teams",
            "com.microsoft.teams2",
            "com.skype.raider",
            "com.skype.rover",
            // Cisco
            "com.cisco.webex.meetings",
            "com.cisco.webex",
            // GoTo
            "com.gotomeeting",
            "com.logmein.join",
            // Discord
            "com.discord",
            // Meta
            "com.facebook.orca",                 // Messenger
            "com.facebook.mlite",
            // WhatsApp
            "com.whatsapp",
            "com.whatsapp.w4b",
            // Signal
            "org.thoughtcrime.securesms",
            // Telegram (video calls)
            "org.telegram.messenger",
            "org.telegram.plus",
            "org.telegram.messenger.web",
            // Viber
            "com.viber.voip",
            // Wire
            "com.wire",
            // Element / Matrix
            "im.vector.app",
            "io.element.android.x",
            // Jio (India)
            "com.jio.meet",
            "com.reliancejio.jiomeet",
            // India enterprise
            "com.zoho.cliq",
            "com.hike.chat.stickers",
            // Others
            "com.webex.teams",
            "com.ringcentral.android",
            "com.vonage.client",
            "com.dialpad.dialing",
            "com.8x8.video",
            "com.lifesize.video",
            "com.bluejeans.android.sdksample",
            "com.whereby.app",
        )

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // VIDEO STREAMING — sconfig 8 (youtube) — decoder + display load
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        private val VIDEO_STREAMING_PACKAGES = setOf(
            // Google
            "com.google.android.youtube",
            "com.google.android.youtube.tv",
            "com.google.android.apps.youtube.kids",
            "com.google.android.apps.youtube.music",   // YT Music (video mode)
            // Netflix
            "com.netflix.mediaclient",
            "com.netflix.ninja",
            // Amazon
            "com.amazon.avod.thirdpartyclient",         // Prime Video
            // Disney / Star
            "com.hotstar.android",                      // Disney+ Hotstar (IN)
            "com.disney.disneyplus",                    // Disney+ (US/global)
            // JioCinema (IN)
            "com.jio.media.jiocinema",
            "com.reliance.jio.jiocinema",
            // Sony (IN)
            "com.sonyliv",
            // ZEE5 (IN)
            "com.zee5.android",
            // MX Player (IN/global)
            "com.mxtech.videoplayer.ad",
            "com.mxtech.videoplayer.pro",
            // Voot (IN — Peacock India)
            "com.voot.client",
            // Eros / Hungama (IN)
            "com.eros.android.movies",
            "com.hungama.movies.tv",
            "com.altbalaji.streaming",               // AltBalaji
            // Twitch
            "tv.twitch.android.app",
            // US streaming
            "com.hulu.plus",
            "com.hbo.hbonow",                        // Max
            "com.apple.atve.androidtv.appletv",
            "com.peacocktv.peacockandroid",
            "com.paramountnetwork.ui",               // Paramount+
            "com.pluto.tv",
            "com.fubo.fuboTV",                       // FuboTV
            "tv.plex.labs.plexpass",                 // Plex
            "com.directv.dvrware",                   // DirecTV Stream
            "com.slingtv.sling",                     // Sling TV
            // International
            "tv.dailymotion.dailymotion",
            "com.mubi.android",                      // MUBI
            "com.crunchyroll.crunchyroid",           // Crunchyroll (anime)
            "com.funimation.funimationapp",          // Funimation
            "com.viki.android",                      // Viki (Korean/Asian)
            "com.wetv.android",                      // WeTV (CN/SEA)
            "com.iqiyi.i18n",                        // iQIYI
            "com.kakao.tv.android",                  // Kakao TV (KR)
            "com.tving.android",                     // TVING (KR)
            "com.nhk.nhkondemand",                   // NHK World (JP)
            "com.amazon.firetv.tubi",                // Tubi TV
            "air.com.vudu.android.DownstreamClient", // Vudu
            "com.fandangonow",                       // Fandango
            // Local video players
            "org.videolan.vlc",                      // VLC
            "com.brouken.player",                    // Just Player
            "com.coder.vlc_player",
            "com.bsplayer.bspandroid.free",          // BSPlayer
            "com.kmplayer",                          // KMPlayer
            "com.mxtech.videoplayer.j",              // MX Player (alt)
            "com.cloverlabs.pvstar",                 // PVStar
        )

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // SOCIAL — sconfig 19 (mgame) — moderate sustained load, scroll/short video
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        private val SOCIAL_PACKAGES = setOf(
            // Meta
            "com.instagram.android",
            "com.facebook.katana",
            "com.facebook.lite",
            "com.facebook.mlite",
            "com.facebook.orca",                    // Messenger
            "com.facebook.pages.app",
            // Twitter / X
            "com.twitter.android",
            "com.twitter.android.lite",
            "com.x.android",
            // Snapchat
            "com.snapchat.android",
            // TikTok / short video
            "com.zhiliaoapp.musically",             // TikTok (global)
            "com.ss.android.ugc.trill",             // TikTok (IN alt package)
            "com.ss.android.ugc.aweme",             // Douyin (CN)
            "com.sharechat.sharechat",              // ShareChat (IN)
            "com.moj.app",                          // Moj (IN)
            "com.mx.takatak",                       // Josh / MX TakaTak (IN)
            "com.roposo.android",                   // Roposo (IN)
            "com.koo.app",                          // Koo (IN)
            "com.bigo.live",                        // Bigo Live
            "tv.danmaku.bili",                      // Bilibili (CN)
            // Reddit
            "com.reddit.frontpage",
            "com.laurencedawson.reddit_sync",
            "is.xyz.omg",                           // Infinity for Reddit
            "com.andrewshu.android.reddit",         // Reddit is Fun
            "com.laurencedawson.reddit_sync.pro",
            // LinkedIn
            "com.linkedin.android",
            "com.linkedin.android.lite",
            // Pinterest
            "com.pinterest",
            // Tumblr
            "com.tumblr",
            // Threads
            "com.instagram.barcelona",              // Threads
            // Mastodon / Fediverse
            "org.joinmastodon.android",
            "com.keylesspalace.tusky",
            // Telegram (chat/social)
            "org.telegram.messenger",
            "org.telegram.plus",
            "org.telegram.biftagram",
            // WhatsApp (messaging)
            "com.whatsapp",
            "com.whatsapp.w4b",
            // BeReal
            "com.bereal.ft",
            // Others
            "com.nianticlabs.pokemongo",            // Pokémon GO (AR social)
            "com.locket.widget",
            "com.vsco.cam",                         // VSCO
            "com.lightricks.facetune2",             // Facetune
            "com.meitu.meipai",                     // Meipai (CN)
            "com.meitu.beautyplusme",               // BeautyPlus
        )

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // MUSIC — sconfig 26 (highfps) — low CPU, smooth animations, battery-friendly
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        private val MUSIC_PACKAGES = setOf(
            // Global
            "com.spotify.music",
            "deezer.android.app",
            "com.amazon.mp3",                        // Amazon Music
            "com.apple.android.music",               // Apple Music
            "com.tidal.android",
            "com.soundcloud.android",
            "com.iheartradio.android",               // iHeartRadio (US)
            "com.pandora.android",                   // Pandora (US)
            "com.shazam.android",
            "com.lastfm.android",                    // Last.fm
            "com.bbc.sounds",                        // BBC Sounds
            "com.audible.application",               // Audible (audiobooks)
            "com.pocket.casts",                      // Pocket Casts (podcasts)
            "com.spotify.podcasts",
            "fm.castbox.audiobook.radio.podcast",    // Castbox
            "com.overcast.podcasts",                 // Overcast alt
            "com.google.android.apps.podcasts",      // Google Podcasts
            "com.stitcher.app",                      // Stitcher
            // India
            "com.gaana",                             // Gaana (IN)
            "com.saavn.android",                     // JioSaavn (IN)
            "com.wynk.music",                        // Wynk (IN — Airtel)
            "com.hungama.myplay",                    // Hungama Music (IN)
            "com.resso.ressoapp",                    // Resso (IN — ByteDance)
            "com.smule.singandroid",                 // Smule (IN popular)
            // Middle East / Global
            "com.anghami",                           // Anghami (ME/MENA)
            "com.yandex.music",                      // Yandex Music (RU)
            "com.naver.vibe",                        // Vibe (KR)
            "com.flo.android",                       // Flo (KR)
            "com.melon.android",                     // Melon (KR)
            // Local players
            "com.maxmpz.audioplayer",                // Poweramp
            "com.neutroncode.mp",                    // Neutron Player
            "com.blackplayer.blackplayerfree",       // BlackPlayer
            "com.musicplayer.plusplayer",            // Plus Player
            "com.a3.musicplayer",                    // Music Player
            "com.smp.music",
            "org.musicbrainz.picard",
            "com.hiby.music",                        // HiBy Music (audiophile)
            "com.vgr.neutron",
            "com.jetpack.musicplayer",
        )

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // GAMING — sconfig 18 (tgame) — explicit known gaming packages
        // (PackageManager category=GAME handles most games automatically;
        //  this list covers games that self-report wrongly or are missed)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        private val GAMING_PACKAGES = setOf(
            // Battle royale / shooters (IN top charts)
            "com.tencent.ig",                        // PUBG Mobile
            "com.pubg.krmobile",                     // PUBG KR
            "com.pubg.newstate",                     // PUBG: New State
            "com.activision.callofduty.shooter",     // COD Mobile
            "com.garena.freefireth",                 // Free Fire
            "com.garena.game.kgtw",                  // Free Fire MAX
            "com.vng.codmvn",
            // MOBAs
            "com.mobile.legends",                    // Mobile Legends
            "com.tencent.tmgp.sgame",               // Honor of Kings (CN/IN)
            "com.tencent.smoba",
            "com.supercell.clashroyale",
            "com.supercell.clashofclans",
            "com.supercell.brawlstars",
            // Battle royale — global
            "com.epicgames.fortnite",
            "com.netease.letsth",                    // LifeAfter
            "com.netease.hyxd.gpj",                  // Knives Out
            // Racing
            "com.ea.game.nfs14_row",                 // NFS: No Limits
            "com.gameloft.android.ANMP.GloftA9HM",  // Asphalt 9
            "com.naturalmotion.csrracing2",
            // Sports
            "com.ea.gp.fifamobile",                  // EA Sports FC Mobile
            "com.konami.efootball",                  // eFootball
            "com.ea.game.nba_row",                   // NBA Live
            // RPG / Gacha
            "com.miHoYo.GenshinImpact",              // Genshin Impact (yuanshen)
            "com.miHoYo.hkrpg",                      // Honkai: Star Rail
            "com.netease.diablo",
            "com.ea.game.starwarsgoh_row",
            // Casual / Hypercasual (IN top)
            "com.miniclip.eightballpool",
            "com.playgendary.tanks",
            "com.innersloth.spacemafia",             // Among Us
            "com.rollic.woodturning3d",
            "com.imangi.templerun2",
            // Strategy
            "com.scopely.monopolygo",
            "com.king.candycrushsaga",
            "com.linkedin.games",
            "com.ea.game.pvz2_row",
            "com.hcg.cok.gp",                       // Clash of Kings
            "com.igg.castleclash",
            // India specific
            "com.octro.teenpatti",                   // Teen Patti (IN)
            "com.octro.teenpattisocial",
            "com.adda52.rummy",                      // Rummy (IN)
            "com.mpl.sport.app",                     // MPL (IN)
            "in.winzo.games.lite",                   // WinZO (IN)
            "com.dreamgame.cricket",                 // Dream11 (IN fantasy)
            "com.dream11.android",
            "com.my11circle",
            // Chess / Board
            "com.chess",
            "com.google.android.apps.chess",
            "org.lichess.mobileapp",
            // Roblox / Minecraft
            "com.roblox.client",
            "com.mojang.minecraftpe",
        )

        @Volatile private var instance: ThermalUtils? = null

        fun getInstance(context: Context): ThermalUtils =
            instance ?: synchronized(this) {
                instance ?: ThermalUtils(context.applicationContext).also { instance = it }
            }
    }
}
