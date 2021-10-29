package com.eurekateam.cameralightsensor;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CameraListener extends Service {

    private final String TAG = CameraListener.class.getSimpleName();
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Handler mHandler = new Handler(Looper.getMainLooper());
        AtomicInteger prev = new AtomicInteger(Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 50));
        mHandler.postDelayed(() -> {
            int current = calculateBrightnessEstimate(getPreviewBitmap());
            Log.i(TAG, ": Current Brigtness: " + current);
            if(prev.get() > current) {
                while (prev.get() - current > 0) {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, current);
                    current += 2;
                }
            }else{
                while (prev.get() - current < 0) {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, current);
                    current -= 2;
                }
            }
            prev.set(current);
        },1000);
    }

    private int findFrontFacingCamera() {
        return 2;
    }

    private Bitmap getPreviewBitmap() {
        AtomicReference<Bitmap> bitmap = null;
        Camera mCamera = Camera.open(findFrontFacingCamera());
        mCamera.setPreviewCallback((data, camera) -> {
                    if (camera != null) {
                        Camera.Parameters parameters = camera.getParameters();
                        int imageFormat = parameters.getPreviewFormat();
                        if (imageFormat == ImageFormat.NV21) {
                            int w = parameters.getPreviewSize().width;
                            int h = parameters.getPreviewSize().height;

                            YuvImage yuvImage = new YuvImage(data, imageFormat, w, h, null);
                            Rect rect = new Rect(0, 0, w, h);
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            yuvImage.compressToJpeg(rect, 100, outputStream);

                            bitmap.set(BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size()));
                        } else if (imageFormat == ImageFormat.JPEG || imageFormat == ImageFormat.RGB_565) {
                            bitmap.set(BitmapFactory.decodeByteArray(data, 0, data.length));
                        }
                    }
                }
        );
        mCamera.stopPreview();
        mCamera.release();
        return bitmap.get();
    }
    private int calculateBrightnessEstimate(Bitmap bitmap) {
        int R = 0; int G = 0; int B = 0;
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int n = 0;
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i += 5) {
            int color = pixels[i];
            R += Color.red(color);
            G += Color.green(color);
            B += Color.blue(color);
            n++;
        }
        return (R + B + G) / (n * 3);
    }
}
