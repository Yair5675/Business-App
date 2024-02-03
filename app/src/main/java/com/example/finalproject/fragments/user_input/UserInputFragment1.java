package com.example.finalproject.fragments.user_input;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.finalproject.R;
import com.example.finalproject.database.online.OnlineDatabase;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.input.InputFragment;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.ImprovedTextWatcher;
import com.example.finalproject.util.InputValidation;
import com.example.finalproject.util.Result;
import com.example.finalproject.util.Util;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

public class UserInputFragment1 extends InputFragment {
    // A reference to the database:
    private OnlineDatabase db;

    // A reference to the user whose details are being changed:
    private final User user;

    // The input fields responsible for getting the name of the user:
    private TextInputLayout tilName, tilSurname;
    private TextInputEditText etName, etSurname;

    // The input field responsible for getting the user's birth date:
    private TextInputLayout tilBirthdate;
    private TextInputEditText etBirthdate;

    // The actual birthdate given by the user:
    private Date birthdate;

    // Input field responsible for receiving the user's email:
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;

    // The input field responsible for receiving the user's password:
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;

    // The initial email of the user prior to the update:
    private final String initialEmail;

    // A hashmap connecting input fields to their validation functions:
    private HashMap<EditText, Function<String, Result<Void, String>>> validationFunctions;

    // The keys for the input bundle:
    public static final String NAME_KEY = "name";
    public static final String SURNAME_KEY = "surname";
    public static final String BIRTHDATE_KEY = "birthdate";
    public static final String EMAIL_KEY = "email";
    public static final String PASSWORD_KEY = "password";

    /**
     * After validation occurred, the info given by the user needs to be given to the activity which
     * is handling input. This class provides a convenient way for the info to pass anywhere.
     * Pay attention that although any class can READ the data that is saved, no class can create an
     * UserInputFragment1.PackagedInfo object, except the UserInputFragment1 class.
     */
    public static class PackagedInfo {
        public final String NAME;
        public final String SURNAME;
        public final Timestamp BIRTHDATE;
        public final String EMAIL;
        public final String PASSWORD;

        private PackagedInfo(
                String NAME,
                String SURNAME,
                Timestamp BIRTHDATE,
                String EMAIL,
                String PASSWORD
        ) {
            this.NAME = NAME;
            this.SURNAME = SURNAME;
            this.BIRTHDATE = BIRTHDATE;
            this.EMAIL = EMAIL;
            this.PASSWORD = PASSWORD;
        }
    }

    public UserInputFragment1(@Nullable User connectedUser) {
        this.user = connectedUser;
        initialEmail = user == null ? null : user.getEmail();
    }

    private boolean areInputsEmpty() {
        boolean areInputsEmpty;
        // Check the first and last name:
        areInputsEmpty = Util.getTextFromEt(this.etName).isEmpty();
        areInputsEmpty &= Util.getTextFromEt(this.etSurname).isEmpty();

        // Check the birthdate:
        areInputsEmpty &= this.birthdate == null;

        // Check the email:
        areInputsEmpty &= Util.getTextFromEt(this.etEmail).isEmpty();

        // Check the password and return the result:
        return areInputsEmpty && Util.getTextFromEt(this.etPassword).isEmpty();
    }

    private void loadInputsFromUser(User user) {
        // Check that all inputs are empty:
        if (this.areInputsEmpty()) {
            // Load all input fields from the user:
            this.etName.setText(user.getName());

            this.etSurname.setText(user.getSurname());

            this.birthdate = user.getBirthdate();
            setBirthdateEtFromDate(user.getBirthdate());

            this.etEmail.setText(user.getEmail());

            this.etPassword.setText(user.getPassword());

            // Clear all errors:
            this.clearErrors();
        }
    }

