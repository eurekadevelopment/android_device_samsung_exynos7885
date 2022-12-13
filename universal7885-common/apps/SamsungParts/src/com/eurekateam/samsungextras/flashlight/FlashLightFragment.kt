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

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Switch
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import com.android.settingslib.widget.MainSwitchPreference
import com.android.settingslib.widget.OnMainSwitchChangeListener
import com.eurekateam.samsungextras.R
import com.eurekateam.samsungextras.interfaces.FlashLight

class FlashLightFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener, OnMainSwitchChangeListener {
    private lateinit var mFlashLightPref: SeekBarPreference
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mFlashLightEnable: MainSwitchPreference
    private val mFlashLight = FlashLight()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.flashlight_settings)
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        mFlashLightPref = findPreference(PREF_FLASHLIGHT)!!
        mFlashLightPref.onPreferenceChangeListener = this
        mFlashLightPref.setMax(10)
        mFlashLightPref.setMin(1)
        mFlashLightPref.value = mFlashLight.getFlash(Build.DEVICE.contains("a10"))
        mFlashLightPref.showSeekBarValue = true
        mFlashLightEnable = findPreference(PREF_FLASHLIGHT_ENABLE)!!
        mFlashLightEnable.isChecked = mSharedPreferences.getBoolean(PREF_FLASHLIGHT_ENABLE, true)
        mFlashLightEnable.addOnSwitchChangeListener(this)
        mFlashLightPref.isEnabled = mFlashLightEnable.isChecked
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference == mFlashLightPref) {
            val value = newValue as Int
            mFlashLight.setFlash(value)
            mSharedPreferences.edit().putInt(PREF_FLASHLIGHT, value).apply()
            return true
        }
        return false
    }

    override fun onSwitchChanged(switchView: Switch, isChecked: Boolean) {
        mFlashLight.setEnabled(isChecked)
        mSharedPreferences.edit().putBoolean(PREF_FLASHLIGHT_ENABLE, isChecked)
        mFlashLightPref.isEnabled = isChecked
    }

    companion object {
        const val PREF_FLASHLIGHT = "flashlight_pref"
        const val PREF_FLASHLIGHT_ENABLE = "flashlight_enable"
    }
}
