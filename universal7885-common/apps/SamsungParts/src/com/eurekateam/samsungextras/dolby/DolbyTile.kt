package com.eurekateam.samsungextras.dolby

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.content.Intent

class DolbyTile : TileService() {
    override fun onClick() {
	val mIntent = Intent(this, DolbyCore::class.java)
        if (DolbyCore.mAudioEffect.enabled) {
           mIntent.putExtra(DolbyCore.DAP_ENABLED, false)
        } else {
           mIntent.putExtra(DolbyCore.DAP_ENABLED, true)
	   mIntent.putExtra(DolbyCore.DAP_PROFILE, DolbyCore.mCurrentProfile)
        }
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile
        tile.state =
            if (DolbyCore.mAudioEffect.enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }
}
