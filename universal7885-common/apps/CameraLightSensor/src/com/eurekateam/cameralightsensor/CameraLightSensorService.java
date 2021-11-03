package com.eurekateam.cameralightsensor;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;


public class CameraLightSensorService extends Activity {
    private static final String TAG = "CameraLightSensor";
    static final boolean DEBUG = true;
    Intent i;
    IntentFilter screenStateFilter;
    private Context mContext;
    public ContentResolver contentResolver;
    private boolean mRegistered;
    private boolean mServiceStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moveTaskToBack(true);
        mContext = getApplicationContext();
        contentResolver = getContentResolver();
        BatteryOptimization(mContext);
        i = new Intent(mContext, Camera2Service.class);
        if (this.checkSelfPermission(Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission Granted");
        } else {
            // You can directly ask for the permission.
            this.requestPermissions(new String[] { Manifest.permission.CAMERA },
                    1);
        }
        mRegistered = false;
        screenStateFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        try {
            if(Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
                    == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC){
                registerReceiver(mScreenStateReceiver, screenStateFilter);
                mRegistered = true;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        Uri setting = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE);
        contentResolver.registerContentObserver(setting, false, observer);

    }
    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        mRegistered = false;
        contentResolver.unregisterContentObserver(observer);
        super.onDestroy();
    }
    private final BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                onDisplayOn();
                mServiceStarted = true;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                if (mServiceStarted) onDisplayOff();
                mServiceStarted = false;
            }
        }
    };
    // Make a listener
    ContentObserver observer = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.i(TAG, "observer: Brightness Settings Changed");
            try {
                if(Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
                        == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC){
                    registerReceiver(mScreenStateReceiver, screenStateFilter);
                    mRegistered = true;
                    mContext.startForegroundService(i);
                }else{
                    if(mRegistered) unregisterReceiver(mScreenStateReceiver);
                    mRegistered = false;
                    mContext.stopService(i);
                }
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
    };
    private void onDisplayOn() {
        if(DEBUG) Log.d(TAG, "Screen is on. Starting Service...");
        mContext.startForegroundService(i);
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
