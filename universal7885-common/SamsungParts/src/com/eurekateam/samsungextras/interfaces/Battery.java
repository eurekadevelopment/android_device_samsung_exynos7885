package com.eurekateam.samsungextras.interfaces;

public class Battery {
   public static native void setChargeSysfs(int enable);
   public static native int getChargeSysfs();
   public static native void setFastCharge(int enable);
   public static native int getFastChargeSysfs();
   public static native int getGeneralBatteryStats(int id);
}
