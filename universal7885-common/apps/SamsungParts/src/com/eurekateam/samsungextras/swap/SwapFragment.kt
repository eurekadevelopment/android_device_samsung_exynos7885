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
import android.os.Handler
import android.os.Looper
import android.widget.Switch
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import com.android.settingslib.widget.MainSwitchPreference
import com.android.settingslib.widget.OnMainSwitchChangeListener
import com.eurekateam.samsungextras.R
import com.eurekateam.samsungextras.interfaces.Swap
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class SwapFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener, OnMainSwitchChangeListener {
    private lateinit var mSwapSizePref: SeekBarPreference
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mSwapEnable: MainSwitchPreference
    private lateinit var mFreeSpace: Preference
    private lateinit var mSwapFileSize: Preference
    private var mPoolExecutor = ScheduledThreadPoolExecutor(3)
    private var mSwapSize = 0
    private val mSwap = Swap()

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
        mSwapSizePref.showSeekBarValue = true
        mFreeSpace = findPreference(INFO_FREE_SPACE)!!
        mFreeSpace.summary = "${mSwap.getFreeSpace()} GB"
        mSwapFileSize = findPreference(INFO_SWAP_FILE_SIZE)!!
        mSwapFileSize.summary = "${mSwap.getSwapSize()} MB"
        mPoolExecutor.scheduleWithFixedDelay(mScheduler, 0, 3, TimeUnit.SECONDS)
    }

    private val mScheduler = Runnable { requireActivity().runOnUiThread {
       mSwapEnable.isEnabled = !mSwap.isLocked()
       mFreeSpace.summary = "${mSwap.getFreeSpace()} GB"
       mSwapFileSize.summary = "${mSwap.getSwapSize()} MB"
      } }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference == mSwapSizePref) {
            val value = newValue as Int
            mSwapSize = value
            mSharedPreferences.edit().putInt(PREF_SWAP_SIZE, value).apply()
            return true
        }
        return false
    }

    // This is called from native - DO NOT CHANGE SIGNATURE
    fun reactToCallbackNative(res: Boolean) {
        if (!res) {
            mSwap.delFile()
            mSharedPreferences.edit().putBoolean(PREF_SWAP_ENABLE, false).apply()
            mSwapSizePref.isEnabled = true
        }
    }

    override fun onSwitchChanged(switchView: Switch, isChecked: Boolean) {
        mSwapEnable.isEnabled = false
        val mSwap = Swap()
        if (isChecked) {
            mSwap.mkFile(mSwapSize)
            mSwap.setSwapOn(true)
        } else {
            mSwap.setSwapOff()
            mSwap.delFile()
        }
        mSwapEnable.isEnabled = true
        mSharedPreferences.edit().putBoolean(PREF_SWAP_ENABLE, isChecked).apply()
        mSwapSizePref.isEnabled = !isChecked
    }

    companion object {
        const val PREF_SWAP_SIZE = "swap_size"
        const val PREF_SWAP_ENABLE = "swap_enable"
        const val INFO_FREE_SPACE = "free_space"
        const val INFO_SWAP_FILE_SIZE = "swap_file_size"
    }
}
