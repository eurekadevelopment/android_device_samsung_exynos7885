package com.eurekateam.cameralightsensor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class CameraLightSensorService extends Service {
    private static final String TAG = "CameraLightSensor";
    private static final boolean DEBUG = false;
    final Intent i = new Intent(getApplicationContext(), CameraListener.class);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter screenStateFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStateReceiver, screenStateFilter);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        super.onDestroy();
        this.unregisterReceiver(mScreenStateReceiver);
    }
    private final BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                try {
                    onDisplayOn();
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                onDisplayOff();
            }
        }
    };
    private void onDisplayOn() throws Settings.SettingNotFoundException {
        if(Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE ) == 1){
            Log.d(TAG, "AutoBrightness Mode Enabled. Starting...");
            getApplicationContext().startService(i);
        }else{
            Log.d(TAG, "AutoBrightness Mode Disabled, Not Starting Service...");
        }
    }
    private void onDisplayOff(){
        Log.d(TAG, "Screen is off. Stopping Service...");
        getApplicationContext().stopService(i);
    }
}
