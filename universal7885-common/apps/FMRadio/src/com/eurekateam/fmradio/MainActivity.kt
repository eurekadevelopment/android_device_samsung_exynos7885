package com.eurekateam.fmradio

import android.content.*
import android.graphics.drawable.Icon
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.eurekateam.fmradio.enums.HeadsetState
import com.eurekateam.fmradio.enums.PowerState
import com.eurekateam.fmradio.fragments.ChannelListFragment
import com.eurekateam.fmradio.fragments.FavoriteFragment
import com.eurekateam.fmradio.fragments.MainFragment
import com.eurekateam.fmradio.utils.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.material.textview.MaterialTextView
import vendor.eureka.hardware.fmradio.SetType
import vendor.eureka.hardware.fmradio.GetType

class MainActivity : AppCompatActivity() {
    private lateinit var mIntent: Intent

    /**
     * Function to change current [Fragment] to other [Fragment]
     * @param [fragment] [Fragment] to change to
     * @param [tagFragmentName] Tag for the changing [fragment]
     */
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
    private lateinit var mAlertTitle: MaterialTextView
    private lateinit var mAlertDesc: MaterialTextView
    private lateinit var mAlertImage: AppCompatImageView
    private lateinit var mAudioManager: AudioManager
    override fun onCreate(savedInstanceState: Bundle?) {
        mAlertView = (getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.alertdialog, null)
        mAlertTitle = mAlertView.findViewById(R.id.alert_title)
        mAlertImage = mAlertView.findViewById(R.id.alert_image)
        mAlertDesc = mAlertView.findViewById(R.id.alert_desc)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        DynamicColors.applyToActivitiesIfAvailable(application)
        mFMInterface.mDevCtl.open()
        mFMInterface.mDevCtl.setValue(SetType.SET_TYPE_FM_APP_PID, Process.myPid())
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        /**
         * Detects whether wired headphones is connected to this device or no
         * @see AudioManager.getDevices
         */
        val mAudioDeviceInfo = mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

        for (i in mAudioDeviceInfo.indices) {
            if (mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
            ) {
                MainFragment.mHeadSetPlugged = HeadsetState.HEADSET_STATE_CONNECTED
                Log.i("Wired Headphones detected")
            }
        }

        if (MainFragment.mHeadSetPlugged != HeadsetState.HEADSET_STATE_CONNECTED) {
            mAlertTitle.text = getString(R.string.no_headphones_error)
            mAlertDesc.text = getString(R.string.no_headphones_error_desc)
            mAlertImage.setImageIcon(
                Icon.createWithResource(
                    mAlertView.context,
                    R.drawable.ic_headphones
                )
            )
            AlertDialog.Builder(mAlertView.context)
                .setCancelable(false)
                .setView(mAlertView)
                .setNegativeButton(R.string.ok) { v: DialogInterface, _: Int ->
                    mAudioManager.setParameters(PowerState.FM_POWER_OFF.mAudioParam)
                    v.dismiss()
                    Handler(Looper.getMainLooper()).postDelayed({
                        finish()
                    }, 500)
                }
                .show()
		return
        }
        val receiverFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        registerReceiver(mWiredHeadsetReceiver, receiverFilter)
        val mRadioMainFragment = MainFragment()
        val mRadioChannelListFragment = ChannelListFragment()
        val mFavoriteFragment = FavoriteFragment()
        changeFragment(mRadioMainFragment, MainFragment::class.java.name)
        mIntent = Intent(this, FMRadioService::class.java)
        findViewById<BottomNavigationView>(R.id.bottom_nav_bar).setOnItemSelectedListener {
            when (it.itemId) {
                R.id.radio_main -> changeFragment(mRadioMainFragment, MainFragment::class.java.name)
                R.id.channel_list -> changeFragment(mRadioChannelListFragment, ChannelListFragment::class.java.name)
                R.id.fav_list -> changeFragment(mFavoriteFragment, FavoriteFragment::class.java.name)
            }
            true
        }
    }

    /**
     * Register a [BroadcastReceiver] for listening to headset events
     */
    private val mWiredHeadsetReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_HEADSET_PLUG) {
                MainFragment.mHeadSetPlugged = HeadsetState.HEADSET_STATE_DISCONNECTED
                val mAudioDeviceInfo = mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (i in mAudioDeviceInfo.indices) {
                    if (mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                    ) {
                        MainFragment.mHeadSetPlugged = HeadsetState.HEADSET_STATE_CONNECTED
                    }
                }
                if (MainFragment.mHeadSetPlugged != HeadsetState.HEADSET_STATE_CONNECTED) {
                    Log.w("onReceive: Headset Unplugged")
                    mAlertTitle.text = getString(R.string.no_headphones_error)
                    mAlertDesc.text = getString(R.string.no_headphones_error_desc)
                    mAlertImage.setImageIcon(
                        Icon.createWithResource(
                            mAlertView.context,
                            R.drawable.ic_headphones
                        )
                    )
                    val mAlertDialog = AlertDialog.Builder(mAlertView.context)
                    mAlertDialog
                        .setCancelable(false)
                        .setView(mAlertView)
                        .setNegativeButton(R.string.ok) { v: DialogInterface, _: Int ->
                            v.dismiss()
                            mAudioManager.setParameters(PowerState.FM_POWER_OFF.mAudioParam)
                            Handler(Looper.getMainLooper()).postDelayed({
                                finish()
                            }, 500)
                        }
                        .show()
                }
            }
        }
    }

    /**
     * Helper function to get [MainActivity]'s [FragmentManager] to sub [Fragment]
     * @return [FragmentManager] supplied to this activity
     */
    fun getMySupportFragmentManager(): FragmentManager {
        return supportFragmentManager
    }

    override fun onPause() {
        super.onPause()
        Log.i("Application onPause")
	if (mFMInterface.mDefaultCtl.getValue(GetType.GET_TYPE_FM_MUTEX_LOCKED) == 0) {
            mIntent.action = null
            startService(mIntent)
	}
    }

    override fun onRestart() {
        super.onRestart()
        mIntent.action = "com.eurekateam.fmradio.STOP"
        startService(mIntent)
    }
}
