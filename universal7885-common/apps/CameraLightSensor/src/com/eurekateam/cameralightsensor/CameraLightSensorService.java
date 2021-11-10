package com.eurekateam.cameralightsensor;

import android.annotation.NonNull;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.nio.ByteBuffer;
import java.util.Collections;

public class CameraLightSensorService extends Service {
    protected static final String TAG = CameraLightSensorService.class.getSimpleName();
    static final boolean DEBUG = true;
    IntentFilter screenStateFilter;
    private Context mContext;
    public ContentResolver contentResolver;
    private boolean mRegistered;
    protected static final int CAMERA_CHOICE = CameraCharacteristics.LENS_FACING_FRONT;
    boolean destroy;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession session;
    volatile boolean avail = true;
    protected ImageReader imageReader;
    private CameraManager manager;
    private final static int DELAY = 5 * 1000; // 5 Seconds
    private boolean mServiceStarted;
    volatile private boolean mLock;
    private boolean mThreadRunning;

    public Notification PushNotification()
    {
        NotificationManager nm = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                mContext.getBasePackageName(), "CameraLightSensor",
                NotificationManager.IMPORTANCE_LOW
        );
        nm.createNotificationChannel(channel);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext, mContext.getBasePackageName());
        Intent notificationIntent = new Intent(mContext, CameraLightSensorService.class);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext,50,
                notificationIntent,PendingIntent.FLAG_IMMUTABLE);

        //set
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.ic_brightness);
        builder.setContentTitle("Camera Light Sensor Service");
        builder.setChannelId(mContext.getBasePackageName());

        return builder.build();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        if(mRegistered) contentResolver.unregisterContentObserver(mSettingsObserver);
        mRegistered = false;
        cameraDevice.close();
        if (session != null && avail) {
            try {
                session.abortCaptures();
                session.close();
            } catch (CameraAccessException e) {
                Log.e(TAG, e.getMessage());
            } catch (IllegalStateException e2){
                Log.e(TAG, "Session Already Closed");
            }

        }
        manager.unregisterAvailabilityCallback(availabilityCallback);
        avail = false;
        destroy = true;
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
    // Make a listener for settings
    ContentObserver mSettingsObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.i(TAG, "observer: Brightness Settings Changed");
            try {
                if(Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
                        == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC){
                    registerReceiver(mScreenStateReceiver, screenStateFilter);
                    mRegistered = true;
                    onDisplayOn();
                }else{
                    if(mRegistered) unregisterReceiver(mScreenStateReceiver);
                    mRegistered = false;
                    onDisplayOff();
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
        avail = true;
        readyCamera();
    }
    private void onDisplayOff(){
        if(DEBUG) Log.d(TAG, "Screen is off. Stopping Service...");
        thread.interrupt();
        avail = false;
        mThreadRunning = false;
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
    protected CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "CameraDevice.StateCallback onOpened");
            cameraDevice = camera;
            actOnReadyCameraDevice();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.w(TAG, "CameraDevice.StateCallback onDisconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "CameraDevice.StateCallback onError " + error);
        }
    };

    protected CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onReady(CameraCaptureSession session) {
            if (!destroy) {
                CameraLightSensorService.this.session = session;
                try {
                    if (createCaptureRequest() == null) return;
                    try {
                        session.capture(createCaptureRequest(), captureCallback, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        Log.w(TAG, "onReady: Session is NULL");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Camera is in use");
                    e.printStackTrace();
                }
            }
        }


        @Override
        public void onConfigured(CameraCaptureSession session) {

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }
    };


    protected ImageReader.OnImageAvailableListener onImageAvailableListener = reader -> {
        if (DEBUG) Log.d(TAG, "onImageAvailable: Capturing");
        Image img = reader.acquireLatestImage();
        if (img != null) {
            try {
                processImage(img);
            } catch (Settings.SettingNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
            img.close();
            if (DEBUG)
                Log.d(TAG, "ImageReader.OnImageAvailableListener: Closing Camera and Sessions..");
            cameraDevice.close();
            session.close();
        }
    };

    @SuppressLint("MissingPermission")
    public void readyCamera() {
        manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String pickedCamera = getCamera(manager);
            manager.registerAvailabilityCallback(availabilityCallback, null);
            manager.openCamera(pickedCamera, cameraStateCallback, null);
            imageReader = ImageReader.newInstance(250, 250, ImageFormat.JPEG, 2 /* images buffered */);
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
            if(!mThreadRunning){
                Thread mMyThread = new Thread(thread);
                mMyThread.start();
                mThreadRunning = true;
            }
            if (DEBUG) Log.d(TAG, "imageReader created");
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public String getCamera(CameraManager manager) {
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CAMERA_CHOICE) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = getApplicationContext();
        contentResolver = getContentResolver();
        startForeground(50, PushNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA);
        BatteryOptimization(mContext);
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
        contentResolver.registerContentObserver(setting, false, mSettingsObserver);
        if(DEBUG) Log.d(TAG, "onStartCommand flags " + flags + " startId " + startId);
        destroy = false;
        avail = true;
        startForeground(50, PushNotification());
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        if(DEBUG) Log.d(TAG, "onCreate service");
        mContext = getApplicationContext();
        startForeground(50, PushNotification());
        super.onCreate();
    }

    public void actOnReadyCameraDevice() {
        try {
            cameraDevice.createCaptureSession(Collections.singletonList(imageReader.getSurface()), sessionStateCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            mLock = false;
        }
    }
    private void processImage(Image image) throws Settings.SettingNotFoundException, InterruptedException {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        int brightness = calculateBrightnessEstimate(bitmapImage,5);
        AdjustBrightness(brightness);
    }

    protected CaptureRequest createCaptureRequest() {
        try {
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            builder.addTarget(imageReader.getSurface());
            return builder.build();
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            mLock = false;
            return null;
        }
    }
    CameraManager.AvailabilityCallback availabilityCallback = new CameraManager.AvailabilityCallback() {
        @Override
        public void onCameraOpened(String cameraId, String packageId) {
            super.onCameraOpened(cameraId, packageId);
            Log.i(TAG, "CameraManager.AvailabilityCallback: Camera " + cameraId
                    + " Opened by Package " + packageId);
            mLock = true;
            if (packageId.equals(mContext.getBasePackageName())) return;
            avail = false;
        }

        @Override
        public void onCameraUnavailable(String cameraId) {
            if (DEBUG) Log.i(TAG, "CameraManager.AvailabilityCallback : Camera NOT Available. ");
            avail = false;
            super.onCameraUnavailable(cameraId);
        }

        @Override
        public void onCameraAvailable(String cameraId) {
            if (DEBUG) Log.i(TAG, "CameraManager.AvailabilityCallback : Camera IS Available. ");
            avail = true;
            mLock = false;
            super.onCameraAvailable(cameraId);
        }

        @Override
        public void onCameraClosed(String cameraId) {
            super.onCameraClosed(cameraId);
        }

    };
    CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber) {
            if (DEBUG) Log.d(TAG, "captureCallback: Closing Session");
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            cameraDevice.close();
            session.close();
        }

    };

    public int calculateBrightnessEstimate(Bitmap bitmap, int pixelSpacing) {
        int R = 0; int G = 0; int B = 0;
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int n = 0;
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i += pixelSpacing) {
            int color = pixels[i];
            R += Color.red(color);
            G += Color.green(color);
            B += Color.blue(color);
            n++;
        }
        return (R + B + G) / (n * 3);
    }
    private void AdjustBrightness(int brightness) throws Settings.SettingNotFoundException {
        if (DEBUG) Log.i(TAG, "AdjustBrightness: Received Brightness Value " + brightness);
        int oldbrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        if (DEBUG) Log.i(TAG, "AdjustBrightness: OldVal = " + oldbrightness + " NewVal = " +
                brightness + " Adjusting..");
        int newbrightness = brightness;
        if (newbrightness > 255){
            newbrightness = 255;
        }else if (newbrightness < 0){
            newbrightness = 0;
        }
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, newbrightness);
    }
    Thread thread = new Thread(() -> {
        while (!Thread.currentThread().isInterrupted()) {
            if (avail && !mLock) {
                SystemClock.sleep(DELAY);
                ContextCompat.getMainExecutor(mContext).execute(() -> {
                    if (avail) readyCamera();
                });
            }
        }

    });
}