    private void clearErrors() {
        this.tilName.setError(null);
        this.tilSurname.setError(null);
        this.tilBirthdate.setError(null);
        this.tilEmail.setError(null);
        this.tilPassword.setError(null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the first registration fragment:
        final View parent = inflater.inflate(R.layout.fragment_user_input_1, container, false);

        // Initialize the database:
        this.db = OnlineDatabase.getInstance();

        // Initialize the edit texts and input layouts for them:
        this.initEditTexts(parent);
        this.initInputLayouts(parent);

        // Add the text watchers to the input fields:
        this.loadInputFieldsTextWatchers();

        // Initialize the custom focus changing:
        this.initFocusChangingListeners();

        // Check if a user was given, and if so load the info from them:
        if (this.user != null)
            this.loadInputsFromUser(this.user);

        this.clearErrors();
        return parent;
    }

    private void initFocusChangingListeners() {
        // For better user experience, once the user clicks next on their keyboard when in the 'Last
        // Name' input field, the birthdate dialog will be activated:
        this.etSurname.setOnEditorActionListener(
                (textView, actionID, keyEvent) -> {
                    // If the user pressed 'next':
                    if (actionID == EditorInfo.IME_ACTION_NEXT) {
                        // Activate the birthdate dialog:
                        activateBirthdateDialog();
                        return true;
                    }
                    return false;
                }
        );

        // Activate the birthdate dialog when the birthdate dialog is focused:
        this.etBirthdate.setOnFocusChangeListener(
                (_v, hasFocus) -> {
                    if (hasFocus)
                        activateBirthdateDialog();
                }
        );

        // Activate the birthdate dialog when the birthdate input is clicked:
        this.etBirthdate.setOnClickListener(view -> activateBirthdateDialog());
    }

    private void initEditTexts(View parent) {
        // Load every input field and the country list spinner:
        this.etName = parent.findViewById(R.id.fragUserInput1EtFirstName);
        this.etSurname = parent.findViewById(R.id.fragUserInput1EtLastName);

        this.etBirthdate = parent.findViewById(R.id.fragUserInput1EtBirthdate);

        this.etEmail = parent.findViewById(R.id.fragUserInput1EtEmail);

        this.etPassword = parent.findViewById(R.id.fragUserInput1EtPassword);
    }

    private void initInputLayouts(View parent) {
        // Load the input layouts of every input field:
        this.tilName = parent.findViewById(R.id.fragUserInput1TilFirstName);
        this.tilSurname = parent.findViewById(R.id.fragUserInput1TilLastName);

        this.tilBirthdate = parent.findViewById(R.id.fragUserInput1TilBirthdate);

        this.tilEmail = parent.findViewById(R.id.fragUserInput1TilEmail);

        this.tilPassword = parent.findViewById(R.id.fragUserInput1TilPassword);
    }

    private void clearFocus() {
        final View focusedView = requireActivity().getCurrentFocus();
        if (focusedView != null)
            focusedView.clearFocus();
    }

    private EditText[] getEditTexts() {
        return new EditText[] {
                this.etName, this.etSurname, this.etEmail, this.etPassword
        };
    }

    private TextInputLayout[] getInputLayouts() {
        return new TextInputLayout[] {
                this.tilName, this.tilSurname, this.tilEmail, this.tilPassword
        };
    }

    private void loadInputFieldsTextWatchers() {
        // Map each input field to its validation method:
        this.validationFunctions = new HashMap<>();
        this.validationFunctions.put(this.etName, InputValidation::validateFirstName);
        this.validationFunctions.put(this.etSurname, InputValidation::validateLastName);
        this.validationFunctions.put(this.etEmail, email -> {
            // If the user has not validated their email address, don't let them change it at
            // all:
            if (initialEmail != null && !initialEmail.equals(email) && !db.isConnectedUserEmailVerified())
                return Result.failure("Cannot change unverified email");

            // Resume normal syntax validation:
            return InputValidation.validateEmail(email);
        });
        this.validationFunctions.put(this.etPassword, InputValidation::validatePassword);

        // List the input fields that need a text watcher:
        final EditText[] fields = this.getEditTexts();
        final TextInputLayout[] layouts = this.getInputLayouts();

        // Add the improved text watcher to each of the input fields and their layouts:
        for (int index = 0; index < fields.length; index++) {
            final int i = index;
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
    }

    private void activateBirthdateDialog() {
        // Clear focus from other views:
        clearFocus();

        // Close the keyboard if it was opened:
        Util.closeKeyboard(requireContext(), etBirthdate);

        // Creating a calendar for the user to pick a date:
        final Calendar calendar = Calendar.getInstance();

        // If a date was already set, set it in the dialog as well:
        int DAY, MONTH, YEAR;
        if (this.birthdate == null) {
            DAY = calendar.get(Calendar.DAY_OF_MONTH);
            MONTH = calendar.get(Calendar.MONTH);
            YEAR = calendar.get(Calendar.YEAR);
        }
        else {
            final ZonedDateTime dateTime = birthdate.toInstant().atZone(ZoneId.systemDefault());
            DAY = dateTime.getDayOfMonth();
            MONTH = dateTime.getMonthValue() - 1; // 1 - 12 so subtract 1
            YEAR = dateTime.getYear();
        }

        // Creating the DatePicker object:
        final DatePickerDialog dateDialog = new DatePickerDialog(
                requireContext(), new BirthdateDialogManager(), YEAR, MONTH, DAY
        );

        // Limiting the minimum age according to the User class:
        calendar.set(Calendar.YEAR, LocalDate.now().getYear() - User.getMinAge());
        dateDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        // Limiting the maximum age according to the User class:
        calendar.set(Calendar.YEAR, LocalDate.now().getYear() - User.getMaxAge());
        dateDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        // Showing the dialog:
        dateDialog.show();
    }

    @Override
    public boolean validateAndSetError() {
        // Check all the normal input fields:
        final EditText[] fields = this.getEditTexts();
        final TextInputLayout[] layouts = this.getInputLayouts();

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

        // Check that a birthdate was selected:
        final boolean birthdateSelected = this.birthdate != null;
        areInputsValid &= birthdateSelected;
        this.tilBirthdate.setError(birthdateSelected ? null : Constants.MANDATORY_INPUT_ERROR);

        return areInputsValid;
    }

    @Override
    public Bundle getInputs() {
        // Create a new bundle:
        final Bundle inputBundle = new Bundle();

        // Put the inputs inside of it:
        inputBundle.putString(NAME_KEY, Util.fixNamingCapitalization(Util.getTextFromEt(this.etName)));
        inputBundle.putString(SURNAME_KEY, Util.fixNamingCapitalization(Util.getTextFromEt(this.etSurname)));
        inputBundle.putSerializable(BIRTHDATE_KEY, this.birthdate);
        inputBundle.putString(EMAIL_KEY, Util.getTextFromEt(this.etEmail));
        inputBundle.putString(PASSWORD_KEY, Util.getTextFromEt(this.etPassword));

        // Return the bundle:
        return inputBundle;
    }

    /**
     * Packs all the information given by the user into a 'Register1Fragment.PackagedInfo' object,
     * through which any activity can use the information easily and conveniently.
     * @return A 'Register1Fragment.PackagedInfo' object containing all the information received
     *         from the user.
     */
    public UserInputFragment1.PackagedInfo getPackagedInfo() {
        return new PackagedInfo(
                Util.fixNamingCapitalization(Util.getTextFromEt(this.etName)),
                Util.fixNamingCapitalization(Util.getTextFromEt(this.etSurname)),
                new Timestamp(this.birthdate),
                Util.getTextFromEt(this.etEmail),
                Util.getTextFromEt(this.etPassword)
        );
    }

    private class BirthdateDialogManager implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            // Create a timestamp from the date (the month is 0-based already):
            birthdate = Util.getDateFromInts(year, month, day);

            // Set the text of the birthdate input and remove its error (if there was one):
            setBirthdateEtFromDate(birthdate);

            // Request focus for the next input field:
            Util.openKeyboard(requireContext(), etEmail);
        }
    }

    private void setBirthdateEtFromDate(Date date) {
        // Set the text of the birthdate input and remove its error (if there was one):
        final String formattedDate = Constants.DATE_FORMAT.format(date);
        etBirthdate.setText(formattedDate);
        tilBirthdate.setError(null);
    }
}
