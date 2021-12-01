/*
 * Copyright (C) 2020 The Xiaomi-SM6250 Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eurekateam.samsungextras.fps

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.*
import android.service.dreams.DreamService
import android.service.dreams.IDreamManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.eurekateam.samsungextras.utils.SurfaceFlingerFPS
import kotlin.math.roundToInt

open class FPSInfoService : Service() {
    private var mView: View? = null
    private var mCurFPSThread: Thread? = null
    private val TAG = "FPSInfoService"
    private var fPSInfoString: String? = null
    private var mDreamManager: IDreamManager? = null

    private inner class FPSView(c: Context) : View(c) {
        private val mOnlinePaint: Paint?
        private val mAscent: Float
        private var mMaxWidth = 0
        private var mNeededWidth = 0
        private var mNeededHeight = 0
        private var mDataAvail = false
        private val mCurFPSHandler: Handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (msg.obj == null || msg.what != 1) {
                    return
                }
                val msgData = msg.obj as String
                fPSInfoString = "$msgData FPS"
                mDataAvail = true
                updateDisplay()
            }
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            mCurFPSHandler.removeMessages(1)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension(
                resolveSize(mNeededWidth, widthMeasureSpec),
                resolveSize(mNeededHeight, heightMeasureSpec)
            )
        }

        public override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (!mDataAvail) {
                return
            }
            val LEFT = width - 1
            val y = mPaddingTop - mAscent.toInt()
            val s = fPSInfoString
            canvas.drawText(
                s, (LEFT - mPaddingLeft - mMaxWidth).toFloat(), (
                        y - 1).toFloat(), mOnlinePaint
            )
        }

        fun updateDisplay() {
            if (!mDataAvail) {
                return
            }
            if (mOnlinePaint != null) {
                mMaxWidth = mOnlinePaint.measureText(fPSInfoString).toInt()
            }
            val neededWidth = mPaddingLeft + mPaddingRight + mMaxWidth
            val neededHeight = mPaddingTop + mPaddingBottom + 40
            if (neededWidth != mNeededWidth || neededHeight != mNeededHeight) {
                mNeededWidth = neededWidth
                mNeededHeight = neededHeight
                requestLayout()
            } else {
                invalidate()
            }
        }

        override fun getHandler(): Handler {
            return mCurFPSHandler
        }

        init {
            val density = c.resources.displayMetrics.density
            val paddingPx = (5 * density).roundToInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
            setBackgroundColor(Color.argb(0x60, 0, 0, 0))
            val textSize = (12 * density).roundToInt()
            val typeface = Typeface.create("monospace", Typeface.NORMAL)
            mOnlinePaint = Paint()
            mOnlinePaint.typeface = typeface
            mOnlinePaint.isAntiAlias = true
            mOnlinePaint.textSize = textSize.toFloat()
            mOnlinePaint.color = Color.WHITE
            mOnlinePaint.setShadowLayer(5.0f, 0.0f, 0.0f, Color.BLACK)
            mAscent = mOnlinePaint.ascent()
            mOnlinePaint.descent()
            updateDisplay()
        }
    }

    /**
     * Thead to monitor fps
     */
    protected class CurFPSThread(private val mHandler: Handler) : Thread() {
        private var mInterrupt = false
        override fun interrupt() {
            mInterrupt = true
        }

        override fun run() {
            try {
                while (!mInterrupt) {
                    sleep(500)
                    StringBuilder()
                    val fpsVal = surfaceFlingerFPS?.fps?.roundToInt().toString()
                    mHandler.sendMessage(mHandler.obtainMessage(1, fpsVal))
                }
            } catch (ignored: InterruptedException) {
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        mView = FPSView(this)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_SECURE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        params.y = 50
        params.gravity = Gravity.START or Gravity.TOP
        params.title = "FPS Info"
        surfaceFlingerFPS = SurfaceFlingerFPS.instance
        startThread()
        mDreamManager = IDreamManager.Stub.asInterface(
            ServiceManager.checkService(DreamService.DREAM_SERVICE)
        )
        val screenStateFilter = IntentFilter(Intent.ACTION_SCREEN_ON)
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(mScreenStateReceiver, screenStateFilter)
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        wm.addView(mView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopThread()
        (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(mView)
        mView = null
        unregisterReceiver(mScreenStateReceiver)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private val mScreenStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_ON) {
                Log.d(TAG, "ACTION_SCREEN_ON $isDozeMode")
                if (!isDozeMode) {
                    startThread()
                    mView!!.visibility = View.VISIBLE
                }
            } else if (intent.action == Intent.ACTION_SCREEN_OFF) {
                Log.d(TAG, "ACTION_SCREEN_OFF")
                mView!!.visibility = View.GONE
                stopThread()
            }
        }
    }

    /**
     * Find is the device is in doze mode
     * @return true if device is in doze mode, else false
     */
    private val isDozeMode: Boolean
        get() {
            try {
                if (mDreamManager != null && mDreamManager!!.isDreaming) {
                    return true
                }
            } catch (e: RemoteException) {
                return false
            }
            return false
        }

    private fun startThread() {
        Log.d(TAG, "started CurFPSThread")
        mRunning = true
        mCurFPSThread = CurFPSThread(mView!!.handler)
        surfaceFlingerFPS!!.start()
        mCurFPSThread?.start()
    }

    private fun stopThread() {
        if (mCurFPSThread != null && mCurFPSThread!!.isAlive) {
            Log.d(TAG, "stopping CurFPSThread")
            surfaceFlingerFPS!!.stop()
            mCurFPSThread!!.interrupt()
            try {
                mCurFPSThread!!.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        mRunning = false
        mCurFPSThread = null
    }

    fun getRunning() : Boolean{
        return mRunning
    }
    companion object {
        private var surfaceFlingerFPS: SurfaceFlingerFPS? = null
        private var mRunning : Boolean = false
    }
}