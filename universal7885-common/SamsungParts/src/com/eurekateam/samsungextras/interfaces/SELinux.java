package com.eurekateam.samsungextras.interfaces;

public class SELinux {
    public static native void setSELinux(int enable);
    public static native int getSELinux();
}
