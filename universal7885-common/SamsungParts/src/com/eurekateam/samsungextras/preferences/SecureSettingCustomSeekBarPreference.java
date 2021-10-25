package com.eurekateam.samsungextras.preferences;

import android.content.Context;
import android.util.AttributeSet;

import com.eurekateam.samsungextras.preferences.SecureSettingsStore;

public class SecureSettingCustomSeekBarPreference extends com.eurekateam.samsungextras.preferences.CustomSeekBarPreference {

    public SecureSettingCustomSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setPreferenceDataStore(new SecureSettingsStore(context.getContentResolver()));
    }

    public SecureSettingCustomSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setPreferenceDataStore(new SecureSettingsStore(context.getContentResolver()));
    }

    public SecureSettingCustomSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPreferenceDataStore(new SecureSettingsStore(context.getContentResolver()));
    }

    public SecureSettingCustomSeekBarPreference(Context context) {
        super(context);
        setPreferenceDataStore(new SecureSettingsStore(context.getContentResolver()));
    }
}