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

package com.eurekateam.samsungextras.flashlight;

import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;

import com.eurekateam.samsungextras.GlobalConstants;
import com.eurekateam.samsungextras.R;
import com.eurekateam.samsungextras.preferences.CustomSeekBarPreference;
import com.eurekateam.samsungextras.utils.FileUtilsWrapper;
import com.eurekateam.samsungextras.utils.SystemProperties;

public class FlashLightFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static int divideprefix = 21;
    private static final String PREF_FLASHLIGHT = "flashlight_pref";
    private CustomSeekBarPreference mFlashLightPref;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.flashlight_settings);
        if (SystemProperties.read("ro.product.device").contains("a10")){
            divideprefix = 1;
        }
        mFlashLightPref = findPreference(PREF_FLASHLIGHT);
        assert mFlashLightPref != null;
        mFlashLightPref.setOnPreferenceChangeListener(this);
        mFlashLightPref.setMax(10);
        mFlashLightPref.setMin(0);
        mFlashLightPref.setEnabled(FileUtilsWrapper.
                fileExists(GlobalConstants.FLASHLIGHT_SYSFS));
        if (FileUtilsWrapper.isFileReadable(GlobalConstants.FLASHLIGHT_SYSFS)){
            Log.d(GlobalConstants.TAG, "onCreatePreferences: " +
                    GlobalConstants.FLASHLIGHT_SYSFS + " readable, value " +
                    Integer.parseInt(
                            FileUtilsWrapper.readOneLine(GlobalConstants.FLASHLIGHT_SYSFS)));
            mFlashLightPref.setValue(Integer.parseInt(
                    FileUtilsWrapper.readOneLine(GlobalConstants.FLASHLIGHT_SYSFS))
                    / divideprefix);
        }else{
            Log.w(GlobalConstants.TAG, "onCreatePreferences: " +
                    GlobalConstants.FLASHLIGHT_SYSFS + " not readable");
            mFlashLightPref.setEnabled(false);
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFlashLightPref) {
            Integer value = (Integer) newValue;
            Log.d(GlobalConstants.TAG, "onPreferenceChange: writing 1 to " +
                    GlobalConstants.FLASHLIGHT_SYSFS_ENABLE);
            FileUtilsWrapper.writeLine(GlobalConstants.FLASHLIGHT_SYSFS_ENABLE, "1");
            Log.d(GlobalConstants.TAG, "onPreferenceChange: writing " + value
                    + " to " + GlobalConstants.FLASHLIGHT_SYSFS);
            FileUtilsWrapper.writeLine(GlobalConstants.FLASHLIGHT_SYSFS, String.valueOf(value));
            mFlashLightPref.setValue(value, true);
            return true;
        }
        return false;
    }

}
