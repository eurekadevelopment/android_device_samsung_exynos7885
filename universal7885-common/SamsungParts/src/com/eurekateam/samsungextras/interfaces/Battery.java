package com.eurekateam.samsungextras.interfaces;

import vendor.eureka.hardware.battery.V1_0.IBattery;
import vendor.eureka.hardware.battery.V1_0.SysfsType;
import vendor.eureka.hardware.battery.V1_0.Number;

public class Battery {
   public void setChargeSysfs(boolean enable){
   	IBattery.getService(true);
   	IBattery.setBatteryWritable(SysfsType::CHARGE, enable ? Number::DISABLE : Number::ENABLE);
   }
   public int getChargeSysfs(){
   	IBattery.getService(true);
   	return Integral.parseInt(IBattery.getBatteryStats(SysfsType::CHARGE));
   }
}
