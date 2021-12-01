/*
 * Copyright (C) 2020 Xiaomi-SM6250 Project
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

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

// TODO: Add FPS drawables
class FPSTileService : TileService() {
    private lateinit var fpsinfo : Intent
    private var isShowing = false
    override fun onStartListening() {
        super.onStartListening()
        fpsinfo = Intent(this, FPSInfoService::class.java)
        val fpsInfoService = FPSInfoService()
        isShowing = fpsInfoService.getRunning()
        updateTile()
    }

    override fun onClick() {
        if (!isShowing) startService(fpsinfo) else stopService(fpsinfo)
        isShowing = !isShowing
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile
        tile.state =
            if (isShowing) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }
}
