package com.example.finalproject.activities;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.finalproject.broadcast_receivers.InternetBroadcastReceiver;
import com.example.finalproject.broadcast_receivers.OnInternetConnectivityChanged;
import com.example.finalproject.util.Util;

public class ShiftFlowApplication extends Application implements Application.ActivityLifecycleCallbacks, OnInternetConnectivityChanged{
    // The no wifi dialog:
    private @Nullable Dialog noInternetDialog;

    // The broadcast receiver of the internet:
    private final InternetBroadcastReceiver receiver;

    // The intent filter for the receiver:
    private static final IntentFilter INTERNET_INTENT_FILTER = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

    public ShiftFlowApplication() {
        this.receiver = new InternetBroadcastReceiver(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Register the life cycle callbacks for every activity:
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        // Unregister the callbacks:
        unregisterActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        // Create the noInternetDialog:
        this.noInternetDialog = Util.getNoInternetDialog(activity);

        // Register the receiver:
        activity.registerReceiver(this.receiver, INTERNET_INTENT_FILTER);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        // Unregister the receiver:
        activity.unregisterReceiver(this.receiver);
    }

    @Override
    public void onInternetAvailable() {
        if (this.noInternetDialog != null)
            this.noInternetDialog.dismiss();
    }

    @Override
    public void onInternetUnavailable() {
        if (this.noInternetDialog != null)
            this.noInternetDialog.show();
    }
}
