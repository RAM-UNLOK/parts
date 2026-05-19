/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 *
 * Listens for charger connect / disconnect events and gates the entire
 * thermal profile system through ThermalUtils.isCharging.
 *
 * Flow:
 *   POWER_CONNECTED
 *     1. Sets ThermalUtils.isCharging = true
 *        → ThermalUtils.setThermalProfile() becomes a no-op
 *        → ThermalUtils.setDefaultThermalProfile() becomes a no-op
 *     2. Writes sconfig=27 (thermal-charge.conf) to the kernel node
 *        → No subsequent foreground app switch can overwrite this
 *
 *   POWER_DISCONNECTED
 *     1. Sets ThermalUtils.isCharging = false
 *        → The setter immediately calls setThermalProfileInternal(currentApp)
 *           so the correct per-app profile is restored in the same atomic step
 *        → No gap window where normal.conf could linger
 *
 * Declared statically in AndroidManifest — fires even if XiaomiParts
 * has never been opened by the user.
 */

package com.xiaomi.settings.thermal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.xiaomi.settings.utils.dlog
import com.xiaomi.settings.utils.writeLine

class ChargingThermalReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val thermalUtils = ThermalUtils.getInstance(context)

        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                dlog(TAG, "Charger connected — locking thermal to charge profile")

                // 1. Gate ThermalService writes FIRST so there is no race window
                thermalUtils.isCharging = true

                // 2. Write charge profile to hardware
                runCatching {
                    writeLine(THERMAL_SCONFIG, SCONFIG_CHARGE)
                    dlog(TAG, "sconfig=$SCONFIG_CHARGE (thermal-charge.conf) applied")
                }.onFailure { e ->
                    Log.e(TAG, "Failed to write charge thermal profile", e)
                }
            }

            Intent.ACTION_POWER_DISCONNECTED -> {
                dlog(TAG, "Charger disconnected — unlocking thermal profiles")

                // Setting isCharging = false triggers setThermalProfileInternal(currentApp)
                // inside the ThermalUtils setter, restoring the correct per-app profile
                // atomically without a separate writeLine call needed here.
                thermalUtils.isCharging = false
            }
        }
    }

    companion object {
        private const val TAG             = "ChargingThermalReceiver"
        private const val THERMAL_SCONFIG = "/sys/devices/virtual/thermal/thermal_message/sconfig"
        /** sconfig 27 → thermal-charge.conf */
        private const val SCONFIG_CHARGE  = "27"
    }
}
