package com.eurekateam.samsungextras

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.eurekateam.samsungextras.dolby.DolbyCore

class DolbyTile : TileService() {
    private var isRunning = false
    private var dolbyCore = DolbyCore()
    override fun onStartListening() {
        super.onStartListening()
        isRunning = dolbyCore.isRunning()
        updateTile()
    }

    override fun onClick() {
        if (isRunning) {
            dolbyCore.stopDolbyEffect()
        } else {
            dolbyCore.justStartOnly()
        }
        isRunning = !isRunning
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile
        tile.state =
            if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }
}