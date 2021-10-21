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

package com.eurekateam.samsungextras.battery;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.eurekateam.samsungextras.GlobalConstants;
import com.eurekateam.samsungextras.R;
import com.eurekateam.samsungextras.utils.FileUtilsWrapper;

public class BatteryFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String PREF_FASTCHARGE = "fastcharge_pref";
    private static final String PREF_CHARGE = "charge_pref";
    private static final String BATTERY_INFO = "battery_info";
    private SwitchPreference mFastChargePref;
    private SwitchPreference mChargePref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.battery_settings);
        mFastChargePref = findPreference(PREF_FASTCHARGE);
        assert mFastChargePref != null;
        mFastChargePref.setOnPreferenceChangeListener(this);
        mFastChargePref.setEnabled(FileUtilsWrapper.
                fileExists(GlobalConstants.FASTCHARGE_SYSFS));
        if (FileUtilsWrapper.isFileReadable(GlobalConstants.FLASHLIGHT_SYSFS)){
            Log.d(GlobalConstants.TAG, "onCreatePreferences: " +
                    GlobalConstants.FASTCHARGE_SYSFS + " readable, value " +
                            FileUtilsWrapper.readOneLine(GlobalConstants.FASTCHARGE_SYSFS));
            mFastChargePref.setChecked(FileUtilsWrapper.readOneLine
                    (GlobalConstants.FASTCHARGE_SYSFS).equals("0"));
        }else{
            Log.w(GlobalConstants.TAG, "onCreatePreferences: " +
                    GlobalConstants.FLASHLIGHT_SYSFS + " not readable");
            mFastChargePref.setEnabled(false);
        }
        mChargePref = findPreference(PREF_CHARGE);
        assert mChargePref != null;
        mChargePref.setOnPreferenceChangeListener(this);
        mChargePref.setEnabled(FileUtilsWrapper.
                fileExists(GlobalConstants.CHARGE_DISABLE_SYSFS));
        if (FileUtilsWrapper.isFileReadable(GlobalConstants.CHARGE_DISABLE_SYSFS)){
            Log.d(GlobalConstants.TAG, "onCreatePreferences: " +
                    GlobalConstants.CHARGE_DISABLE_SYSFS + " readable, value " +
                            FileUtilsWrapper.readOneLine(GlobalConstants.CHARGE_DISABLE_SYSFS));
            mChargePref.setChecked(FileUtilsWrapper.readOneLine
                    (GlobalConstants.CHARGE_DISABLE_SYSFS).equals("0"));
        }else{
            Log.w(GlobalConstants.TAG, "onCreatePreferences: " +
                    GlobalConstants.FLASHLIGHT_SYSFS + " not readable");
            mFastChargePref.setEnabled(false);
        }
        ListPreference mBatteryInfo = findPreference(BATTERY_INFO);
        CharSequence[] items = {
                Integer.parseInt(FileUtilsWrapper.readOneLine(GlobalConstants.BATTERY_CAPACITY_MAX_SYSFS)) / 1000 + " mAh",
                FileUtilsWrapper.readOneLine(GlobalConstants.BATTERY_CAPACITY_CURRENT_SYSFS) + " %",
                Float.parseFloat(FileUtilsWrapper.readOneLine(GlobalConstants.BATTERY_CAPACITY_MAX_SYSFS)) *
                        Float.parseFloat(FileUtilsWrapper.readOneLine(GlobalConstants.BATTERY_CAPACITY_CURRENT_SYSFS)) / 100000 + " mAh" ,
                FileUtilsWrapper.readOneLine(GlobalConstants.BATTERY_CURRENT_SYSFS) + " mAh",
                Integer.parseInt(FileUtilsWrapper.readOneLine(GlobalConstants.BATTERY_CURRENT_SYSFS)) >= 0
                        ? "Charging" : "Discharging",
                Float.parseFloat(FileUtilsWrapper.readOneLine(GlobalConstants.BATTERY_TEMP_SYSFS)) / 10 + " \u2103",
        };
        assert mBatteryInfo != null;
        mBatteryInfo.setEntryValues(items);
        mBatteryInfo.setOnPreferenceChangeListener((preference, newValue) -> {
            Toast.makeText(getContext(), (String) newValue, Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFastChargePref) {
            Boolean value = (Boolean) newValue;
            Log.d(GlobalConstants.TAG, "onPreferenceChange: writing " + value + " to " +
                    GlobalConstants.FASTCHARGE_SYSFS);
            FileUtilsWrapper.writeLine(GlobalConstants.FASTCHARGE_SYSFS, value ?  "0" : "1");
            mFastChargePref.setChecked(FileUtilsWrapper.readOneLine
                    (GlobalConstants.FASTCHARGE_SYSFS).equals("0"));
            return true;
        }else if (preference == mChargePref){
            Boolean value = (Boolean) newValue;
            Log.d(GlobalConstants.TAG, "onPreferenceChange: writing " + value
                    + " to " + GlobalConstants.CHARGE_DISABLE_SYSFS);
            FileUtilsWrapper.writeLine(GlobalConstants.CHARGE_DISABLE_SYSFS, value ?  "0" : "1");
            mChargePref.setChecked(FileUtilsWrapper.readOneLine
                    (GlobalConstants.CHARGE_DISABLE_SYSFS).equals("0"));
            return true;
        }
        return false;
    }

}
