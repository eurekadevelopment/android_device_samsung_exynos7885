package com.eurekateam.samsungextras;


import android.os.IBinder;
import android.os.Parcel;
import android.os.ServiceManager;

/*
 *  Copyright (C) 2020-2021 Samsung Electronics Co. Ltd
 *  This Class is based on Samsung's OneUI 3.1
 *  /system/frameworks/gamesdk.jar
 */
public class SurfaceFlingerFPS {
    private double mFps;
    private final Object mLock;
    private long mPrevFPSCheckStartTime;
    private int mPrevFlipCount;
    private double mPrevFps;
    public boolean running;
    private long mPrevPrevCheckStartTime;
    private int mPrevPrevFlipCount;
    private volatile boolean mSkipUpdate;
    private volatile boolean mStopThread;
    private ThreadGetFlip mThread;

    private SurfaceFlingerFPS() {
        this.mThread = null;
        this.mStopThread = false;
        this.mSkipUpdate = false;
        this.mLock = new Object();
        this.mFps = 0.0d;
        this.mPrevFps = 0.0d;
        this.mPrevFlipCount = 0;
        this.mPrevFPSCheckStartTime = 0;
        this.mPrevPrevFlipCount = 0;
        this.mPrevPrevCheckStartTime = 0;
    }

    /**
     * Starts the thread
     */
    public void start() {
        synchronized (this) {
            if (this.mThread == null || this.mStopThread) {
                this.mStopThread = false;
                ThreadGetFlip threadGetFlip = new ThreadGetFlip();
                this.mThread = threadGetFlip;
                threadGetFlip.start();
                running = true;
            }
        }
    }

    /**
     * Stops the thread
     */
    public void stop() {
        synchronized (this) {
            this.mStopThread = true;
            if (this.mThread != null) {
                this.mThread.interrupt();
            }
            this.mThread = null;
            running = false;
        }
    }

    /**
     * Check if the fps monitor is able to start
     * @return true if able to start, else false
     */
    private boolean isAvailable() {
        return this.mStopThread || this.mThread == null;
    }

    private void updateCurrentFps() {
        try {
            synchronized (this.mLock) {
                this.mSkipUpdate = true;
                long curTime = System.currentTimeMillis();
                int curFlipCount = getFlipCount();
                if (curTime - this.mPrevFPSCheckStartTime < 200) {
                    this.mFps = ((double) (curFlipCount - this.mPrevPrevFlipCount)) / (((double) (curTime - this.mPrevPrevCheckStartTime)) / 1000.0d);
                } else {
                    this.mFps = ((double) (curFlipCount - this.mPrevFlipCount)) / (((double) (curTime - this.mPrevFPSCheckStartTime)) / 1000.0d);
                }
                if (this.mFps < 1.0d) {
                    this.mFps = this.mPrevFps;
                }
                this.mPrevFlipCount = curFlipCount;
                this.mPrevFPSCheckStartTime = curTime;
                this.mPrevFps = this.mFps;
                this.mSkipUpdate = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the fps value from internal thread
     * @return fps value in Double
     */
    public double getFps() {
        if (isAvailable()) {
            return -999.0d;
        }
        updateCurrentFps();
        return this.mFps;
    }

    /**
     * Gets the flip count from SurfaceFlinger Service
     * @return flip count
     */
    private int getFlipCount() {
        int flipCount = -1;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                IBinder surfaceFlinger = ServiceManager.getService("SurfaceFlinger");
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                if (surfaceFlinger != null && surfaceFlinger.transact(1013, data, reply, 0)) {
                    flipCount = reply.readInt();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            data.recycle();
            reply.recycle();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return flipCount;
    }

    /* access modifiers changed from: private */

    public void updateFlipCountRecord() {
        try {
            synchronized (this.mLock) {
                this.mPrevPrevCheckStartTime = this.mPrevFPSCheckStartTime;
                this.mPrevPrevFlipCount = this.mPrevFlipCount;
                this.mPrevFPSCheckStartTime = System.currentTimeMillis();
                this.mPrevFlipCount = getFlipCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ThreadGetFlip extends Thread {
        private ThreadGetFlip() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (!SurfaceFlingerFPS.this.mStopThread) {
                if (!SurfaceFlingerFPS.this.mSkipUpdate) {
                    SurfaceFlingerFPS.this.updateFlipCountRecord();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Gets an instance of this class
     * @return new instance
     */
    public static SurfaceFlingerFPS getInstance(){
        return new SurfaceFlingerFPS();
    }

}
