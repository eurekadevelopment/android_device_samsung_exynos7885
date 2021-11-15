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

package com.eurekateam.samsungextras.dolby;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.eurekateam.samsungextras.R;

public class DolbyFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String PREF_DOLBY = "dolby_pref";
    private static final String DOLBY_MODES = "dolby_modes";
    ListPreference DolbyModesPreference;
    SwitchPreference DolbyEnablePreference;
    DolbyCore dolbyCore = new DolbyCore();
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.dolby_settings);
        DolbyModesPreference = findPreference(DOLBY_MODES);
        DolbyEnablePreference = findPreference(PREF_DOLBY);
        assert DolbyEnablePreference != null;
        DolbyEnablePreference.setChecked(dolbyCore.isRunning());
        DolbyEnablePreference.setOnPreferenceChangeListener(this);
        CharSequence[] items = {
            "1", "2", "3", "4", "5", "6", "7", "8", "9"
        };
        DolbyModesPreference.setEntries(items);
        DolbyModesPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            switch ((int) newValue){
                case 1:
                    dolbyCore.startDolbyEffect(DolbyCore.PROFILE_AUTO);
                    break;
                case 2:
                    dolbyCore.startDolbyEffect(DolbyCore.PROFILE_GAME);
                    break;
                case 3:
                    dolbyCore.startDolbyEffect(DolbyCore.PROFILE_GAME_1);
                    break;
                case 4:
                    dolbyCore.startDolbyEffect(DolbyCore.PROFILE_GAME_2);
                    break;
                case 5:
                    dolbyCore.startDolbyEffect(DolbyCore.PROFILE_MOVIE);
                    break;
                case 6:
                    dolbyCore.startDolbyEffect(DolbyCore.PROFILE_MUSIC);
                    break;
                case 7:
                    dolbyCore.startDolbyEffect(DolbyCore.PROFILE_OFF);
                    break;
                case 8:
                    dolbyCore.startDolbyEffect(DolbyCore.PROFILE_VOICE);
                    break;
                case 9:
                    dolbyCore.startDolbyEffect(DolbyCore.PROFILE_SPACIAL_AUDIO);
                    break;
                default:
                    break;
            }
            return true;
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == DolbyEnablePreference) {
            Boolean value = (Boolean) newValue;
            if(value){
                dolbyCore.justStartOnly();
            }else{
                dolbyCore.stopDolbyEffect();
            }
            return true;
        }
        return false;
    }

}
