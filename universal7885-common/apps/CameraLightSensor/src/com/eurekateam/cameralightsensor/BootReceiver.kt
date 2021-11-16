package com.eurekateam.cameralightsensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED, ignoreCase = true)) {
            val i = Intent(context, CameraLightSensorService::class.java)
            context.startForegroundService(i)
        }
    }
}