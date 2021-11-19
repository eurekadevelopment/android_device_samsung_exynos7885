package com.eurekateam.samsungextras.utils

import android.os.Parcel
import android.os.ServiceManager

/*
 *  Copyright (C) 2020-2021 Samsung Electronics Co. Ltd
 *  This Class is based on Samsung's OneUI 3.1
 *  /system/frameworks/gamesdk.jar
 */
class SurfaceFlingerFPS private constructor() {
    private var mFps: Double
    private val mLock: Any = Any()
    private var mPrevFPSCheckStartTime: Long
    private var mPrevFlipCount: Int
    private var mPrevFps: Double
    private var running = false
    private var mPrevPrevCheckStartTime: Long
    private var mPrevPrevFlipCount: Int

    @Volatile
    private var mSkipUpdate = false

    @Volatile
    private var mStopThread = false
    private var mThread: ThreadGetFlip? = null

    /**
     * Starts the thread
     */
    fun start() {
        synchronized(this) {
            if (mThread == null || mStopThread) {
                mStopThread = false
                val threadGetFlip = ThreadGetFlip()
                mThread = threadGetFlip
                threadGetFlip.start()
                running = true
            }
        }
    }

    /**
     * Stops the thread
     */
    fun stop() {
        synchronized(this) {
            mStopThread = true
            if (mThread != null) {
                mThread!!.interrupt()
            }
            mThread = null
            running = false
        }
    }

    /**
     * Check if the fps monitor is able to start
     * @return true if able to start, else false
     */
    private val isAvailable: Boolean
        get() = mStopThread || mThread == null

    private fun updateCurrentFps() {
        try {
            synchronized(mLock) {
                mSkipUpdate = true
                val curTime = System.currentTimeMillis()
                val curFlipCount = flipCount
                mFps = if (curTime - mPrevFPSCheckStartTime < 200) {
                    (curFlipCount - mPrevPrevFlipCount).toDouble() / ((curTime - mPrevPrevCheckStartTime).toDouble() / 1000.0)
                } else {
                    (curFlipCount - mPrevFlipCount).toDouble() / ((curTime - mPrevFPSCheckStartTime).toDouble() / 1000.0)
                }
                if (mFps < 1.0) {
                    mFps = mPrevFps
                }
                mPrevFlipCount = curFlipCount
                mPrevFPSCheckStartTime = curTime
                mPrevFps = mFps
                mSkipUpdate = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Gets the fps value from internal thread
     * @return fps value in Double
     */
    val fps: Double
        get() {
            if (isAvailable) {
                return -999.0
            }
            updateCurrentFps()
            return mFps
        }

    /**
     * Gets the flip count from SurfaceFlinger Service
     * @return flip count
     */
    private val flipCount: Int
        get() {
            var flipCount = -1
            try {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    val surfaceFlinger = ServiceManager.getService("SurfaceFlinger")
                    data.writeInterfaceToken("android.ui.ISurfaceComposer")
                    if (surfaceFlinger != null && surfaceFlinger.transact(1013, data, reply, 0)) {
                        flipCount = reply.readInt()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                data.recycle()
                reply.recycle()
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
            return flipCount
        }

    /* access modifiers changed from: private */
    fun updateFlipCountRecord() {
        try {
            synchronized(mLock) {
                mPrevPrevCheckStartTime = mPrevFPSCheckStartTime
                mPrevPrevFlipCount = mPrevFlipCount
                mPrevFPSCheckStartTime = System.currentTimeMillis()
                mPrevFlipCount = flipCount
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /* access modifiers changed from: private */ /* loaded from: classes.dex */
    inner class ThreadGetFlip : Thread() {
        // java.lang.Thread, java.lang.Runnable
        override fun run() {
            while (!mStopThread) {
                if (!mSkipUpdate) {
                    updateFlipCountRecord()
                    try {
                        sleep(300)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Gets an instance of this class
         * @return new instance
         */
        val instance: SurfaceFlingerFPS
            get() = SurfaceFlingerFPS()
    }

    init {
        mFps = 0.0
        mPrevFps = 0.0
        mPrevFlipCount = 0
        mPrevFPSCheckStartTime = 0
        mPrevPrevFlipCount = 0
        mPrevPrevCheckStartTime = 0
    }
}