package com.eurekateam.samsungextras.interfaces;

public class Battery {
   public native void setChargeSysfs(int enable);
   public native int getChargeSysfs();
}
