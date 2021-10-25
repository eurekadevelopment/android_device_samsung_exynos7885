package com.eurekateam.samsungextras.utils;

import android.util.Log;

import com.eurekateam.samsungextras.GlobalConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SystemProperties {

    /**
     * To read a prop using getprop
     * @param propName the prop name to get
     * @return the prop's value, empty string is not found
     */
    public static String read(String propName) {
        Process process = null;
        BufferedReader bufferedReader = null;

        String TAG = GlobalConstants.TAG;
        try {
            String GETPROP_EXECUTABLE_PATH = "/system/bin/getprop";
            process = new ProcessBuilder().command(GETPROP_EXECUTABLE_PATH, propName).redirectErrorStream(true).start();
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = bufferedReader.readLine();
            if (line == null){
                line = ""; //prop not set
            }
            Log.i(TAG,"read System Property: " + propName + "=" + line);
            return line;
        } catch (Exception e) {
            Log.e(TAG,"Failed to read System Property " + propName,e);
            return "";
        } finally{
            if (bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException ignored) {}
            }
            if (process != null){
                process.destroy();
            }
        }
    }

    /**
     * @return The status of SELinux on the device
     * true: enforcing, false: permissive
     */
    public static boolean getenforce() {
        Process process = null;
        BufferedReader bufferedReader;
        String TAG = GlobalConstants.TAG;
        try {
            String GETENFORCE_EXECUTABLE_PATH = "/system/bin/getenforce";
            process = new ProcessBuilder().command(GETENFORCE_EXECUTABLE_PATH).redirectErrorStream(true).start();
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = bufferedReader.readLine();
            if (line == null) {
                line = ""; //prop not set
            }
            Log.i(TAG, "Getenforce: " + line);
            return line.equals("Enforcing");
        } catch (Exception e) {
            Log.e(TAG, "Getenforce failed", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }
}
