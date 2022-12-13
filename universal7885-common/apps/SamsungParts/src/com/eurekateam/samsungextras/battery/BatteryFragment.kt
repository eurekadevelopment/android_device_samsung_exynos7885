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
import android.widget.Switch
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.android.settingslib.widget.MainSwitchPreference
import com.android.settingslib.widget.OnMainSwitchChangeListener
import com.eurekateam.samsungextras.R
import com.eurekateam.samsungextras.interfaces.Battery
import com.eurekateam.samsungextras.interfaces.BatteryIds
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class BatteryFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener, OnMainSwitchChangeListener {
    private lateinit var mFastChargePref: SwitchPreference
    private var mPoolExecutor: ScheduledThreadPoolExecutor? = null
    private lateinit var mChargePref: SwitchPreference
    private lateinit var mShowDataPref: MainSwitchPreference
    private lateinit var mSharedPreferences: SharedPreferences
    private val mBattery = Battery()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.battery_settings)
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        mFastChargePref = findPreference(PREF_FASTCHARGE)!!
        mFastChargePref.onPreferenceChangeListener = this
        mFastChargePref.isChecked = mSharedPreferences.getBoolean(PREF_FASTCHARGE, true)
        mChargePref = findPreference(PREF_CHARGE)!!
        mChargePref.onPreferenceChangeListener = this
        mChargePref.isChecked = mSharedPreferences.getBoolean(PREF_CHARGE, true)
        mShowDataPref = findPreference(PREF_SHOW_DATA)!!
        mShowDataPref.isChecked = mSharedPreferences.getBoolean(PREF_SHOW_DATA, true)
        mShowDataPref.addOnSwitchChangeListener(this)
        if (mShowDataPref.isChecked) {
            mPoolExecutor = ScheduledThreadPoolExecutor(3)
            mPoolExecutor!!.scheduleWithFixedDelay(mScheduler, 0, 2, TimeUnit.SECONDS)
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference == mChargePref) {
            mBattery.Charge = newValue as Boolean
            mChargePref.isChecked = mBattery.Charge
            mSharedPreferences.edit().putBoolean(PREF_CHARGE, mBattery.Charge).apply()
            return true
        } else if (preference == mFastChargePref) {
            mBattery.FastCharge = newValue as Boolean
            mFastChargePref.isChecked = mBattery.FastCharge
            mSharedPreferences.edit().putBoolean(PREF_FASTCHARGE, mBattery.FastCharge).apply()
            return true
        }
        return false
    }
    private val mScheduler = Runnable {
        requireActivity().runOnUiThread {
            findPreference<Preference>(INFO_MAX_CAP)!!.summary = mBattery.getGeneralBatteryStats(BatteryIds.BATTERY_CAPACITY_MAX).toString() + " mAh"
            findPreference<Preference>(INFO_CHARGED_UP_TO)!!.summary = mBattery.getGeneralBatteryStats(BatteryIds.BATTERY_CAPACITY_CURRENT).toString() + " %"
            findPreference<Preference>(INFO_CHARGED_MAH)!!.summary = mBattery.getGeneralBatteryStats(BatteryIds.BATTERY_CAPACITY_CURRENT_MAH).toString() + " mAh"
            findPreference<Preference>(INFO_CURRENT)!!.summary = mBattery.getGeneralBatteryStats(BatteryIds.BATTERY_CURRENT).toString() + " mA"
            findPreference<Preference>(INFO_STATUS)!!.summary = if (mBattery.getGeneralBatteryStats(BatteryIds.CHARGING_STATE) == 1) "Charging" else "Discharging"
            findPreference<Preference>(INFO_TEMP)!!.summary = mBattery.getGeneralBatteryStats(BatteryIds.BATTERY_TEMP).toString() + " \u2103"
        }
    }
    override fun onSwitchChanged(switchView: Switch, isChecked: Boolean) {
        if (isChecked) {
            if (mPoolExecutor == null) {
                mPoolExecutor = ScheduledThreadPoolExecutor(2)
                mPoolExecutor!!.scheduleWithFixedDelay(
                    mScheduler,
                    0,
                    2,
                    TimeUnit.SECONDS
                )
            }
        } else {
            if (mPoolExecutor != null) {
                mPoolExecutor!!.shutdown()
                mPoolExecutor = null
            }
            val mNotShown = requireContext().getString(R.string.not_shown)
            findPreference<Preference>(INFO_MAX_CAP)!!.summary = mNotShown
            findPreference<Preference>(INFO_CHARGED_UP_TO)!!.summary = mNotShown
            findPreference<Preference>(INFO_CHARGED_MAH)!!.summary = mNotShown
            findPreference<Preference>(INFO_CURRENT)!!.summary = mNotShown
            findPreference<Preference>(INFO_STATUS)!!.summary = mNotShown
            findPreference<Preference>(INFO_TEMP)!!.summary = mNotShown
        }
        mSharedPreferences.edit().putBoolean(PREF_SHOW_DATA, isChecked).apply()
    }

    companion object {
        const val PREF_FASTCHARGE = "fastcharge_pref"
        const val PREF_CHARGE = "charge_pref"
        const val INFO_MAX_CAP = "battery_capacity"
        const val INFO_CHARGED_UP_TO = "battery_charged_up_to"
        const val INFO_CHARGED_MAH = "battery_charged_up_to_mah"
        const val INFO_CURRENT = "battery_current"
        const val INFO_STATUS = "battery_status"
        const val INFO_TEMP = "battery_temp"
        const val PREF_SHOW_DATA = "show_data"
        private const val BATTERY_INFO = "battery_info"
    }
}
