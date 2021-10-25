package com.eurekateam.samsungextras.interfaces;

public class Battery {
   public static native void setChargeSysfs(int enable);
   public static native int getChargeSysfs();
}
