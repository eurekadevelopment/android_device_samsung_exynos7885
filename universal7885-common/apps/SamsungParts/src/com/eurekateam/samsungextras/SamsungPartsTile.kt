package com.eurekateam.samsungextras

import android.content.ActivityNotFoundException
import android.content.Intent
import android.service.quicksettings.TileService

class SamsungPartsTile : TileService() {
    override fun onClick() {
        try {
            val intent = Intent(this, DeviceSettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivityAndCollapse(intent)
        } catch (ignored: ActivityNotFoundException) {
            // At this point, the app is most likely hidden and set to only open from Settings
            val intent = Intent(this, DeviceSettings::class.java)
            startActivityAndCollapse(intent)
        }
    }
}