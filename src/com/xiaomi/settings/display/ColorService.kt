/*
 * SPDX-FileCopyrightText: 2023-2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * Background service that keeps the display colour mode in sync with
 * Settings.System.DISPLAY_COLOR_MODE.
 *
 * Responsibilities:
 *  - Observe DISPLAY_COLOR_MODE changes via ContentObserver
 *  - Apply the corresponding DisplayFeature AIDL call
 *  - On AOD (screen-off with AOD enabled) force STANDARD mode to avoid
 *    incorrect colour rendering while in ambient display
 *  - Restore the user-selected mode on screen-on after AOD
 */

package com.xiaomi.settings.display

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.hardware.display.AmbientDisplayConfiguration
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemProperties
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.os.postDelayed

class ColorService : Service() {

    companion object {
        private const val TAG = "ColorService"
        private val DEBUG = Log.isLoggable(TAG, Log.DEBUG)

        /** System property set by the device tree for the default colour mode id. */
        private val DEFAULT_COLOR_MODE =
            SystemProperties.getInt("persist.sys.sf.native_mode", 0)
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var ambientConfig: AmbientDisplayConfiguration
    private var isDozing = false

    /** Re-applies the current colour mode whenever the setting changes. */
    private val settingObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            if (DEBUG) Log.d(TAG, "SettingObserver: onChange")
            setCurrentColorMode()
        }
    }

    /** Handles screen-on (exit AOD) and screen-off (enter AOD) events. */
    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (DEBUG) Log.d(TAG, "onReceive: ${intent.action}")
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> {
                    if (isDozing) {
                        isDozing = false
                        handler.removeCallbacksAndMessages(null)
                        // Short delay so the display pipeline is fully awake
                        handler.postDelayed(100) {
                            if (DEBUG) Log.d(TAG, "Exiting AOD — restoring colour mode")
                            setCurrentColorMode()
                        }
                    }
                }
                Intent.ACTION_SCREEN_OFF -> {
                    if (!ambientConfig.alwaysOnEnabled(UserHandle.USER_CURRENT)) {
                        if (DEBUG) Log.d(TAG, "AOD not enabled — no colour override needed")
                        isDozing = false
                        return
                    }
                    isDozing = true
                    handler.removeCallbacksAndMessages(null)
                    if (DEBUG) Log.d(TAG, "Entering AOD — forcing STANDARD colour mode")
                    handler.post { ColorMode.STANDARD.setCurrent() }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (DEBUG) Log.d(TAG, "onCreate")
        ambientConfig = AmbientDisplayConfiguration(this)

        contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.DISPLAY_COLOR_MODE),
            /* notifyForDescendants= */ false,
            settingObserver,
            UserHandle.USER_CURRENT,
        )

        registerReceiver(
            screenStateReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            },
        )

        setCurrentColorMode()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (DEBUG) Log.d(TAG, "onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy")
        contentResolver.unregisterContentObserver(settingObserver)
        unregisterReceiver(screenStateReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /** Reads the current colour mode from Settings and applies it via the AIDL. */
    private fun setCurrentColorMode() {
        if (isDozing) {
            if (DEBUG) Log.d(TAG, "setCurrentColorMode: skipping — in AOD")
            return
        }
        val colorModeId = Settings.System.getIntForUser(
            contentResolver,
            Settings.System.DISPLAY_COLOR_MODE,
            DEFAULT_COLOR_MODE,
            UserHandle.USER_CURRENT,
        )
        val mode = ColorMode.fromId(colorModeId) ?: run {
            Log.e(TAG, "Unknown colour mode id: $colorModeId")
            return
        }
        if (DEBUG) Log.d(TAG, "setCurrentColorMode: $mode")
        handler.post { mode.setCurrent() }
    }

    /**
     * All supported colour modes for the Xiaomi garnet display pipeline.
     *
     * Each entry maps a [Settings.System.DISPLAY_COLOR_MODE] id to the
     * DisplayFeature AIDL (mode, value, cookie) tuple.
     *
     * Expert modes additionally require a secondary AIDL call to switch
     * the panel into its expert colour space path.
     *
     * @param id       Settings.System.DISPLAY_COLOR_MODE value
     * @param mode     IDisplayFeature mode parameter
     * @param value    IDisplayFeature value parameter
     * @param cookie   IDisplayFeature cookie parameter
     * @param isExpert Whether a second AIDL call is required
     */
    enum class ColorMode(
        val id: Int,
        val mode: Int,
        val value: Int,
        val cookie: Int,
        val isExpert: Boolean = false,
    ) {
        VIVID    (258, 0,  2, 255),
        SATURATED(256, 1,  2, 255),
        STANDARD (257, 2,  2, 255),
        ORIGINAL (269, 26, 1, 0,   isExpert = true),
        P3       (268, 26, 2, 0,   isExpert = true),
        SRGB     (267, 26, 3, 0,   isExpert = true);

        /** Applies this colour mode to the display hardware. */
        fun setCurrent() {
            DisplayFeatureWrapper.setFeature(mode, value, cookie)
            if (isExpert) {
                // Expert modes require a second call to enter the expert gamut path
                DisplayFeatureWrapper.setFeature(EXPERT_MODE, EXPERT_VALUE, EXPERT_COOKIE)
            }
        }

        companion object {
            private const val EXPERT_MODE   = 26
            private const val EXPERT_VALUE  = 0
            private const val EXPERT_COOKIE = 10

            fun fromId(id: Int): ColorMode? = values().find { it.id == id }
        }
    }
}
