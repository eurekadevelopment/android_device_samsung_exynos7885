/*
 * Copyright (C) 2020 Paranoid Android
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

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.eurekateam.samsungextras.GlobalConstants
import com.eurekateam.samsungextras.R
import com.eurekateam.samsungextras.interfaces.Flashlight
import com.eurekateam.samsungextras.preferences.CustomSeekBarPreference
import com.eurekateam.samsungextras.utils.SystemProperties
import java.io.File

class FlashLightFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    private var mFlashLightPref: CustomSeekBarPreference? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.flashlight_settings)
        mFlashLightPref = findPreference(PREF_FLASHLIGHT)
        assert(mFlashLightPref != null)
        mFlashLightPref!!.onPreferenceChangeListener = this
        if (!File(GlobalConstants.FLASHLIGHT_SYSFS).exists()) {
            mFlashLightPref!!.isEnabled = false
        }
        mFlashLightPref!!.setMax(10)
        mFlashLightPref!!.setMin(1)
        val isa10 = SystemProperties.read("ro.product.device") == "a10"
        mFlashLightPref!!.value = Flashlight.getFlash(if (isa10) 1 else 0)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference === mFlashLightPref) {
            val value = newValue as Int
            Flashlight.setFlash(value)
            mFlashLightPref!!.setValue(value, true)
            return true
        }
        return false
    }

    companion object {
        private const val PREF_FLASHLIGHT = "flashlight_pref"
    }
}