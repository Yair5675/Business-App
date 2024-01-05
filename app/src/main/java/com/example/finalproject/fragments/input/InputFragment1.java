package com.example.finalproject.fragments.input;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalproject.R;
import com.example.finalproject.database.AppDatabase;
import com.example.finalproject.database.entities.City;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.ImprovedTextWatcher;
import com.example.finalproject.util.InputValidation;
import com.example.finalproject.util.Result;
import com.example.finalproject.database.entities.User;
import com.example.finalproject.util.Util;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.function.Function;

public class InputFragment1 extends Fragment {
    // A reference to the app database:
    private AppDatabase db;

    // The input fields responsible for getting the name of the user:
    private TextInputLayout tilName, tilSurname;
    private TextInputEditText etName, etSurname;

    // The radio group responsible for getting the user's gender:
    private RadioGroup rgGender;

    // The input field responsible for getting the user's birth date:
    private TextInputLayout tilBirthdate;
    private TextInputEditText etBirthdate;

    // The actual birthdate given by the user:
    private LocalDate birthdate;

    // The input field responsible for getting the user's address:
    private TextInputLayout tilAddress;
    private TextInputEditText etAddress;

    // The auto-complete input field for the city of the user:
    private TextInputLayout tilCity;
    private AutoCompleteTextView acTvCity;

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
        public final String GENDER;
        public final LocalDate BIRTHDATE;
        public final String ADDRESS;
        public final String CITY;

