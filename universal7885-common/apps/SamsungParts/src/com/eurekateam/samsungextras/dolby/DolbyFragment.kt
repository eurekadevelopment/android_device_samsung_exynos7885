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
package com.eurekateam.samsungextras.dolby

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.eurekateam.samsungextras.R
import android.content.Intent

class DolbyFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    private var DolbyModesPreference: ListPreference? = null
    private var DolbyEnablePreference: SwitchPreference? = null
    private lateinit var mIntent : Intent
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.dolby_settings)
        DolbyModesPreference = findPreference(DOLBY_MODES)
        DolbyEnablePreference = findPreference(PREF_DOLBY)
        DolbyEnablePreference!!.isChecked = DolbyCore.mAudioEffect.enabled
        DolbyEnablePreference!!.onPreferenceChangeListener = this
        val items = arrayOf<CharSequence>(
            "0", "1", "2", "3"
        )
	mIntent = Intent(requireContext(), DolbyCore::class.java)
        DolbyModesPreference!!.entryValues = items
        DolbyModesPreference!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                when ((newValue as String).toInt()) {
                    0 -> mIntent.putExtra(DolbyCore.DAP_PROFILE, DolbyCore.PROFILE_AUTO)
                    1 -> mIntent.putExtra(DolbyCore.DAP_PROFILE, DolbyCore.PROFILE_MOVIE)
                    2 -> mIntent.putExtra(DolbyCore.DAP_PROFILE, DolbyCore.PROFILE_MUSIC)
                    3 -> mIntent.putExtra(DolbyCore.DAP_PROFILE, DolbyCore.PROFILE_VOICE)
                    else -> {
                    }
                }
		mIntent.putExtra(DolbyCore.DAP_ENABLED, true)
		requireContext().startService(mIntent)
                true
            }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference == DolbyEnablePreference) {
            val value = newValue as Boolean
            mIntent.putExtra(DolbyCore.DAP_ENABLED, value)
	    requireContext().startService(mIntent)
            return true
        }
        return false
    }

    companion object {
        private const val PREF_DOLBY = "dolby_pref"
        private const val DOLBY_MODES = "dolby_modes"
    }
}
