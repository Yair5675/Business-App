package com.example.finalproject.fragments.input;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalproject.R;
import com.example.finalproject.database.AppDatabase;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.ImprovedTextWatcher;
import com.example.finalproject.util.InputValidation;
import com.example.finalproject.util.Result;
import com.example.finalproject.database.entities.User;
import com.example.finalproject.util.Util;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.Function;

public class InputFragment1 extends Fragment {
    // A reference to the app database:
    private AppDatabase db;

    // The input fields responsible for getting the name of the user:
    private TextInputLayout tilName, tilSurname;
    private TextInputEditText etName, etSurname;

    // The input field responsible for getting the user's birth date:
    private TextInputLayout tilBirthdate;
    private TextInputEditText etBirthdate;

    // The actual birthdate given by the user:
    private LocalDate birthdate;

    // Input field responsible for receiving the user's email:
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;

    // The country code picker that receives the user's phone number and country:
    private TextInputLayout tilPhone;
    private TextInputEditText etPhone;
    private CountryCodePicker countryCodePicker;

    // The edit code that displays the country code digits selected:
    private TextInputEditText etCountryCode;

    // The input field responsible for receiving the user's password:
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;

    // A hashmap connecting input fields to their validation functions:
    private HashMap<EditText, Function<String, Result<Void, String>>> validationFunctions;

    /**
     * After validation occurred, the info given by the user needs to be given to the activity which
     * is handling input. This class provides a convenient way for the info to pass anywhere.
     * Pay attention that although any class can READ the data that is saved, no class can create an
     * InputFragment1.PackagedInfo object, except the InputFragment1 class.
     */
    public static class PackagedInfo {
        public final String NAME;
        public final String SURNAME;
        public final LocalDate BIRTHDATE;
        public final String EMAIL;
        public final String COUNTRY;
        public final String PHONE;
        public final String PASSWORD;

        private PackagedInfo(
                String NAME,
                String SURNAME,
                LocalDate BIRTHDATE,
                String EMAIL,
                String COUNTRY,
                String PHONE,
                String PASSWORD
        ) {
            this.NAME = NAME;
            this.SURNAME = SURNAME;
            this.BIRTHDATE = BIRTHDATE;
            this.EMAIL = EMAIL;
            this.COUNTRY = COUNTRY;
            this.PHONE = PHONE;
            this.PASSWORD = PASSWORD;
        }
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

        // Check the phone number:
        areInputsEmpty &= this.countryCodePicker.getFullNumberWithPlus().isEmpty();

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

            this.countryCodePicker.setFullNumber(user.getPhoneNumber());

            this.etPassword.setText(user.getPassword());

            // TODO: When switching to Firestore this function will have to be changed. Idea: Create
            //  an interface called "UserReactive", which will have two methods: one sets the UI
            //  when the user is not connected and the other sets the UI when they are connected
            //  (or updated)

