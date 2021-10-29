package com.eurekateam.samsungextras;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.service.quicksettings.TileService;

public class SamsungPartsTile extends TileService {
    @Override
    public void onClick() {
        try {
            Intent intent = new Intent(this, DeviceSettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityAndCollapse(intent);
        } catch (ActivityNotFoundException ignored) {
            // At this point, the app is most likely hidden and set to only open from Settings
            Intent intent = new Intent(this, DeviceSettings.class);
            startActivityAndCollapse(intent);
        }
    }
}