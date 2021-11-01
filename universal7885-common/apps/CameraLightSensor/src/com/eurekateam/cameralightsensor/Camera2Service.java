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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Collections;

public class Camera2Service extends Service {
    protected static final String TAG = Camera2Service.class.getSimpleName();
    protected static final int CAMERACHOICE = CameraCharacteristics.LENS_FACING_FRONT;
    boolean destroy;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession session;
    boolean isavail = true;
    protected ImageReader imageReader;
    private Context mContext;
    private CameraManager manager;

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
                Camera2Service.this.session = session;
                try {
                    try {
                        if (createCaptureRequest() == null) return;
                        session.capture(createCaptureRequest(), captureCallback, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e){
                        Log.w(TAG, "onReady: Session is NULL");
                    }
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

    public void readyCamera() {
        manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String pickedCamera = getCamera(manager);
            if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.registerAvailabilityCallback(availabilityCallback, null);
            manager.openCamera(pickedCamera, cameraStateCallback, null);
            imageReader = ImageReader.newInstance(250, 250, ImageFormat.JPEG, 2 /* images buffered */);
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
            if (DEBUG) Log.d(TAG, "imageReader created");
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            isavail = false;
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
        destroy = false;
        isavail = true;
        startForeground(50, PushNotification());
        readyCamera();
        return START_NOT_STICKY;
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
        startForeground(50, PushNotification());
        readyCamera();
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
        cameraDevice.close();
        if (session != null && isavail) {
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
        isavail = false;
        destroy = true;
        stopForeground(true);
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
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(imageReader.getSurface());
            return builder.build();
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }
    CameraManager.AvailabilityCallback availabilityCallback = new CameraManager.AvailabilityCallback() {
        @Override
        public void onCameraOpened(String cameraId, String packageId) {
            super.onCameraOpened(cameraId, packageId);
            Log.i(TAG, "CameraManager.AvailabilityCallback: Camera " + cameraId
                    + " Opened by Package " + packageId);
            if (packageId.equals(mContext.getBasePackageName())) return;
            isavail = false;
        }
        @Override
        public void onCameraClosed(String cameraId) {
            Log.d(TAG, "CameraManager.AvailabilityCallback: Camera is closed now, " +
                    "sleeping for 4000 ms");
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(() -> {
                if(isavail) readyCamera();
            },4000);
            super.onCameraClosed(cameraId);
        }

        @Override
        public void onCameraUnavailable(String cameraId) {
            Log.i(TAG, "CameraManager.AvailabilityCallback : Camera NOT Available. ");
            super.onCameraUnavailable(cameraId);
        }

        @Override
        public void onCameraAvailable(String cameraId) {
            Log.i(TAG, "CameraManager.AvailabilityCallback : Camera IS Available. ");
            isavail = true;
            super.onCameraAvailable(cameraId);
        }
    };
    CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber) {
            if (DEBUG) Log.d(TAG, "captureCallback: Closing Session");
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(() -> readyCamera(),400);
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
        }

    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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
        if (DEBUG) Log.i(TAG, "AdjustBrightness: Oldval = " + oldbrightness + " Newval = " +
                brightness + " Adjusting..");
	int newbrightness = brightness;
	if (newbrightness > 255){
		newbrightness = 255;
	}else if (newbrightness < 0){
		newbrightness = 0;
	}
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, newbrightness);
    }
}
