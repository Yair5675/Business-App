package com.example.finalproject.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

public class Permissions {
    public static boolean checkPermissions(Context context, String ... permissions) {
        int result = Arrays.stream(permissions)
                .map(permission -> ContextCompat.checkSelfPermission(context, permission))
                .reduce(
                        PackageManager.PERMISSION_GRANTED,
                        (accumulator, current) -> {
                            if (accumulator == PackageManager.PERMISSION_DENIED || current == PackageManager.PERMISSION_DENIED)
                                return PackageManager.PERMISSION_DENIED;
                            else
                                return PackageManager.PERMISSION_GRANTED;
                        });

        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissions(Activity activity, String ... permissions) {
        ActivityCompat.requestPermissions(activity, permissions, 1);
    }
}
