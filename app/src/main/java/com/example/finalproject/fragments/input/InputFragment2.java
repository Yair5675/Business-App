package com.example.finalproject.fragments.input;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalproject.R;
import com.example.finalproject.database.AppDatabase;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.ImprovedTextWatcher;
import com.example.finalproject.util.InputValidation;
import com.example.finalproject.database.entities.User;
import com.example.finalproject.util.Result;
import com.example.finalproject.util.Util;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.function.Function;

public class InputFragment2 extends Fragment {
    // An instance of the database:
    private AppDatabase db;

    // Input field responsible for receiving the user's phone number:
    private TextInputLayout tilPhone;
    private TextInputEditText etPhone;

    // Input field responsible for receiving the user's email:
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;

    // The three input fields responsible for receiving the user's password, confirmed password, and
    // old password:
    private TextInputLayout tilPwd, tilConfirmPwd, tilOldPwd;
    private TextInputEditText etPwd, etConfirmPwd, etOldPwd;

    // A hashmap connecting input fields to their validation functions:
    private HashMap<TextInputEditText, Function<String, Result<Void, String>>> validationFunctions;

    // The mode of the fragment (can be register or update):
    private Mode mode;

    private enum Mode {
        REGISTER,
        UPDATE
    }

    /**
     * After validation occurred, the info given by the user needs to be given to the activity which
     * needs it. This class provides a convenient way for the info to pass anywhere.
     * Pay attention that although any class can READ the data that is saved, no class can create an
     * InputFragment2.PackagedInfo object, except the InputFragment2 class.
     */
    public static class PackagedInfo {
        public final String PHONE;
        public final String EMAIL;
        public final String PASSWORD;

        public PackagedInfo(String PHONE, String EMAIL, String PASSWORD) {
            this.PHONE = PHONE;
            this.EMAIL = EMAIL;
            this.PASSWORD = PASSWORD;
        }
    }

