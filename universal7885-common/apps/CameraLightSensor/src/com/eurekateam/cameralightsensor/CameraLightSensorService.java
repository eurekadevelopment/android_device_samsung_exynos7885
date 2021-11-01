package com.eurekateam.cameralightsensor;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;


public class CameraLightSensorService extends Activity {
    private static final String TAG = "CameraLightSensor";
    static final boolean DEBUG = false;
    Intent i;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moveTaskToBack(true);
        mContext = getApplicationContext();
        IntentFilter screenStateFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        BatteryOptimization(mContext);
        i = new Intent(mContext, Camera2Service.class);
        mContext.startForegroundService(i);
        registerReceiver(mScreenStateReceiver, screenStateFilter);
        if (this.checkSelfPermission(Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission Granted");
        } else {
            // You can directly ask for the permission.
            this.requestPermissions(new String[] { Manifest.permission.CAMERA },
                    1);
        }
    }
    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        super.onDestroy();
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
                Settings.System.SCREEN_BRIGHTNESS_MODE ) ==
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC){
            if(DEBUG) Log.d(TAG, "AutoBrightness Mode Enabled. Starting...");
            i = new Intent(mContext, Camera2Service.class);
            mContext.startForegroundService(i);
        }else{
            Log.d(TAG, "AutoBrightness Mode Disabled, Not Starting Service...");
        }
    }
    private void onDisplayOff(){
        if(DEBUG) Log.d(TAG, "Screen is off. Stopping Service...");
        mContext.stopService(i);
    }
    public static void BatteryOptimization(Context context){
            Intent intent = new Intent();
            String packageName = context.getPackageName();
            PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
    }
}
