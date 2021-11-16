/*
 * Copyright (C) 2020 The Xiaomi-SM6250 Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.eurekateam.samsungextras

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.eurekateam.samsungextras.interfaces.GPU

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED, ignoreCase = true)) {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val fpsenabled = sharedPrefs.getBoolean(DeviceSettings.PREF_KEY_FPS_INFO, false)
            if (fpsenabled) {
                context.startService(Intent(context, FPSInfoService::class.java))
            }
            val gpuenabled = sharedPrefs.getBoolean(DeviceSettings.PREF_GPUEXYNOS, true)
            if (gpuenabled) {
                GPU.GPU = 1
            }else{
                GPU.GPU = 0
            }
        }
    }
}