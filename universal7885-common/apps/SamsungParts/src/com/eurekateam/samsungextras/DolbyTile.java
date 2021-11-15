package com.eurekateam.samsungextras;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.eurekateam.samsungextras.dolby.DolbyCore;

public class DolbyTile extends TileService {
    private boolean isRunning;
    DolbyCore dolbyCore = new DolbyCore();
    @Override
    public void onStartListening() {
        super.onStartListening();
        isRunning = dolbyCore.isRunning();
        updateTile();
    }

    @Override
    public void onClick() {
        if (isRunning) {
            dolbyCore.stopDolbyEffect();
        }else{
            dolbyCore.justStartOnly();
        }
        isRunning = !isRunning;
        updateTile();
    }
    private void updateTile() {
        final Tile tile = getQsTile();
        tile.setState(isRunning ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }
}