    public void loadInputsFromUser(User user) {
        // Check that the inputs are empty:
        if (this.areInputsEmpty()) {
            // Load the fields from the user:
            this.etPhone.setText(user.getPhoneNumber());
            this.etEmail.setText(user.getEmail());

            // The passwords will be empty for security reasons:
            this.etPwd.setText("");
            this.etConfirmPwd.setText("");
            this.etOldPwd.setText("");

            // Clear all errors:
            this.clearErrors();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the second input fragment:
        final View parent = inflater.inflate(R.layout.fragment_input_2, container, false);

        // Initialize the database:
        this.db = AppDatabase.getInstance(parent.getContext());

        // Set the mode:
        this.mode = AppDatabase.isUserLoggedIn() ? Mode.UPDATE : Mode.REGISTER;

        // Initialize the input fields and their text watchers:
        this.initInputFields(parent);
        this.initTextWatchers();

        // Set the IME options of the "confirm password" to be "actionNext" if the mode is UPDATE
        // or "actionDone" if the mode is REGISTER:
        this.setConfirmPwdIme();

        // If a user is logged in, use the info from them:
        if (this.mode == Mode.UPDATE)
            this.loadInputsFromUser(AppDatabase.getConnectedUser());

        return parent;
    }

    @Override
    public void onResume() {
        super.onResume();

        // If all inputs are empty, clear the errors:
        this.clearErrors();
    }

    private void setConfirmPwdIme() {
        switch (this.mode) {
            case REGISTER: {
                this.etConfirmPwd.setImeOptions(EditorInfo.IME_ACTION_DONE);
                break;
            }
            case UPDATE: {
                this.etConfirmPwd.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                break;
            }
        }
    }

    private void clearErrors() {
        this.tilPhone.setError(null);
        this.tilPhone.clearFocus();
        this.tilEmail.setError(null);
        this.tilEmail.clearFocus();
        this.tilPwd.setError(null);
        this.tilPwd.clearFocus();
        this.tilConfirmPwd.setError(null);
        this.tilConfirmPwd.clearFocus();
        this.tilOldPwd.setError(null);
        this.tilOldPwd.clearFocus();
    }

    private void initInputFields(View parent) {
        // Load the phone input layout and edit text:
        this.etPhone = parent.findViewById(R.id.fragInput2EtPhone);
        this.tilPhone = parent.findViewById(R.id.fragInput2TilPhone);

        // Load the email input layout and edit text:
        this.etEmail = parent.findViewById(R.id.fragInput2EtEmail);
        this.tilEmail = parent.findViewById(R.id.fragInput2TilEmail);

        // Load the password input layout and edit text:
        this.etPwd = parent.findViewById(R.id.fragInput2EtPassword);
        this.tilPwd = parent.findViewById(R.id.fragInput2TilPassword);

        // Load the confirm password input layout and edit text:
        this.etConfirmPwd = parent.findViewById(R.id.fragInput2EtConfirmPassword);
        this.tilConfirmPwd = parent.findViewById(R.id.fragInput2TilConfirmPassword);

        // Load the old password input layout and edit text:
        this.etOldPwd = parent.findViewById(R.id.fragInput2EtOldPassword);
        this.tilOldPwd = parent.findViewById(R.id.fragInput2TilOldPassword);

        // Hide the old password if the mode is register:
        this.tilOldPwd.setVisibility(this.mode == Mode.REGISTER ? View.GONE : View.VISIBLE);
    }

    private void initTextWatchers() {
        // Map each input field to its validation method:
        this.validationFunctions = new HashMap<>();
        this.validationFunctions.put(this.etPhone, this::validatePhone);
        this.validationFunctions.put(this.etEmail, this::validateEmail);
        this.validationFunctions.put(this.etPwd, InputValidation::validatePassword);
        this.validationFunctions.put(this.etConfirmPwd,
                (pwd) -> {
                    if (pwd.isEmpty())
                        return Result.failure(Constants.MANDATORY_INPUT_ERROR);
                    else if (!pwd.equals(Util.getTextFromEt(this.etPwd)))
                        return Result.failure("Passwords do not match");
                    else
                        return Result.success(null);
                });
        this.validationFunctions.put(this.etOldPwd, this::validateOldPwd);

        // List the input fields that need a text watcher:
        final TextInputEditText[] fields = {
                this.etPhone, this.etEmail, this.etConfirmPwd, this.etOldPwd
        };
        final TextInputLayout[] layouts = {
                this.tilPhone, this.tilEmail, this.tilConfirmPwd, this.tilOldPwd
        };

        // Add the improved text watcher to each of the input fields and their layouts:
        for (int index = 0; index < fields.length; index++) {
            int i = index;
            fields[index].addTextChangedListener(
                    (ImprovedTextWatcher)
                            (_c, _i, _i1, _i2) -> {
                                // Get the validator for the current field:
                                Function<String, Result<Void, String>> validator;
                                validator = validationFunctions.get(fields[i]);
                                if (validator != null)
                                    Util.validateAndSetError(layouts[i], fields[i], validator);
                            }
            );
        }

        // Set the password textWatcher separately because it needs to call the confirm password's
        // text watcher:
        this.etPwd.addTextChangedListener(
                (ImprovedTextWatcher)
                        (_c, _i, _i1, _i2) -> {
                            // Get the validator for the current field:
                            Function<String, Result<Void, String>> validator;
                            validator = validationFunctions.get(etPwd);
                            if (validator != null)
                                Util.validateAndSetError(tilPwd, etPwd, validator);

                            // Trigger the confirm password text watcher:
                            etConfirmPwd.setText(Util.getTextFromEt(etConfirmPwd));
                        }
        );
    }

    private Result<Void, String> validatePhone(String phone) {
        // Check for syntactical errors:
        Result<Void, String> syntaxResult = InputValidation.validatePhone(phone);
        if (syntaxResult.isErr())
            return syntaxResult;

        // Check that the phone isn't used by another user:
        User user = this.db.userDao().getUserByPhone(phone);
        if (user == null || user.equals(AppDatabase.getConnectedUser()))
            return Result.success(null);
        else
            return Result.failure("This phone belongs to another user already");
    }

    private Result<Void, String> validateEmail(String email) {
        // Check for syntactical errors:
        Result<Void, String> syntaxResult = InputValidation.validateEmail(email);
        if (syntaxResult.isErr())
            return syntaxResult;

        // Check that the email isn't used by another user:
        User user = this.db.userDao().getUserByEmail(email);
        if (user == null || user.equals(AppDatabase.getConnectedUser()))
            return Result.success(null);
        else
            return Result.failure("Email belongs to another user already");
    }

    private Result<Void, String> validateOldPwd(String password) {
        // If the current mode is REGISTER, the old password field is not checked:
        if (this.mode == Mode.REGISTER)
            return Result.success(null);

        // Check that the password isn't empty:
        if (password.isEmpty())
            return Result.failure(Constants.MANDATORY_INPUT_ERROR);
        // Check the password of the connected user:
        else if (AppDatabase.getConnectedUser().getPassword().equals(password))
            return Result.success(null);
        else
            return Result.failure("Wrong password given");
    }

    /**
     * Checks all inputs and their validity. If some inputs are invalid, the function will present
     * an error to the user (if one wasn't presented already).
     * @return True if all inputs are valid, False otherwise.
     */
    public boolean areInputsValid() {
        // Check all the input fields:
        final TextInputEditText[] fields = {
                this.etPhone, this.etEmail, this.etPwd, this.etConfirmPwd, this.etOldPwd
        };
        final TextInputLayout[] layouts = {
                this.tilPhone, this.tilEmail, this.tilPwd, this.tilConfirmPwd, this.tilOldPwd
        };

        boolean areInputsValid = true;
        Function<String, Result<Void, String>> validator;
        for (int i = 0; i < fields.length; i++) {
            // Apply validation:
            validator = this.validationFunctions.get(fields[i]);
            if (validator == null)
                continue;

            Util.validateAndSetError(layouts[i], fields[i], validator);
            areInputsValid &= layouts[i].getError() == null;
        }

        return areInputsValid;
    }

    /**
     * Checks that all inputs are empty, useful when loading information while avoiding overwriting
     * previous information.
     * @return True if all inputs are empty, false if even one is filled.
     */
    private boolean areInputsEmpty() {
        // Check phone:
        boolean isEmpty = Util.getTextFromEt(this.etPhone).isEmpty();

        // Check email:
        isEmpty &= Util.getTextFromEt(this.etEmail).isEmpty();

        // Check all passwords (and consider the mode in the old password):
        isEmpty &= Util.getTextFromEt(this.etPwd).isEmpty();
        isEmpty &= Util.getTextFromEt(this.etConfirmPwd).isEmpty();
        if (this.mode == Mode.UPDATE)
            isEmpty &= Util.getTextFromEt(this.etOldPwd).isEmpty();

        return isEmpty;
    }

    /**
     * Collects the info given by the user in this fragment and packages it for convenient use.
     * Pay attention this function should be called after calling the 'areInputsValid' function,
     * and making sure it returns true.
     * @return An InputFragment2.PackagedInfo object for convenient use of the info given by the
     *         user in this fragment.
     */
    public InputFragment2.PackagedInfo getPackagedInfo() {
        return new PackagedInfo(
                Util.getTextFromEt(this.etPhone),
                Util.getTextFromEt(this.etEmail),
                Util.getTextFromEt(this.etPwd)
        );
    }
}
