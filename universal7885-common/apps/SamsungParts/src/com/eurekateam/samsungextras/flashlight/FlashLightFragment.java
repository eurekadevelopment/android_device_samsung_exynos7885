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

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;

import com.eurekateam.samsungextras.GlobalConstants;
import com.eurekateam.samsungextras.R;
import com.eurekateam.samsungextras.interfaces.Flashlight;
import com.eurekateam.samsungextras.preferences.CustomSeekBarPreference;
import com.eurekateam.samsungextras.utils.FileUtilsWrapper;
import com.eurekateam.samsungextras.utils.SystemProperties;

public class FlashLightFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String PREF_FLASHLIGHT = "flashlight_pref";
    private CustomSeekBarPreference mFlashLightPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.flashlight_settings);
        mFlashLightPref = findPreference(PREF_FLASHLIGHT);
        assert mFlashLightPref != null;
        mFlashLightPref.setOnPreferenceChangeListener(this);
        if (!FileUtilsWrapper.fileExists(GlobalConstants.FLASHLIGHT_SYSFS)){
            mFlashLightPref.setEnabled(false);
        }
        mFlashLightPref.setMax(10);
        mFlashLightPref.setMin(0);
        boolean isa10 = SystemProperties.read("ro.product.device").equals("a10");
        mFlashLightPref.setValue(Flashlight.getFlash(isa10 ? 1 : 0));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFlashLightPref) {
            Integer value = (Integer) newValue;
            Flashlight.setFlash(value);
            mFlashLightPref.setValue(value, true);
            return true;
        }
        return false;
    }

}
