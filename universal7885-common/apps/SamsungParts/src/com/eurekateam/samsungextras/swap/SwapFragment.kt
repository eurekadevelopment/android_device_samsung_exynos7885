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
package com.eurekateam.samsungextras.swap

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Switch
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import com.android.settingslib.widget.MainSwitchPreference
import com.android.settingslib.widget.OnMainSwitchChangeListener
import com.eurekateam.samsungextras.R
import com.eurekateam.samsungextras.interfaces.Swap
import java.lang.Thread

class SwapFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener, OnMainSwitchChangeListener {
    private lateinit var mSwapSizePref: SeekBarPreference
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mSwapEnable: MainSwitchPreference
    private lateinit var mFreeSpace: Preference
    private lateinit var mSwapFileSize: Preference
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.swap_settings)
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        mSwapSizePref = findPreference(PREF_SWAP_SIZE)!!
        mSwapSizePref.onPreferenceChangeListener = this
        mSwapSizePref.setMax(100)
        mSwapSizePref.setMin(10)
        mSwapSizePref.value = mSharedPreferences.getInt(PREF_SWAP_SIZE, 50)
        mSwapEnable = findPreference(PREF_SWAP_ENABLE)!!
        mSwapEnable.isChecked = mSharedPreferences.getBoolean(PREF_SWAP_ENABLE, false)
        mSwapEnable.addOnSwitchChangeListener(this)
        mSwapSizePref.isEnabled = !mSwapEnable.isChecked
        mFreeSpace = findPreference(INFO_FREE_SPACE)!!
        mFreeSpace.summary = "${Swap.getFreeSpace()} GB"
        mSwapFileSize = findPreference(INFO_SWAP_FILE_SIZE)!!
        mSwapFileSize.summary = "${Swap.getSwapSize()} MB"
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference == mSwapSizePref) {
            val value = newValue as Int
            Swap.setSize(value)
            mSharedPreferences.edit().putInt(PREF_SWAP_SIZE, value).apply()
            mFreeSpace.summary = "${Swap.getFreeSpace()} GB"
            mSwapFileSize.summary = "${Swap.getSwapSize()} MB"
            return true
        }
        return false
    }

    override fun onSwitchChanged(switchView: Switch, isChecked: Boolean) {
        mSwapEnable.isEnabled = false
        Thread {
            Swap.setSwapOn(isChecked)
            requireActivity().runOnUiThread {
                mSwapEnable.isEnabled = true
                mSharedPreferences.edit().putBoolean(PREF_SWAP_ENABLE, isChecked).apply()
                mSwapSizePref.isEnabled = !isChecked
		mFreeSpace.summary = "${Swap.getFreeSpace()} GB"
		mSwapFileSize.summary = "${Swap.getSwapSize()} MB"
            }
        }.start()
    } 
    companion object {
        const val PREF_SWAP_SIZE = "swap_size"
        const val PREF_SWAP_ENABLE = "swap_enable"
        const val INFO_FREE_SPACE = "free_space"
        const val INFO_SWAP_FILE_SIZE = "swap_file_size"
    }
}
