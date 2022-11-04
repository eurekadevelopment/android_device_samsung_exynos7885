package com.eurekateam.fmradio

import android.app.Service
import android.app.PendingIntent
import android.app.Notification
import android.app.NotificationManager
import android.app.NotificationChannel
import androidx.core.app.NotificationCompat
import android.content.Context
import android.content.Intent
import android.os.IBinder

import com.eurekateam.fmradio.NativeFMInterface
import com.eurekateam.fmradio.R

import vendor.eureka.hardware.fmradio.GetType

import java.text.DecimalFormat

class FMRadioService : Service() {
     private val mFMInterface = NativeFMInterface()
     private val mCleanFormat = DecimalFormat("0.#")
     private fun pushNotification(mContext: Context): Notification {
        val nm = mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            mContext.packageName,
            "FM Radio",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nm.createNotificationChannel(channel)
        val builder = NotificationCompat.Builder(mContext, mContext.packageName)
        val notificationIntent = Intent(mContext, FMRadioService::class.java)
        val contentIntent = PendingIntent.getActivity(
            mContext,
            50,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val mCurrFreq = mFMInterface.mDefaultCtl.getValue(GetType.GET_TYPE_FM_FREQ)

        builder.setContentIntent(contentIntent)
        builder.setOngoing(true)
        builder.setSmallIcon(R.drawable.ic_radio)
        builder.setContentTitle("FMRadio is running - ${mCleanFormat.format(mCurrFreq.toFloat() / 1000)} Mhz")
        builder.setChannelId(mContext.packageName)
        return builder.build()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "com.eurekateam.fmradio.STOP") {
             stopSelf()
             return super.onStartCommand(intent, flags, startId)
        } else {
             startForeground(50, pushNotification(this))
             return START_STICKY
        }
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
