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
import vendor.eureka.hardware.parts.ISmartCharge

class SmartCharge {
    private val mSmartCharge: ISmartCharge

    init {
        mSmartCharge = ISmartCharge.Stub.asInterface(ServiceManager.waitForDeclaredService("vendor.eureka.hardware.parts.ISmartCharge/default"))
    }

    fun start() = mSmartCharge.start()
    fun stop() = mSmartCharge.stop()
    fun setConfig(limit: Int, restart: Int) = mSmartCharge.setConfig(limit, restart)

    fun getStats(type: StatsType): Int = when (type) {
        StatsType.TYPE_LIMITED_CNT -> mSmartCharge.getLimitCnt()
        StatsType.TYPE_RESTARTED_CNT -> mSmartCharge.getRestartCnt()
    }
}
