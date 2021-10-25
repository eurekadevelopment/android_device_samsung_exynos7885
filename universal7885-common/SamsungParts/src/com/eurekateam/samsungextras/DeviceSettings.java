/*
 * Copyright (C) 2020 The Xiaomi-SM6250 Project
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
 * limitations under the License
 */

package com.eurekateam.samsungextras;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.eurekateam.samsungextras.battery.BatteryActivity;
import com.eurekateam.samsungextras.flashlight.FlashLightActivity;
import com.eurekateam.samsungextras.speaker.ClearSpeakerActivity;
import com.eurekateam.samsungextras.utils.FileUtilsWrapper;
import com.eurekateam.samsungextras.utils.SystemProperties;

public class DeviceSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    public static final String PREF_KEY_FPS_INFO = "fps_info";
    private static final String PREF_CLEAR_SPEAKER = "clear_speaker_settings";
    private static final String PREF_FLASHLIGHT = "flashlight_settings";
    public static final String PREF_GPUEXYNOS = "gpuexynos_settings";
    private static final String PREF_SELINUX = "selinux_settings";
    public static final String PREF_BATTERY = "battery_settings";

    public DeviceSettings() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        System.loadLibrary("libnativebridges");
        setPreferencesFromResource(R.xml.preferences_samsung_parts, rootKey);
        Context mContext = this.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        Preference mClearSpeakerPref = findPreference(PREF_CLEAR_SPEAKER);
        assert mClearSpeakerPref != null;
        mClearSpeakerPref.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), ClearSpeakerActivity.class);
            startActivity(intent);
            return true;
        });

        SwitchPreference mFpsInfo = findPreference(PREF_KEY_FPS_INFO);
        assert mFpsInfo != null;
        mFpsInfo.setChecked(prefs.getBoolean(PREF_KEY_FPS_INFO, false));
        mFpsInfo.setOnPreferenceChangeListener(this);

        SwitchPreference mGPUExynos = findPreference(PREF_GPUEXYNOS);
        assert mGPUExynos != null;
        if (FileUtilsWrapper.isFileReadable(GlobalConstants.TMU_SYSFS)) {
            Log.d(GlobalConstants.TAG, "onCreatePreferences: "
                    + (GlobalConstants.TMU_SYSFS) + " readable, value " +
                    FileUtilsWrapper.readOneLine(GlobalConstants.TMU_SYSFS));
            mGPUExynos.setChecked(
                    FileUtilsWrapper.readOneLine(GlobalConstants.TMU_SYSFS).equals("1"));
        }else {
            Log.w(GlobalConstants.TAG, "onCreatePreferences: "
                    + (GlobalConstants.TMU_SYSFS) + " not readable");
            mGPUExynos.setEnabled(false);
        }
        mGPUExynos.setOnPreferenceChangeListener(this);

        Preference mSELinux = findPreference(PREF_SELINUX);
        assert mSELinux != null;
        mSELinux.setOnPreferenceClickListener(preference -> {
            boolean selinux_enforcing = SystemProperties.getenforce();
            Toast.makeText(getContext(), selinux_enforcing ?
                            "SELinux is in enforcing state." :
                            "SELinux is in permissive state.",
                    Toast.LENGTH_SHORT).show();
            return true;
        });
        Preference mFlashLight = findPreference(PREF_FLASHLIGHT);
        mFlashLight.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), FlashLightActivity.class);
            startActivity(intent);
            return true;
        });
        Preference mFastCharge = findPreference(PREF_BATTERY);
        assert mFastCharge != null;
        mFastCharge.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), BatteryActivity.class);
            startActivity(intent);
            return true;
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final String key = preference.getKey();
        switch (key) {
            case PREF_KEY_FPS_INFO:
                boolean fps_enabled = (Boolean) value;
                Intent fpsinfo = new Intent(this.getContext(), FPSInfoService.class);
                if (fps_enabled) {
                    this.getContext().startService(fpsinfo);
                } else {
                    this.getContext().stopService(fpsinfo);
                }
                break;
            case PREF_GPUEXYNOS:
                boolean gpu_enabled = (Boolean) value;
                Log.d(GlobalConstants.TAG, "onPreferenceChange: Writing " +
                        gpu_enabled + " to " + GlobalConstants.TMU_SYSFS);
                FileUtilsWrapper.writeLine(GlobalConstants.TMU_SYSFS, gpu_enabled ? "1" : "0");
                Toast.makeText(getContext(), gpu_enabled ? "GPU Throttling is now enabled."
                        : "GPU Throttling is now disabled.",
                        Toast.LENGTH_SHORT).show();
            default:
                break;
        }
        return true;
    }
}
