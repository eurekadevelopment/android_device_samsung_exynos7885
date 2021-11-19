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
package com.eurekateam.samsungextras.battery

import android.os.Bundle
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.eurekateam.samsungextras.R
import com.eurekateam.samsungextras.interfaces.Battery

class BatteryFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    private var mFastChargePref: SwitchPreference? = null
    private var mChargePref: SwitchPreference? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.battery_settings)
        mFastChargePref = findPreference(PREF_FASTCHARGE)
        assert(mFastChargePref != null)
        mFastChargePref!!.onPreferenceChangeListener = this
        mFastChargePref!!.isChecked = Battery.chargeSysfs == 0
        mChargePref = findPreference(PREF_CHARGE)
        assert(mChargePref != null)
        mChargePref!!.onPreferenceChangeListener = this
        mChargePref!!.isChecked = Battery.fastChargeSysfs == 0
        val mBatteryInfo : ListPreference = findPreference(BATTERY_INFO)!!
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
            mChargePref!!.isChecked = Battery.chargeSysfs == 0
            return true
        } else if (preference === mFastChargePref) {
            val value = newValue as Boolean
            Battery.setFastCharge(if (value) 0 else 1)
            mFastChargePref!!.isChecked =
                Battery.fastChargeSysfs == 0
            return true
        }
        return false
    }

    companion object {
        private const val PREF_FASTCHARGE = "fastcharge_pref"
        private const val PREF_CHARGE = "charge_pref"
        private const val BATTERY_INFO = "battery_info"
    }
}