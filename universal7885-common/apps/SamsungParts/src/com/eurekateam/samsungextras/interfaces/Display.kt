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
import vendor.eureka.hardware.parts.DisplaySys
import vendor.eureka.hardware.parts.IDisplayConfigs

class Display {
    private val mDisplay: IDisplayConfigs

    init {
        mDisplay = IDisplayConfigs.Stub.asInterface(ServiceManager.waitForDeclaredService(AIDLStringFactory.IDisplay))
    }

    var DT2W: Boolean = false
        set(k) = mDisplay.writeDisplay(k, DisplaySys.DOUBLE_TAP)

    var GloveMode: Boolean = false
        set(k) = mDisplay.writeDisplay(k, DisplaySys.GLOVE_MODE)
}
