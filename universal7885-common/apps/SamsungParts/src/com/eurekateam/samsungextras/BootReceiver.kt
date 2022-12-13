package com.eurekateam.samsungextras

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import com.eurekateam.samsungextras.battery.BatteryFragment
import com.eurekateam.samsungextras.dolby.DolbyCore
import com.eurekateam.samsungextras.dolby.DolbyFragment
import com.eurekateam.samsungextras.flashlight.FlashLightFragment
import com.eurekateam.samsungextras.smartcharge.SmartChargeFragment
import com.eurekateam.samsungextras.interfaces.Battery
import com.eurekateam.samsungextras.interfaces.Display
import com.eurekateam.samsungextras.interfaces.FlashLight
import com.eurekateam.samsungextras.interfaces.Swap
import com.eurekateam.samsungextras.interfaces.SmartCharge
import com.eurekateam.samsungextras.swap.SwapFragment

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val mSharedPreferences = p0?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        if (p1 != null && mSharedPreferences != null) {
            if (p1.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
                System.loadLibrary("StorageHelper")
                System.loadLibrary("SwapCallback")
                // Battery
                val mBattery = Battery()
                mBattery.Charge = mSharedPreferences.getBoolean(BatteryFragment.PREF_CHARGE, true)
                mBattery.FastCharge = mSharedPreferences.getBoolean(BatteryFragment.PREF_FASTCHARGE, true)
                // Dolby
                DolbyCore.setEnabled(
                    mSharedPreferences
                        .getBoolean(DolbyFragment.PREF_DOLBY_ENABLE, false)
                )
                DolbyCore.setProfile(
                    mSharedPreferences
                        .getInt(DolbyFragment.PREF_DOLBY_PROFILE, 0)
                )

                // FlashLight
                val mFlash = FlashLight()
                mFlash.setFlash(mSharedPreferences.getInt(FlashLightFragment.PREF_FLASHLIGHT, 5))

                // ZRAM
                val mSwap = Swap()
                if (mSharedPreferences.getBoolean(SwapFragment.PREF_SWAP_ENABLE, false)) {
                    mSwap.setSwapOn(false)
                }

                // Display
                val mDisplay = Display()
                mDisplay.DT2W = mSharedPreferences.getBoolean(DeviceSettings.PREF_DOUBLE_TAP, true)
                mDisplay.GloveMode = mSharedPreferences.getBoolean(DeviceSettings.PREF_GLOVE_MODE, false)

                val limit = mSharedPreferences.getInt(SmartChargeFragment.PREF_LIMIT, 20)
                val restart = mSharedPreferences.getInt(SmartChargeFragment.PREF_RESTART, 80)
                val mSmartCharge = SmartCharge()
                mSmartCharge.setConfig(limit, restart)

                Log.i("SamsungParts", "Applied settings")
            }
        }
    }
}
