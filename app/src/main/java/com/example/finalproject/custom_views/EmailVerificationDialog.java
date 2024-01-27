package com.example.finalproject.custom_views;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;


import com.example.finalproject.R;
import com.example.finalproject.threads.EmailVerificationThread;

public class EmailVerificationDialog implements View.OnClickListener {
    // The actual dialog:
    private final Dialog dialog;

    // The thread that will check if the email was verified:
    private final EmailVerificationThread emailVerificationThread;

    public EmailVerificationDialog(
            Context context,
            Resources res,
            Runnable onEmailVerifiedCallback,
            DialogInterface.OnCancelListener onCanceledCallback
    ) {
        // Binding the dialog to its XML:
        this.dialog = new Dialog(context);
        this.dialog.setContentView(R.layout.dialog_email_verification);

        // Initialize the thread:
        Handler handler = new Handler(Looper.getMainLooper(), message -> {
            // If a message was received then the email was verified:
            onEmailVerifiedCallback.run();
            return false;
        });
        this.emailVerificationThread = new EmailVerificationThread(handler);

        // Set the onCancelListener:
        this.dialog.setOnCancelListener(dialogInterface -> {
            // Stop the thread:
            emailVerificationThread.interrupt();

            // Activate the onCancelCallback:
            onCanceledCallback.onCancel(dialogInterface);
        });

        // Setting the width of the dialog to 90% the screen, and its height to minimal:
        final int width = (int) (res.getDisplayMetrics().widthPixels * 0.9);
        final int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        Window window;
        if ((window = this.dialog.getWindow()) != null)
            window.setLayout(width, height);

        // Configure the on click listeners for the buttons:
        this.dialog.findViewById(R.id.dialogEmailVerificationBtnResend).setOnClickListener(this);
    }

    public void show() {
        // Activate the email verification thread:
        this.emailVerificationThread.start();

        // Show the dialog:
        this.dialog.show();
    }

    @Override
    public void onClick(View view) {
        final int ID = view.getId();

        if (ID == R.id.dialogEmailVerificationBtnResend) {
            // TODO: Resend an email
        }
    }
}
