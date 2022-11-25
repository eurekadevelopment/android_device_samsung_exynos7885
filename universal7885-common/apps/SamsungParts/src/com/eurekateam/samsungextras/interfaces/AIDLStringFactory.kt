package com.eurekateam.samsungextras.interfaces

import com.royna.aidlvintf.AIDLInterface.makeAIDLStr

object AIDLStringFactory {
    private fun construct(kInterface: String) = makeAIDLStr("vendor.eureka.hardware.parts", kInterface)
    val IBattery = construct("IBatteryStats")
    val IFlashLight = construct("IFlashBrightness")
    val IDisplay = construct("IDisplayConfigs")
    val ISwap = construct("ISwapOnData")
    val ICharge = construct("ISmartCharge")
}
