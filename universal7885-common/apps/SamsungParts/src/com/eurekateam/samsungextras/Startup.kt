/*
 * Copyright (C) 2013 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.eurekateam.samsungextras

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager

class Startup : BroadcastReceiver() {
    override fun onReceive(context: Context, bootintent: Intent) {
        val enabled: Boolean
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        enabled = sharedPrefs.getBoolean(DeviceSettings.PREF_KEY_FPS_INFO, false)
        if (enabled) {
            context.startService(Intent(context, FPSInfoService::class.java))
        }
    }
}