        private PackagedInfo(
                String NAME,
                String SURNAME,
                String GENDER,
                LocalDate BIRTHDATE,
                String ADDRESS,
                String CITY
        ) {
            this.NAME = NAME;
            this.SURNAME = SURNAME;
            this.GENDER = GENDER;
            this.BIRTHDATE = BIRTHDATE;
            this.ADDRESS = ADDRESS;
            this.CITY = CITY;
        }
    }

    private boolean areInputsEmpty() {
        boolean areInputsEmpty;
        // Check the first and last name:
        areInputsEmpty = Util.getTextFromEt(this.etName).isEmpty();
        areInputsEmpty &= Util.getTextFromEt(this.etSurname).isEmpty();

        // Check the gender:
        areInputsEmpty &= this.getSelectedGender().isEmpty();

        // Check the address:
        areInputsEmpty &= Util.getTextFromEt(this.etAddress).isEmpty();

        // Check the birthdate:
        areInputsEmpty &= this.birthdate == null;

        // Check the city and return the result:
        return areInputsEmpty && Util.getTextFromEt(this.acTvCity).isEmpty();
    }

    private void loadInputsFromUser(User user) {
        // Check that all inputs are empty:
        if (this.areInputsEmpty()) {
            // Load all input fields from the user:
            this.etName.setText(user.getName());

            this.etSurname.setText(user.getSurname());

            if (user.getGender().equals("Male"))
                this.rgGender.check(R.id.fragInput1RbMale);
            else if (user.getGender().equals("Female"))
                this.rgGender.check(R.id.fragInput1RbFemale);

            this.birthdate = user.getBirthdate();
            setBirthdateEtFromDate(user.getBirthdate());

            this.etAddress.setText(user.getAddress());

            this.acTvCity.setText(user.getCityName(requireContext()));

            // Clear all errors:
            this.tilName.setError(null);
            this.tilSurname.setError(null);
            this.tilCity.setError(null);
            this.tilAddress.setError(null);
            this.tilBirthdate.setError(null);
        }
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

        // Add the text watchers to the input fields:
        this.loadInputFieldsTextWatchers();

        // Initialize the auto-completion for the city input:
        this.initCityAutoCompletion();

        // Initialize the custom focus changing:
        this.initFocusChangingListeners();

        // If a user is logged in, load the info from them:
        if (AppDatabase.isUserLoggedIn())
            this.loadInputsFromUser(AppDatabase.getConnectedUser());

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
        // Load every input field and the gender radio group:
        this.etName = parent.findViewById(R.id.fragInput1EtFirstName);
        this.etSurname = parent.findViewById(R.id.fragInput1EtLastName);

        this.rgGender = parent.findViewById(R.id.fragInput1RgGender);

        this.etBirthdate = parent.findViewById(R.id.fragInput1EtBirthdate);

        this.etAddress = parent.findViewById(R.id.fragInput1EtAddress);

        this.acTvCity = parent.findViewById(R.id.fragInput1AcTvCity);
    }

    private void initInputLayouts(View parent) {
        // Load the input layouts of every input field:
        this.tilName = parent.findViewById(R.id.fragInput1TilFirstName);
        this.tilSurname = parent.findViewById(R.id.fragInput1TilLastName);

        this.tilBirthdate = parent.findViewById(R.id.fragInput1TilBirthdate);

        this.tilAddress = parent.findViewById(R.id.fragInput1TilAddress);

        this.tilCity = parent.findViewById(R.id.fragInput1TilCity);
    }

    private void clearFocus() {
        final View focusedView = requireActivity().getCurrentFocus();
        if (focusedView != null)
            focusedView.clearFocus();
    }

    private void loadInputFieldsTextWatchers() {
        // Map each input field to its validation method:
        this.validationFunctions = new HashMap<>();
        this.validationFunctions.put(this.etName, InputValidation::validateFirstName);
        this.validationFunctions.put(this.etSurname, InputValidation::validateLastName);
        this.validationFunctions.put(this.etAddress, InputValidation::validateAddress);
        this.validationFunctions.put(this.acTvCity, this::validateCity);

        // List the input fields that need a text watcher:
        final EditText[] fields = {
                this.etName, this.etSurname, this.etAddress, this.acTvCity
        };
        final TextInputLayout[] layouts = {
                this.tilName, this.tilSurname, this.tilAddress, this.tilCity
        };

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

    private Result<Void, String> validateCity(String city) {
        // If it's an empty string:
        if (city.isEmpty())
            return Result.failure(Constants.MANDATORY_INPUT_ERROR);

        // Check if the city name is valid (without considering the case):
        City foundCity = this.db.cityDao().getCityByNameIgnoreCase(city);
        if (foundCity != null)
            return Result.success(null);
        else
            return Result.failure("Invalid city");
    }

    private void initCityAutoCompletion() {
        // Set up the adapter and the validator:
        final ArrayAdapter<City> cityAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, this.db.cityDao().getAll()
        );
        this.acTvCity.setAdapter(cityAdapter);
    }

    private String getSelectedGender() {
        // Get the ID of the selected button:
        final int ID = this.rgGender.getCheckedRadioButtonId();
        if (ID == R.id.fragInput1RbMale)
            return "Male";
        else if (ID == R.id.fragInput1RbFemale)
            return "Female";
        // If the code reached here, no button was selected:
        else
            return "";
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
     * @param context The context of the activity that holds the fragment. Will be used for toast
     *                messages.
     * @return True if all inputs are valid, False otherwise.
     */
    public boolean areInputsValid(Context context) {
        // Check all the normal input fields:
        final EditText[] fields = {
                this.etName, this.etSurname, this.etAddress, this.acTvCity
        };
        final TextInputLayout[] layouts = {
                this.tilName, this.tilSurname, this.tilAddress, this.tilCity
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

        // Check that a gender was selected:
        final boolean genderSelected = this.rgGender.getCheckedRadioButtonId() != -1;
        areInputsValid &= genderSelected;
        if (!genderSelected)
            Toast.makeText(context, "Please choose your gender", Toast.LENGTH_SHORT).show();

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
        // Since the city fields isn't case sensitive, make sure to get the correct city name:
        final City city = this.db.cityDao().getCityByNameIgnoreCase(Util.getTextFromEt(this.acTvCity));
        return new PackagedInfo(
                Util.fixNamingCapitalization(Util.getTextFromEt(this.etName)),
                Util.fixNamingCapitalization(Util.getTextFromEt(this.etSurname)),
                this.getSelectedGender(),
                this.birthdate,
                Util.getTextFromEt(this.etAddress),
                city.getCityName()
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
            Util.openKeyboard(requireContext(), etAddress);
        }
    }

    private void setBirthdateEtFromDate(LocalDate date) {
        // Set the text of the birthdate input and remove its error (if there was one):
        final String formattedDate = date.format(Constants.DATE_FORMATTER);
        etBirthdate.setText(formattedDate);
        tilBirthdate.setError(null);
    }
}
