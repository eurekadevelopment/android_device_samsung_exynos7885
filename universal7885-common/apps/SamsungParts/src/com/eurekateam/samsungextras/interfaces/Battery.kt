package com.eurekateam.samsungextras.interfaces

object Battery {
    var chargeSysfs: Int
        external get
        external set

    external fun setFastCharge(enable: Int)
    val fastChargeSysfs: Int
        external get

    external fun getGeneralBatteryStats(id: Int): Int
}