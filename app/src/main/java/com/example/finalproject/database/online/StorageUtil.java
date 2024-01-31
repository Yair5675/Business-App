package com.example.finalproject.database.online;

import java.util.Locale;

public class StorageUtil {
    public static String getStorageImagePath(String uid) {
        return String.format(Locale.getDefault(), "images/%s/%d", uid, System.currentTimeMillis());
    }
}
