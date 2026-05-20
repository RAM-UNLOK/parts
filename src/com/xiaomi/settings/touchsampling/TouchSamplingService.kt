/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 *
 * Background service that applies the high touch sampling rate (HTSR)
 * to the Xiaomi touch AIDL on screen-on and unlock events.
 *
 * Enabled state:   setTouchMode calls boost polling rate + sensitivity
 * Disabled state:  setTouchMode calls return hardware to baseline
 *
 * SharedPreferences key: HTSR_STATE in the SHAREDHTSR file.
 * The service is started at locked-boot via BootCompletedReceiver.
 */

package com.xiaomi.settings.touchsampling

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import vendor.xiaomi.hw.touchfeature.ITouchFeature

/** Maintains the high touch sampling rate state across screen-on events. */
class TouchSamplingService : Service() {

    private var touchFeature: ITouchFeature? = null
    private var screenUnlockReceiver: BroadcastReceiver? = null
    private var preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "TouchSamplingService started")
        initTouchFeature()
        registerScreenUnlockReceiver()
        registerPreferenceChangeListener()
        applyTouchSamplingRateFromPreferences()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (DEBUG) Log.d(TAG, "onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "TouchSamplingService stopped")
        runCatching { if (screenUnlockReceiver != null) unregisterReceiver(screenUnlockReceiver) }
        runCatching {
            getSharedPreferences(SHAREDHTSR, Context.MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    /** Binds to the Xiaomi TouchFeature AIDL service. Fails silently if unavailable. */
    private fun initTouchFeature() {
        runCatching {
            val fqName = ITouchFeature.DESCRIPTOR + "/default"
            val binder = android.os.Binder.allowBlocking(
                android.os.ServiceManager.waitForDeclaredService(fqName),
            )
            touchFeature = ITouchFeature.Stub.asInterface(binder)
            if (DEBUG) Log.d(TAG, "TouchFeature AIDL connected")
        }.onFailure { e ->
            Log.w(TAG, "Failed to bind TouchFeature AIDL: $e")
        }
    }

    /** Re-applies the touch sampling rate when the screen turns on or the user unlocks. */
    private fun registerScreenUnlockReceiver() {
        screenUnlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_USER_PRESENT ||
                    intent.action == Intent.ACTION_SCREEN_ON) {
                    Log.d(TAG, "Screen on / unlock — reapplying touch sampling rate")
                    applyTouchSamplingRateFromPreferences()
                }
            }
        }
        registerReceiver(
            screenUnlockReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(Intent.ACTION_SCREEN_ON)
            },
            Context.RECEIVER_NOT_EXPORTED,
        )
    }

    /** Reacts to SharedPreferences changes so the service stays in sync with the UI. */
    private fun registerPreferenceChangeListener() {
        val sharedPref = getSharedPreferences(SHAREDHTSR, Context.MODE_PRIVATE)
        preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == HTSR_STATE) {
                Log.d(TAG, "Preference $key changed — reapplying touch sampling rate")
                applyTouchSamplingRate(if (prefs.getBoolean(key, false)) 1 else 0)
            }
        }
        sharedPref.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    private fun applyTouchSamplingRateFromPreferences() {
        val enabled = getSharedPreferences(SHAREDHTSR, Context.MODE_PRIVATE)
            .getBoolean(HTSR_STATE, false)
        applyTouchSamplingRate(if (enabled) 1 else 0)
    }

    /**
     * Applies [state] (1 = boost, 0 = normal) to the touch hardware.
     * All setTouchMode calls are wrapped in runCatching so a single AIDL
     * failure does not crash the service.
     *
     * Mode indices correspond to ITouchFeature constants for garnet:
     *   0   = GAME_MODE         (main game-mode flag)
     *   1   = HIGH_RATE         (enable high polling rate path)
     *   2   = SENSITIVITY       (finger sensitivity; 99 = max)
     *   3   = REPORT_RATE       (touch report rate Hz; 34 = ~240 Hz)
     *   7   = POWER_SAVE        (invert: 0 = boost on, 1 = power save on)
     *   202 = SAMPLE_RATE_EXT   (extended polling rate toggle)
     */
    private fun applyTouchSamplingRate(state: Int) {
        val tf = touchFeature ?: run {
            Log.w(TAG, "TouchFeature AIDL not available — skipping rate apply")
            return
        }
        runCatching { tf.setTouchMode(0, 0,   state) }
        runCatching { tf.setTouchMode(0, 202, state) }
        runCatching { tf.setTouchMode(0, 1,   state) }
        runCatching { tf.setTouchMode(0, 3,   if (state == 1) 34 else 0) }
        runCatching { tf.setTouchMode(0, 2,   if (state == 1) 99 else 0) }
        runCatching { tf.setTouchMode(0, 7,   if (state == 1) 0  else 1) }
        if (DEBUG) Log.d(TAG, "Touch sampling rate applied: state=$state")
    }

    companion object {
        private const val TAG   = "TouchSamplingService"
        private val DEBUG       = Log.isLoggable(TAG, Log.DEBUG)

        /** SharedPreferences file name — must match TouchBoostScreen. */
        const val SHAREDHTSR = "htsr_prefs"
        /** Preference key for the HTSR enabled boolean. */
        const val HTSR_STATE = "htsr_enable"
    }
}
