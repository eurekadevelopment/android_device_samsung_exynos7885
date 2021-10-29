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
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.eurekateam.samsungextras.R;
import com.eurekateam.samsungextras.interfaces.Battery;

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
        mFastChargePref.setChecked(Battery.getChargeSysfs() == 0);
        mChargePref = findPreference(PREF_CHARGE);
        assert mChargePref != null;
        mChargePref.setOnPreferenceChangeListener(this);
        mChargePref.setChecked(Battery.getFastChargeSysfs() == 0);
        ListPreference mBatteryInfo = findPreference(BATTERY_INFO);
        CharSequence[] items = {
                Battery.getGeneralBatteryStats(1) + " mAh",
                Battery.getGeneralBatteryStats(2) + " %",
                Battery.getGeneralBatteryStats(3) + " mAh" ,
                Battery.getGeneralBatteryStats(6) + " mAh",
                Battery.getGeneralBatteryStats(4) == 1
                        ? "Charging" : "Discharging",
                Battery.getGeneralBatteryStats(5) + " \u2103",
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
        if (preference == mChargePref) {
            Boolean value = (Boolean) newValue;
            Battery.setChargeSysfs(value ?  0 : 1 );
            mChargePref.setChecked(Battery.getChargeSysfs() == 0);
            return true;
        }else if (preference == mFastChargePref){
            Boolean value = (Boolean) newValue;
            Battery.setFastCharge(value ?  0 : 1 );
            mFastChargePref.setChecked(Battery.getFastChargeSysfs() == 0);
            return true;
        }
        return false;
    }

}