            // Clear all errors:
            this.clearErrors();
        }
    }

    private void clearErrors() {
        this.tilName.setError(null);
        this.tilSurname.setError(null);
        this.tilBirthdate.setError(null);
        this.tilEmail.setError(null);
        this.tilPhone.setError(null);
        this.tilPassword.setError(null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the first registration fragment:
        final View parent = inflater.inflate(R.layout.fragment_input_1, container, false);

        // Initialize a pointer to the database:
        this.db = AppDatabase.getInstance(parent.getContext());

        // Initialize the edit texts and input layouts for them:
        this.initEditTexts(parent);
        this.initInputLayouts(parent);

        // Re-establish the CCP:
        this.initCountryCodePicker(parent);

        // Display the selected country code:
        this.updateDisplayedCountryCode();

        // Add the text watchers to the input fields:
        this.loadInputFieldsTextWatchers();

        // Initialize the custom focus changing:
        this.initFocusChangingListeners();

        // If a user is logged in, load the info from them:
        if (AppDatabase.isUserLoggedIn())
            this.loadInputsFromUser(AppDatabase.getConnectedUser());

        this.clearErrors();
        return parent;
    }

    private void updateDisplayedCountryCode() {
        this.etCountryCode.setText(
                String.format(
                        Locale.getDefault(),
                        "+%s",
                        this.countryCodePicker.getSelectedCountryCode()
                )
        );
    }

    private void initCountryCodePicker(View parent) {
        this.countryCodePicker = parent.findViewById(R.id.fragInput1CountryCodePicker);

        // Bind the phone number edit text to the CCP:
        this.countryCodePicker.registerCarrierNumberEditText(this.etPhone);
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
        this.etName = parent.findViewById(R.id.fragInput1EtFirstName);
        this.etSurname = parent.findViewById(R.id.fragInput1EtLastName);

        this.etBirthdate = parent.findViewById(R.id.fragInput1EtBirthdate);

        this.etEmail = parent.findViewById(R.id.fragInput1EtEmail);

        this.etPhone = parent.findViewById(R.id.fragInput1EtPhoneNumber);
        this.etCountryCode = parent.findViewById(R.id.fragInput1EtCountryCode);

        this.etPassword = parent.findViewById(R.id.fragInput1EtPassword);
    }

    private void initInputLayouts(View parent) {
        // Load the input layouts of every input field:
        this.tilName = parent.findViewById(R.id.fragInput1TilFirstName);
        this.tilSurname = parent.findViewById(R.id.fragInput1TilLastName);

        this.tilBirthdate = parent.findViewById(R.id.fragInput1TilBirthdate);

        this.tilEmail = parent.findViewById(R.id.fragInput1TilEmail);

        this.tilPhone = parent.findViewById(R.id.fragInput1TilPhoneNumber);

        this.tilPassword = parent.findViewById(R.id.fragInput1TilPassword);
    }

    private void clearFocus() {
        final View focusedView = requireActivity().getCurrentFocus();
        if (focusedView != null)
            focusedView.clearFocus();
    }

    private EditText[] getEditTexts() {
        return new EditText[] {
                this.etName, this.etSurname, this.etEmail, this.etPhone, this.etPassword
        };
    }

    private TextInputLayout[] getInputLayouts() {
        return new TextInputLayout[] {
                this.tilName, this.tilSurname, this.tilEmail, this.tilPhone, this.tilPassword
        };
    }

    private void loadInputFieldsTextWatchers() {
        // Map each input field to its validation method:
        this.validationFunctions = new HashMap<>();
        this.validationFunctions.put(this.etName, InputValidation::validateFirstName);
        this.validationFunctions.put(this.etSurname, InputValidation::validateLastName);
        this.validationFunctions.put(this.etEmail, InputValidation::validateEmail);
        this.validationFunctions.put(this.etPhone, _input -> {
            if (this.countryCodePicker.isValidFullNumber())
                return Result.success(null);
            else if (Util.getTextFromEt(this.etPhone).isEmpty())
                return Result.failure(Constants.MANDATORY_INPUT_ERROR);
            else
                return Result.failure("Invalid phone number");
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

        // Add the onValidityChanged listener for the CCP:
        this.countryCodePicker.setPhoneNumberValidityChangeListener(isValidNumber -> {
            Log.d("Given phone", this.countryCodePicker.getFullNumberWithPlus());
            if (isValidNumber) {
                this.tilPhone.setError(null);
            } else if (Util.getTextFromEt(this.etPhone).isEmpty()){
                this.tilPhone.setError(Constants.MANDATORY_INPUT_ERROR);
            } else {
                this.tilPhone.setError("Invalid phone number");
            }
        });

        // Change country code number:
        this.countryCodePicker.setOnCountryChangeListener(this::updateDisplayedCountryCode);
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
            DAY = this.birthdate.getDayOfMonth();
            MONTH = this.birthdate.getMonthValue() - 1;
            YEAR = this.birthdate.getYear();
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

    /**
     * Checks all inputs and their validity. If some inputs are invalid, the function will present
     * an error to the user (if one wasn't presented already).
     * @return True if all inputs are valid, False otherwise.
     */
    public boolean areInputsValid() {
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

        // Check that the country code picker and phone number are valid:
        areInputsValid &= this.countryCodePicker.isValidFullNumber();

        // Check that a birthdate was selected:
        final boolean birthdateSelected = this.birthdate != null;
        areInputsValid &= birthdateSelected;
        this.tilBirthdate.setError(birthdateSelected ? null : Constants.MANDATORY_INPUT_ERROR);

        return areInputsValid;
    }

    /**
     * Packs all the information given by the user into a 'Register1Fragment.PackagedInfo' object,
     * through which any activity can use the information easily and conveniently.
     * @return A 'Register1Fragment.PackagedInfo' object containing all the information received
     *         from the user.
     */
    public InputFragment1.PackagedInfo getPackagedInfo() {
        return new PackagedInfo(
                Util.fixNamingCapitalization(Util.getTextFromEt(this.etName)),
                Util.fixNamingCapitalization(Util.getTextFromEt(this.etSurname)),
                this.birthdate,
                Util.getTextFromEt(this.etEmail),
                this.countryCodePicker.getSelectedCountryName(),
                this.countryCodePicker.getFullNumberWithPlus(),
                Util.getTextFromEt(this.etPassword)
        );
    }

    private class BirthdateDialogManager implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            // Converting the date into a LocalDate object and saving it:
            final LocalDate date = LocalDate.of(year, month + 1, day);
            birthdate = date;

            // Set the text of the birthdate input and remove its error (if there was one):
            setBirthdateEtFromDate(date);

            // Request focus for the next input field:
            Util.openKeyboard(requireContext(), etEmail);
        }
    }

    private void setBirthdateEtFromDate(LocalDate date) {
        // Set the text of the birthdate input and remove its error (if there was one):
        final String formattedDate = date.format(Constants.DATE_FORMATTER);
        etBirthdate.setText(formattedDate);
        tilBirthdate.setError(null);
    }
}
