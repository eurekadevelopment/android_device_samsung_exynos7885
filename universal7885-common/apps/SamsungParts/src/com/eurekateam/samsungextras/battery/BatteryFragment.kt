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
package com.eurekateam.samsungextras.battery

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.eurekateam.samsungextras.R
import com.eurekateam.samsungextras.interfaces.Battery

class BatteryFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    private lateinit var mFastChargePref: SwitchPreference
    private lateinit var mChargePref: SwitchPreference
    private lateinit var mSharedPreferences: SharedPreferences
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.battery_settings)
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        mFastChargePref = findPreference(PREF_FASTCHARGE)!!
        mFastChargePref.onPreferenceChangeListener = this
        mFastChargePref.isChecked = mSharedPreferences.getBoolean(PREF_FASTCHARGE, true)
        mChargePref = findPreference(PREF_CHARGE)!!
        mChargePref.onPreferenceChangeListener = this
        mChargePref.isChecked = mSharedPreferences.getBoolean(PREF_CHARGE, true)
        val mBatteryInfo: ListPreference = findPreference(BATTERY_INFO)!!
        val items = arrayOf<CharSequence>(
            Battery.getGeneralBatteryStats(1).toString() + " mAh",
            Battery.getGeneralBatteryStats(2).toString() + " %",
            Battery.getGeneralBatteryStats(3).toString() + " mAh",
            Battery.getGeneralBatteryStats(6).toString() + " mAh",
            if (Battery.getGeneralBatteryStats(4) == 1) "Charging" else "Discharging",
            Battery.getGeneralBatteryStats(5).toString() + " \u2103"
        )
        mBatteryInfo.entryValues = items
        mBatteryInfo.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any? ->
                Toast.makeText(
                    context, newValue as String, Toast.LENGTH_SHORT
                ).show()
                true
            }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference === mChargePref) {
            val value = newValue as Boolean
            Battery.chargeSysfs = if (value) 0 else 1
            mChargePref.isChecked = Battery.chargeSysfs == 0
            mSharedPreferences.edit().putBoolean(PREF_CHARGE, Battery.chargeSysfs == 0).apply()
            return true
        } else if (preference === mFastChargePref) {
            val value = newValue as Boolean
            Battery.setFastCharge(if (value) 0 else 1)
            mFastChargePref.isChecked = Battery.fastChargeSysfs == 0
            mSharedPreferences.edit().putBoolean(PREF_FASTCHARGE, Battery.fastChargeSysfs == 0).apply()
            return true
        }
        return false
    }

    companion object {
        const val PREF_FASTCHARGE = "fastcharge_pref"
        const val PREF_CHARGE = "charge_pref"
        private const val BATTERY_INFO = "battery_info"
    }
}
