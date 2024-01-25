package com.example.finalproject.custom_views;

import android.app.Dialog;
import android.content.res.Resources;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.activities.MainActivity;
import com.example.finalproject.database.local.AppDatabase;
import com.example.finalproject.database.local.SharedPreferenceHandler;
import com.example.finalproject.database.local.entities.User;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.ImprovedTextWatcher;
import com.example.finalproject.util.Result;
import com.example.finalproject.util.Util;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.function.Function;

public class LoginDialog {
    // The actual dialog this class represents:
    private final Dialog dialog;

    // The main activity that holds the dialog:
    private final MainActivity mainActivity;

    // The 'Remember me' checkbox:
    private final CheckBox cbRememberMe;

    // The input fields inside the dialog responsible for the phone:
    private final TextInputLayout tilPhone;
    private final TextInputEditText etPhone;

    // The input fields inside the dialog responsible for the email:
    private final TextInputLayout tilEmail;
    private final TextInputEditText etEmail;

    // The input fields inside the dialog responsible for the email password:
    private final TextInputLayout tilPassword;
    private final TextInputEditText etPassword;

    // The button for submitting the results:
    private final Button btnSubmit;

    public LoginDialog(MainActivity mainActivity, Resources res) {
        // Binding the dialog to its XML:
        this.mainActivity = mainActivity;
        this.dialog = new Dialog(mainActivity);
        this.dialog.setContentView(R.layout.dialog_login);

        // Setting the width of the dialog to 90% the screen, and its height to minimal:
        final int width = (int) (res.getDisplayMetrics().widthPixels * 0.9);
        final int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        Window window;
        if ((window = this.dialog.getWindow()) != null)
            window.setLayout(width, height);

        // Loading pointers to the views inside the dialog:
        this.etPhone = this.dialog.findViewById(R.id.dialogLoginEtPhone);
        this.tilPhone = this.dialog.findViewById(R.id.dialogLoginTilPhone);

        this.etEmail = this.dialog.findViewById(R.id.dialogLoginEtEmail);
        this.tilEmail = this.dialog.findViewById(R.id.dialogLoginTilEmail);

        this.etPassword = this.dialog.findViewById(R.id.dialogLoginEtPassword);
        this.tilPassword = this.dialog.findViewById(R.id.dialogLoginTilPassword);

        this.cbRememberMe = this.dialog.findViewById(R.id.dialogLoginCbRememberMe);

        this.btnSubmit = this.dialog.findViewById(R.id.dialogLoginBtnSubmit);

        // Initializing the button's onClickListener:
        this.initSubmitButtonClick();

        // Initialize text watchers:
        this.initTextWatchers();

        // Show the dialog right away:
        this.dialog.show();
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
        final TextInputLayout[] layouts = { this.tilPhone, this.tilEmail, this.tilPassword };
        final TextInputEditText[] ets = { this.etPhone, this.etEmail, this.etPassword };
        for (int i = 0; i < layouts.length; i++) {
            final int index = i;
            ets[i].addTextChangedListener(
                    (ImprovedTextWatcher)
                            (_c, _i, _i1, _i2) ->
                                    Util.validateAndSetError(layouts[index], ets[index], validator)
            );
        }
    }

    private void initSubmitButtonClick() {
        this.btnSubmit.setOnClickListener(view -> {
            // Validate the inputs:
            final Result<User, String> searchResult = this.getValidationResult();

            // If the user was found:
            if (searchResult.isOk()) {
                // Connect the user to the database
                final User user = searchResult.getValue();
                this.connectUserToDatabase(user);

                // Close the dialog:
                dialog.dismiss();
            }
            // If not, make a toast message:
            else
                Toast.makeText(mainActivity, searchResult.getError(), Toast.LENGTH_SHORT).show();
        });
    }

    private Result<User, String> getValidationResult() {
        // Get the phone, email and password:
        final String phone = Util.getTextFromEt(this.etPhone);
        final String email = Util.getTextFromEt(this.etEmail);
        final String password = Util.getTextFromEt(this.etPassword);

        // If any of them are missing, return an error:
        final boolean isPhoneMissing = phone.isEmpty(), isEmailMissing = email.isEmpty(),
                isPasswordMissing = password.isEmpty();

        this.tilPhone.setError(isPhoneMissing ? Constants.MANDATORY_INPUT_ERROR : null);
        this.tilEmail.setError(isEmailMissing ? Constants.MANDATORY_INPUT_ERROR : null);
        this.tilPassword.setError(isPasswordMissing ? Constants.MANDATORY_INPUT_ERROR : null);

        if (isPhoneMissing || isEmailMissing || isPasswordMissing)
            return Result.failure("Some fields are missing");

        // Search for a user with those inputs:
        final AppDatabase db = AppDatabase.getInstance(this.mainActivity);
        return Result.ofNullable(
                db.userDao().findUserByCredentials(phone, email, password),
                "Incorrect info given"
        );
    }

    private void connectUserToDatabase(User user) {
        // Connect to the normal database:
        AppDatabase.connectUser(user);
        mainActivity.userConnectivityChanged();

        // If the user checked the 'Remember me' option, save their ID:
        if (this.cbRememberMe.isChecked()) {
            SharedPreferenceHandler spHandler = SharedPreferenceHandler.getInstance(this.mainActivity);
            spHandler.putLong("id", user.getUserId());
        }
    }
}
