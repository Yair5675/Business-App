package com.example.finalproject.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetBroadcastReceiver extends BroadcastReceiver {
    // A callback that will be run once there is wifi:
    private final Runnable onInternetAvailableCallback;

    // A callback that will be run once there isn't wifi:
    private final Runnable onInternetNotAvailableCallback;

    public InternetBroadcastReceiver(Runnable onInternetAvailableCallback, Runnable onInternetNotAvailableCallback) {
        this.onInternetAvailableCallback = onInternetAvailableCallback;
        this.onInternetNotAvailableCallback = onInternetNotAvailableCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Activate the callbacks according to the state of the internet:
        if (isOnline(context))
            this.onInternetAvailableCallback.run();
        else
            this.onInternetNotAvailableCallback.run();
    }

    private static boolean isOnline(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
