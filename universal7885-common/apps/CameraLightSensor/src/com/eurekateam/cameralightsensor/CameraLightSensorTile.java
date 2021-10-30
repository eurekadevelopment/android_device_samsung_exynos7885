package com.eurekateam.cameralightsensor;

import android.content.Intent;
import android.service.quicksettings.TileService;

public class CameraLightSensorTile extends TileService {
    @Override
    public void onClick() {
        try {
            Intent intent = new Intent(this, CameraLightSensorService.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
