package com.eurekateam.samsungextras.utils;

/*
 * Copyright (C) 2016 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.os.FileUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;


public final class FileUtilsWrapper {
    private static final String TAG = "FileUtils";

    /**
     * Reads the first line of text from the given file.
     * Reference {@link FileUtils} for clarification on what a line is
     *
     */
    public static String readOneLine(String fileName){
        try {
            return FileUtils.readTextFile(new File(fileName),0,null)
            .replaceAll("[\n]","");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Writes the given value into the given file
     *
     */
    public static void writeLine(String fileName, String value){
        try {
            FileUtils.stringToFile(fileName,value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks whether the given file exists
     *
     * @return true if exists, false if not
     */
    public static boolean fileExists(String fileName) {
        final File file = new File(fileName);
        return file.exists();
    }

    /**
     * Checks whether the given file is readable
     *
     * @return true if readable, false if not
     */
    public static boolean isFileReadable(String fileName) {
        final File file = new File(fileName);
        return file.exists() && file.canRead();
    }

    /**
     * Checks whether the given file is writable
     *
     * @return true if writable, false if not
     */
    public static boolean isFileWritable(String fileName) {
        final File file = new File(fileName);
        return file.exists() && file.canWrite();
    }

    /**
     * Deletes an existing file
     *
     * @return true if the delete was successful, false if not
     */
    public static boolean delete(String fileName) {
        final File file = new File(fileName);
        boolean ok = false;
        try {
            ok = file.delete();
        } catch (SecurityException e) {
            Log.w(TAG, "SecurityException trying to delete " + fileName, e);
        }
        return ok;
    }

    /**
     * Renames an existing file
     *
     * @return true if the rename was successful, false if not
     */
    public static boolean rename(String srcPath, String dstPath) {
        final File srcFile = new File(srcPath);
        final File dstFile = new File(dstPath);
        boolean ok = false;
        try {
            ok = srcFile.renameTo(dstFile);
        } catch (SecurityException e) {
            Log.w(TAG, "SecurityException trying to rename " + srcPath + " to " + dstPath, e);
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException trying to rename " + srcPath + " to " + dstPath, e);
        }
        return ok;
    }
}
