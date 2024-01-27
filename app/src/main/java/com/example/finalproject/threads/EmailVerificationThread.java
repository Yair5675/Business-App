package com.example.finalproject.threads;

import android.os.Handler;

import com.example.finalproject.database.online.OnlineDatabase;

public class EmailVerificationThread extends Thread {
    // A reference to the database:
    private final OnlineDatabase db;

    // The handler of the thread, enables the thread to inform the calling thread when the email
    // was verified:
    private final Handler handler;

    public EmailVerificationThread(Handler handler) {
        this.handler = handler;
        this.db = OnlineDatabase.getInstance();
    }

    @Override
    public void run() {
        super.run();

        while (!this.db.isConnectedUserEmailVerified()) {
            // Sleep 1 second before checking again:
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                // Exit the thread:
                break;
            }
        }

        // Inform the calling thread that the email was verified:
        if (this.db.isConnectedUserEmailVerified())
            this.handler.sendEmptyMessage(100);
    }
}
