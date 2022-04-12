/*
 * Copyright (C) 2022 Eureka Team
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
 * limitations under the License.
 */
package com.eurekateam.samsungextras.flashlight

import android.os.Build
import android.os.Bundle
import android.os.PerformanceHintManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import android.content.SharedPreferences
import com.eurekateam.samsungextras.R
import com.eurekateam.samsungextras.interfaces.Flashlight
import com.eurekateam.samsungextras.preferences.CustomSeekBarPreference

class FlashLightFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    private lateinit var mFlashLightPref: CustomSeekBarPreference
    private lateinit var mSharedPreferences : SharedPreferences
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.flashlight_settings)
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        mFlashLightPref = findPreference(PREF_FLASHLIGHT)!!
        mFlashLightPref.onPreferenceChangeListener = this
        mFlashLightPref.setMax(10)
        mFlashLightPref.setMin(1)
        mFlashLightPref.value = Flashlight.getFlash(if (Build.DEVICE.contains("a10")) 1 else 0)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference == mFlashLightPref) {
            val value = newValue as Int
            Flashlight.setFlash(value)
            mSharedPreferences.edit().putInt(PREF_FLASHLIGHT, value).apply()
            return true
        }
        return false
    }

    companion object {
        const val PREF_FLASHLIGHT = "flashlight_pref"
    }
}
