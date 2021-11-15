package com.eurekateam.samsungextras;

import android.service.quicksettings.TileService;

import com.eurekateam.samsungextras.dolby.DolbyCore;

public class DolbyTile extends TileService {
    @Override
    public void onClick() {
        DolbyCore dolbyCore = new DolbyCore();
        if(dolbyCore.isRunning()){
            dolbyCore.stopDolbyEffect();
        }else{
            dolbyCore.justStartOnly();
        }
    }

}