package com.eurekateam.fmradio

import android.content.*
import android.graphics.drawable.Icon
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.eurekateam.fmradio.fragments.ChannelListFragment
import com.eurekateam.fmradio.fragments.FavouriteFragment
import com.eurekateam.fmradio.fragments.MainFragment
import com.eurekateam.fmradio.utils.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.material.textview.MaterialTextView


class MainActivity : AppCompatActivity() {
    private lateinit var mIntent : Intent
    private fun changeFragment(fragment: Fragment?, tagFragmentName: String) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container_view, fragment!!, tagFragmentName)
            setPrimaryNavigationFragment(fragment)
            setReorderingAllowed(true)
            commitNow()
        }
    }
    private val mFMInterface = NativeFMInterface()
    private lateinit var mAlertView: View
    private lateinit var mAlertTitle : MaterialTextView
    private lateinit var mAlertDesc : MaterialTextView
    private lateinit var mAlertImage : AppCompatImageView
    private lateinit var mAudioManager : AudioManager
    override fun onCreate(savedInstanceState: Bundle?) {
        System.loadLibrary("fmioctl_jni")
        MainFragment.fd = mFMInterface.openFMDevice()
        mAlertView = (getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.alertdialog, null)
        mAlertTitle = mAlertView.findViewById(R.id.alert_title)
        mAlertImage = mAlertView.findViewById(R.id.alert_image)
        mAlertDesc = mAlertView.findViewById(R.id.alert_desc)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        if (MainFragment.fd == -1){
            Log.e("CANNOT OPEN /dev/radio0!!!")
            mAlertTitle.text = getString(R.string.radio_io_error)
            mAlertDesc.text = getString(R.string.radio_io_error_desc)
            mAlertImage.setImageIcon(Icon.createWithResource(this, R.drawable.ic_error))
            val mAlertDialog = AlertDialog.Builder(this)
            mAlertDialog
                .setCancelable(false)
                .setView(mAlertView)
                .setNegativeButton(R.string.ok) { _: DialogInterface, _: Int ->
                    finish()
                }
                .show()
        }
        DynamicColors.applyToActivitiesIfAvailable(application)
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val mAudioDeviceInfo = mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (i in mAudioDeviceInfo.indices){
            if (mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES) {
                MainFragment.mHeadSetPlugged = true
                Log.i("Wired Headphones detected")
            }
        }
        if (!MainFragment.mHeadSetPlugged){
            mAlertTitle.text = getString(R.string.no_headphones_error)
            mAlertDesc.text = getString(R.string.no_headphones_error_desc)
            mAlertImage.setImageIcon(Icon.createWithResource(mAlertView.context,
                R.drawable.ic_headphones
            ))
            AlertDialog.Builder(mAlertView.context)
                .setCancelable(false)
                .setView(mAlertView)
                .setNegativeButton(R.string.ok) { v: DialogInterface, _: Int ->
                    mAudioManager.setParameters(FM_RADIO_OFF)
                    v.dismiss()
                    Handler(Looper.getMainLooper()).postDelayed({
                        finish()
                    },500)
                }
                .show()
        }
        val receiverFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        registerReceiver(mWiredHeadsetReceiver, receiverFilter)
        val mRadioMainFragment = MainFragment()
        val mRadioChannelListFragment = ChannelListFragment()
        val mFavouriteFragment = FavouriteFragment()
        changeFragment(mRadioMainFragment, MainFragment::class.java.name)
        mIntent = Intent(this, FMRadioService::class.java)
        mIntent.action = BEGIN_BG_SERVICE
        startService(mIntent)
        findViewById<BottomNavigationView>(R.id.bottom_nav_bar).setOnItemSelectedListener {
            when (it.itemId){
                R.id.radio_main -> changeFragment(mRadioMainFragment, MainFragment::class.java.name)
                R.id.channel_list -> changeFragment(mRadioChannelListFragment, ChannelListFragment::class.java.name)
                R.id.fav_list -> changeFragment(mFavouriteFragment, FavouriteFragment::class.java.name)
            }
            true
        }
    }

    private val mWiredHeadsetReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_HEADSET_PLUG) {
                MainFragment.mHeadSetPlugged = false
                val mAudioDeviceInfo = mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (i in mAudioDeviceInfo.indices){
                    if (mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES){
                        MainFragment.mHeadSetPlugged = true
                    }
                }
                if (!MainFragment.mHeadSetPlugged){
                    Log.w("onReceive: Headset Unplugged")
                    mAlertTitle.text = getString(R.string.no_headphones_error)
                    mAlertDesc.text = getString(R.string.no_headphones_error_desc)
                    mAlertImage.setImageIcon(Icon.createWithResource(mAlertView.context,
                        R.drawable.ic_headphones
                    ))
                    val mAlertDialog = AlertDialog.Builder(mAlertView.context)
                    mAlertDialog
                        .setCancelable(false)
                        .setView(mAlertView)
                        .setNegativeButton(R.string.ok) { v: DialogInterface, _: Int ->
                            v.dismiss()
                            mAudioManager.setParameters(FM_RADIO_OFF)
                            Handler(Looper.getMainLooper()).postDelayed({
                                finish()
                            },500)
                        }
                        .show()
                }
            }
        }
    }
    fun getMySupportFragmentManager(): FragmentManager {
        return supportFragmentManager
    }

    override fun onPause() {
        super.onPause()
        mIntent.action = BEGIN_BG_SERVICE
        startService(mIntent)
    }

    override fun onRestart() {
        super.onRestart()
        stopService(mIntent)
    }

    companion object {
        private const val PACKAGENAME = "com.eurekateam.fmradio"
        private const val BEGIN_BG_SERVICE = "$PACKAGENAME.STARTBG"
        private const val FM_RADIO_OFF = "l_fmradio_mode=off"
    }
}
