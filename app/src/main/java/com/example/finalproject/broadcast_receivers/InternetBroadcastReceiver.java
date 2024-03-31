package com.example.finalproject.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetBroadcastReceiver extends BroadcastReceiver {
    // The callbacks handling changes in the internet:
    private final OnInternetConnectivityChanged internetCallback;

    public InternetBroadcastReceiver(OnInternetConnectivityChanged internetCallback) {
        this.internetCallback = internetCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Activate the callbacks according to the state of the internet:
        if (isOnline(context))
            this.internetCallback.onInternetAvailable();
        else
            this.internetCallback.onInternetUnavailable();
    }

    private static boolean isOnline(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
