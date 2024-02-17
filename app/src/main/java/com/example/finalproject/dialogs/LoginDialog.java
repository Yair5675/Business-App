package com.example.finalproject.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.finalproject.R;
import com.example.finalproject.database.online.OnlineDatabase;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.ImprovedTextWatcher;
import com.example.finalproject.util.InputValidation;
import com.example.finalproject.util.Result;
import com.example.finalproject.util.Util;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.function.Function;

public class LoginDialog {
    // A reference to the database:
    private final OnlineDatabase db;

    // The context of the dialog:
    private final Context context;

    // The actual dialog this class represents:
    private final Dialog dialog;

    // The callback that will be called in case the sign in was a success:
    private final OnSuccessListener<User> onSuccessCallback;

    // The input fields inside the dialog responsible for the email:
    private final TextInputLayout tilEmail;
    private final TextInputEditText etEmail;

    // The input fields inside the dialog responsible for the email password:
    private final TextInputLayout tilPassword;
    private final TextInputEditText etPassword;

    // The button for submitting the results:
    private final Button btnSubmit;

    // The progress bar that will be shown once the validation is happening:
    private final ProgressBar pbValidating;

    // Tag for logging purposes:
    public static final String TAG = "Login Dialog";

    public LoginDialog (Context context, Resources res, OnSuccessListener<User> onSuccessCallback) {
        // Binding the dialog to its XML:
        this.dialog = new Dialog(context);
        this.dialog.setContentView(R.layout.dialog_login);

        // Save the context:
        this.context = context;

        // Save the callbacks:
        this.onSuccessCallback = onSuccessCallback;

        // Initialize the database reference:
        this.db = OnlineDatabase.getInstance();

        // Setting the width of the dialog to 90% the screen, and its height to minimal:
        final int width = (int) (res.getDisplayMetrics().widthPixels * 0.9);
        final int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        Window window;
        if ((window = this.dialog.getWindow()) != null)
            window.setLayout(width, height);

        // Loading pointers to the views inside the dialog:
        this.etEmail = this.dialog.findViewById(R.id.dialogLoginEtEmail);
        this.tilEmail = this.dialog.findViewById(R.id.dialogLoginTilEmail);

        this.etPassword = this.dialog.findViewById(R.id.dialogLoginEtPassword);
        this.tilPassword = this.dialog.findViewById(R.id.dialogLoginTilPassword);

        this.btnSubmit = this.dialog.findViewById(R.id.dialogLoginBtnSubmit);

        this.pbValidating = this.dialog.findViewById(R.id.dialogLoginPbValidating);

        // Initializing the button's onClickListener:
        this.initSubmitButtonClick();

        // Initialize text watchers:
        this.initTextWatchers();
    }

    /**
     * Wrapper of the dialog's show method. The function will clear out previous input saved in the
     * dialog and then show it, allowing re-usability of the dialog and saving resources.
     */
    public void show() {
        // Clear out previous info:
        this.clearEditTexts();

        // Clear errors:
        this.clearErrors();

        // Make the progress bar disappear:
        this.pbValidating.setVisibility(View.GONE);

        // Clear previous focus:
        final View focusedView = this.dialog.getCurrentFocus();
        if (focusedView != null)
            focusedView.clearFocus();

        // Show the dialog:
        this.dialog.show();
    }

    private void clearErrors() {
        for (TextInputLayout inputLayout : this.getInputLayouts())
            inputLayout.setError("");
    }

    private void clearEditTexts() {
        for (EditText et : this.getEditTexts())
            et.setText("");
    }

    private void initTextWatchers() {
        // Create a simple validator that just checks if the input is empty:
        final Function<String, Result<Void, String>> validator = (input) -> {
            if (input.isEmpty())
                return Result.failure(Constants.MANDATORY_INPUT_ERROR);
            else
                return Result.success(null);
        };

        // Map the validator to every input field:
        final TextInputLayout[] layouts = getInputLayouts();
        final TextInputEditText[] ets = getEditTexts();
        for (int i = 0; i < layouts.length; i++) {
            final int index = i;
            ets[i].addTextChangedListener(
                    (ImprovedTextWatcher)
                            (_c, _i, _i1, _i2) ->
                                    Util.validateAndSetError(layouts[index], ets[index], validator)
            );
        }
    }

    @NonNull
    private TextInputLayout[] getInputLayouts() {
        return new TextInputLayout[] { this.tilEmail, this.tilPassword };
    }

    @NonNull
    private TextInputEditText[] getEditTexts() {
        return new TextInputEditText[] { this.etEmail, this.etPassword };
    }

    private void initSubmitButtonClick() {
        this.btnSubmit.setOnClickListener(view -> {
            // Show the progress bar and hide the login button:
            this.pbValidating.setVisibility(View.VISIBLE);
            this.btnSubmit.setVisibility(View.GONE);

            // Validate the inputs:
            final Result<Void, String> syntaxValidation = this.validateInputSyntax();

            // Proceed to the database only if the syntax is valid:
            if (syntaxValidation.isErr()) {
                Toast.makeText(this.context, syntaxValidation.getError(), Toast.LENGTH_SHORT).show();
                this.btnSubmit.setVisibility(View.VISIBLE);
                this.pbValidating.setVisibility(View.GONE);
                return;
            }

            // Try to log in with the info:
            final String email = Util.getTextFromEt(this.etEmail);
            final String password = Util.getTextFromEt(this.etPassword);
            this.db.logUserIn(email, password, user -> {
                // Hide the progress bar and show the button:
                pbValidating.setVisibility(View.GONE);
                btnSubmit.setVisibility(View.VISIBLE);

                // Activate the callback and dismiss the dialog:
                onSuccessCallback.onSuccess(user);
                dialog.dismiss();
            }, e -> {
                // Log the error:
                Log.e(TAG, "Error signing in", e);

                // Make a toast message:
                Toast.makeText(context, "Incorrect info given", Toast.LENGTH_SHORT).show();

                // Hide the progress bar and show the button:
                pbValidating.setVisibility(View.GONE);
                btnSubmit.setVisibility(View.VISIBLE);
            });

        });
    }

    private Result<Void, String> validateInputSyntax() {
        // Get the email and password:
        final String email = Util.getTextFromEt(this.etEmail);
        final String password = Util.getTextFromEt(this.etPassword);

        // If any of them are missing, return an error:
        final boolean isEmailMissing = email.isEmpty(), isPasswordMissing = password.isEmpty();

        this.tilEmail.setError(isEmailMissing ? Constants.MANDATORY_INPUT_ERROR : null);
        this.tilPassword.setError(isPasswordMissing ? Constants.MANDATORY_INPUT_ERROR : null);

        if (isEmailMissing || isPasswordMissing)
            return Result.failure("Some fields are missing");

        // Check the syntax:
        final boolean isInputValid = InputValidation.validateEmail(email).isOk() &&
                InputValidation.validatePassword(password).isOk();

        // Return the result:
        return isInputValid ? Result.success(null) : Result.failure("Incorrect info given");
    }
}
