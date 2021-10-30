package com.eurekateam.cameralightsensor;

import static com.eurekateam.cameralightsensor.CameraLightSensorService.DEBUG;

import android.Manifest;
import android.annotation.NonNull;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Collections;

public class Camera2Service extends Service {
    protected static final String TAG = Camera2Service.class.getSimpleName();
    protected static final int CAMERACHOICE = CameraCharacteristics.LENS_FACING_FRONT;
    protected static long cameraCaptureStartTime;
    boolean destroy;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession session;
    boolean isavail = true;
    protected ImageReader imageReader;
    private Context mContext;

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
            if (!destroy && isavail) {
                Camera2Service.this.session = session;
                isavail = false;
                try {
                    session.capture(createCaptureRequest(), null, null);
                } catch (Exception e){
                    Log.e(TAG,  "Camera is in use");
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

    protected ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
                if (DEBUG) Log.d(TAG, "onImageAvailable: Capturing");
                Image img = reader.acquireLatestImage();
                cameraCaptureStartTime = System.currentTimeMillis();
                if (img != null) {
                    try {
                        processImage(img);
                    } catch (Settings.SettingNotFoundException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    img.close();
                }
            try {
                session.stopRepeating();
                session.close();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }
    };

    public void readyCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String pickedCamera = getCamera(manager);
            if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(pickedCamera, cameraStateCallback, null);
            imageReader = ImageReader.newInstance(720, 720, ImageFormat.JPEG, 2 /* images buffered */);
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
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
                if (cOrientation == CAMERACHOICE) {
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
        if(DEBUG) Log.d(TAG, "onStartCommand flags " + flags + " startId " + startId);
        readyCamera();
        destroy = false;
        isavail = true;
        cameraCaptureStartTime = System.currentTimeMillis();
        startForeground(50, PushNotification());
        return START_STICKY;
    }

    public Notification PushNotification()
    {
        NotificationManager nm = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                mContext.getPackageName(), "CameraLightSensor",
                NotificationManager.IMPORTANCE_LOW
        );
        nm.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(mContext);
        Intent notificationIntent = new Intent(mContext, Camera2Service.class);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext,50,notificationIntent,PendingIntent.FLAG_IMMUTABLE);

        //set
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.ic_brightness);
        builder.setContentText("Camera Light Sensor is running");
        builder.setContentTitle("Camera Light Sensor");
        builder.setChannelId(mContext.getPackageName());
        builder.setAutoCancel(true);

        return builder.build();
    }
    @Override
    public void onCreate() {
        if(DEBUG) Log.d(TAG, "onCreate service");
        mContext = getApplicationContext();
        startForeground(0, PushNotification());
        super.onCreate();
    }

    public void actOnReadyCameraDevice() {
        try {
            cameraDevice.createCaptureSession(Collections.singletonList(imageReader.getSurface()), sessionStateCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        if (session != null && isavail) {
            try {
                session.abortCaptures();
                session.close();
            } catch (CameraAccessException e) {
                Log.e(TAG, e.getMessage());
            }

        }
        destroy = true;
        Log.i(TAG, "onDestory: Destory");
        stopForeground(true);
    }

    private void processImage(Image image) throws Settings.SettingNotFoundException, InterruptedException {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        int brightness = calculateBrightnessEstimate(bitmapImage,10);
        Log.i(TAG, "brightness: " + brightness);
        AdjustBrightness(brightness);
    }

    protected CaptureRequest createCaptureRequest() {
        try {
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            builder.addTarget(imageReader.getSurface());
            return builder.build();
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public int calculateBrightnessEstimate(android.graphics.Bitmap bitmap, int pixelSpacing) {
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
    private void AdjustBrightness(int brightness) throws Settings.SettingNotFoundException, InterruptedException {
        if(DEBUG) Log.i(TAG, "AdjustBrightness: Received Brightness Value " + brightness);
        int oldbrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        if (DEBUG) Log.i(TAG, "AdjustBrightness: Oldval = " + oldbrightness + " Newval = " +
                brightness + " Adjusting..");
        if(oldbrightness > brightness){
            while (oldbrightness > brightness){
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
                brightness -= 5;
                Thread.sleep(500);
            }
        }else{
            while (oldbrightness < brightness){
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
                brightness += 5;
                Thread.sleep(500);
            }
        }
    }
}
