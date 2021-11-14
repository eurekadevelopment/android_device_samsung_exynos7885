package com.eurekateam.cameralightsensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.eurekateam.cameralightsensor.CameraLightSensorService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            if (context != null) {
                final Intent i = new Intent(context, CameraLightSensorService.class);
                context.startForegroundService(i);
            }
        }
    }
}
