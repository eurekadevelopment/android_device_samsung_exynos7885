/*
 * Copyright (C) 2022 Eureka Team
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
package com.eurekateam.samsungextras.interfaces

import android.os.ServiceManager
import vendor.eureka.hardware.parts.BatterySys
import vendor.eureka.hardware.parts.IBatteryStats

class Battery {
    private val mBattery: IBatteryStats

    init {
        mBattery = IBatteryStats.Stub.asInterface(ServiceManager.waitForDeclaredService("vendor.eureka.hardware.parts.IBatteryStats/default"))
    }

    var Charge: Boolean
        get() {
            return mBattery.getBatteryStats(BatterySys.CHARGE) == 0
        }
        set(k) {
            mBattery.setBatteryWritable(BatterySys.CHARGE, !k)
        }

    var FastCharge: Boolean
        get() {
            return mBattery.getBatteryStats(BatterySys.FASTCHARGE) == 0
        }
        set(k) {
            mBattery.setBatteryWritable(BatterySys.FASTCHARGE, !k)
        }

    fun getGeneralBatteryStats(id: BatteryIds): Int = when (id) {
        BatteryIds.BATTERY_CAPACITY_MAX -> mBattery.getBatteryStats(BatterySys.CAPACITY_MAX) / 1000
        BatteryIds.BATTERY_CAPACITY_CURRENT -> mBattery.getBatteryStats(BatterySys.CAPACITY_CURRENT)
        BatteryIds.BATTERY_CAPACITY_CURRENT_MAH -> (mBattery.getBatteryStats(BatterySys.CAPACITY_CURRENT).toFloat() * mBattery.getBatteryStats(BatterySys.CAPACITY_MAX).toFloat() / 100000).toInt()
        BatteryIds.CHARGING_STATE -> if (mBattery.getBatteryStats(BatterySys.CURRENT) > 0) 1 else 0
        BatteryIds.BATTERY_TEMP -> mBattery.getBatteryStats(BatterySys.TEMP) / 10
        BatteryIds.BATTERY_CURRENT -> mBattery.getBatteryStats(BatterySys.CURRENT)
    }
}
