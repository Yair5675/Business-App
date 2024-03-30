package com.example.finalproject.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.finalproject.R;
import com.example.finalproject.activities.MainActivity;
import com.example.finalproject.database.online.collections.Shift;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ShiftNotificationService extends Service {
    // Whether the service was already started:
    private boolean isRunning;

    // The list of shifts of the user:
    private List<Shift> shifts;

    // An index pointing to the closest shift in the shifts list:
    private int closestShiftIdx;

    // The listener to the database:
    private ListenerRegistration shiftsListener;

    // The count down timer that is going to send the notification:
    private @Nullable CountDownTimer notificationTimer;

    // The amount of milliseconds before the shift starts that the notification is sent:
    private static final long MILLIS_BEFORE_SHIFT = 1000 * 60 * 5; // Five minutes

    // Tag for debugging purposes:
    private static final String TAG = "ShiftsNotificationService";

    // Constants regarding the notification channel:
    private static final String CHANNEL_ID = "ShiftNotificationService";
    private static final String CHANNEL_NAME = CHANNEL_ID;

    // The intent key for passing the user ID:
    public static final String UID_INTENT_KEY = "uidIntentKey";

    // The name of the handler thread that the service will work on:
    private static final String NOTIFICATIONS_SCHEDULER_THREAD_NAME = "Notification Scheduler Thread Name";

    public ShiftNotificationService() {
        this.closestShiftIdx = -1;
        this.notificationTimer = null;
        this.isRunning = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Establish a notification channel:
        final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        final NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        // Initialize the closest shift index:
        this.setClosestShiftIdx(0);
    }

    private void handleShiftsChanges(@Nullable QuerySnapshot shifts, @Nullable FirebaseFirestoreException error) {
        if (error != null) {
            Log.e(TAG, "Error listening to shifts", error);
            return;
        }

        if (shifts != null) {
            // Define a HandlerThread for the notification scheduling:
            final HandlerThread handlerThread = new HandlerThread(NOTIFICATIONS_SCHEDULER_THREAD_NAME);
            handlerThread.start();

            // Create a Handler associated with the HandlerThread
            final Handler handler = new Handler(handlerThread.getLooper());

            // Schedule the shifts in a separate thread:
            handler.post(() -> {
                // Save the shifts:
                this.shifts = shifts.toObjects(Shift.class);

                // Update the closest shift index:
                this.refreshClosestShiftIndex();

                // Schedule new notifications:
                if (this.getClosestShiftIdx() >= 0 && this.getClosestShiftIdx() < this.shifts.size())
                    this.scheduleNotifications();
            });
        }
    }

    private synchronized int getClosestShiftIdx() {
        return closestShiftIdx;
    }

    private synchronized void setClosestShiftIdx(int closestShiftIdx) {
        this.closestShiftIdx = closestShiftIdx;
    }

    private void refreshClosestShiftIndex() {
        // Keep the previous shift index if possible:
        this.setClosestShiftIdx(Math.min(this.shifts.size() - 1, this.getClosestShiftIdx()));
        final long now = System.currentTimeMillis();
        boolean found = false;
        while (!found && this.getClosestShiftIdx() >= 0 && this.getClosestShiftIdx() < this.shifts.size()) {
            final Shift currentShift = this.shifts.get(this.getClosestShiftIdx());
            // If the current shift is more than five minutes after now:
            if (currentShift.getStartingTime().getTime() - MILLIS_BEFORE_SHIFT > now) {
                if (this.getClosestShiftIdx() == 0)
                    found = true;
                    // If the previous shift is before now:
                else if (this.shifts.get(this.getClosestShiftIdx() - 1).getStartingTime().getTime() - MILLIS_BEFORE_SHIFT < now)
                    found = true;
                else
                    this.setClosestShiftIdx(this.getClosestShiftIdx() - 1);
            }
            else
                this.setClosestShiftIdx(this.getClosestShiftIdx() + 1);
        }
        if (this.getClosestShiftIdx() >= this.shifts.size())
            Log.i(TAG, "No future shifts to notify about");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start only if the service didn't start already:
        if (this.isRunning)
            return START_STICKY;

        this.isRunning = true;

        // Get the UID from the intent:
        if (intent == null) {
            this.isRunning = false;
            return START_STICKY;
        }
        final String uid = intent.getStringExtra(UID_INTENT_KEY);

        // Attach the listener (and detach previous ones just in case):
        if (this.shiftsListener != null)
            this.shiftsListener.remove();

        this.shiftsListener = FirebaseFirestore.getInstance()
                .collection("shifts")
                .whereEqualTo(Shift.UID, uid)
                .whereGreaterThan(Shift.STARTING_TIME, new Date())
                .orderBy(Shift.STARTING_TIME)
                .addSnapshotListener(this::handleShiftsChanges);

        return START_STICKY;
    }

    private void scheduleNotifications() {
        // Get the difference in milliseconds between now and the closest shift (minus the
        // determined time):
        final Shift closestShift = this.shifts.get(this.getClosestShiftIdx());
        final long diff = closestShift.getStartingTime().getTime() - MILLIS_BEFORE_SHIFT - new Date().getTime();

        // Start the count down timer:
        if (this.notificationTimer != null)
            this.notificationTimer.cancel();

        // Notify every minute how much time is left:
        this.notificationTimer = new CountDownTimer(diff, 1000 * 60) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, (millisUntilFinished / 1000) + " seconds until notification is sent");
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "Count down stopped, sending notification");

                // Make sure that shifts are not null:
                if (shifts == null) {
                    Log.e(TAG, "Shifts are null when timer stopped");
                    return;
                }

                // Loop over every shift whose starting time is in 5 minutes
                final long now = System.currentTimeMillis();
                for (int i = getClosestShiftIdx(); i < shifts.size() && shifts.get(i).getStartingTime().getTime() - MILLIS_BEFORE_SHIFT <= now; i++) {
                    sendNotification(shifts.get(i));
                }

                // When
            }
        }.start();
    }

    private void sendNotification(Shift shift) {
        // Go to the main activity if the user taps the notification:
        final Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("You have a shift soon!")
                .setContentText(String.format(
                        Locale.getDefault(),
                        "Your shift in %s begins in %d minutes!",
                        shift.getCompanyName(), MILLIS_BEFORE_SHIFT / (1000 * 60)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
            NotificationManagerCompat.from(this).notify(new Random().nextInt(), builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Detach the listener:
        if (this.shiftsListener != null)
            this.shiftsListener.remove();

        // Cancel the count down timer:
        if (this.notificationTimer != null)
            this.notificationTimer.cancel();

        this.isRunning = false;
    }
}
