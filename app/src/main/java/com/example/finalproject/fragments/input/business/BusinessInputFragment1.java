package com.example.finalproject.fragments.input.business;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.finalproject.R;
import com.example.finalproject.fragments.input.InputFragment;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.ImprovedTextWatcher;
import com.example.finalproject.util.InputValidation;
import com.example.finalproject.util.Result;
import com.example.finalproject.util.Util;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Locale;
import java.util.function.Function;

public class BusinessInputFragment1 extends InputFragment {
    // The views responsible for the company name:
    private TextInputLayout tilCompanyName;
    private TextInputEditText etCompanyName;

    // The views responsible for the branch password:
    private TextInputLayout tilBranchPassword;
    private TextInputEditText etBranchPassword;

    // The views responsible for the opening time:
    private TextInputLayout tilOpeningTime;
    private TextInputEditText etOpeningTime;

    // The views responsible for the closing time:
    private TextInputLayout tilClosingTime;
    private TextInputEditText etClosingTime;

    // The opening and closing time in minutes:
    private int openingTimeMinutes = -1, closingTimeMinutes = -1;

    // A hashmap connecting input fields to their validation functions:
    private HashMap<EditText, Function<String, Result<Void, String>>> validationFunctions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment's XML file:
        final View parent = inflater.inflate(R.layout.fragment_business_input_1, container, false);

        // Load the layouts and edit texts:
        this.initTextLayouts(parent);
        this.initEditTexts(parent);

        // Load default values:
        if (this.openingTimeMinutes == -1 && this.closingTimeMinutes == -1) {
            this.setOpeningTime(8, 0);
            this.setClosingTime(22, 0);
        }

        // Initialize time picker dialogs:
        this.initTimePickers();

        // Initialize the text watchers:
        this.initTextWatchers();

        return parent;
    }

    private void initTimePickers() {
        // Create the time picker dialogs:
        final TimePickerDialog openingTimeDialog = new TimePickerDialog (
                requireContext(),
                (timePicker, hour, minute) -> setOpeningTime(hour, minute),
                this.openingTimeMinutes / 60, this.openingTimeMinutes % 60, true
        );
        final TimePickerDialog closingTimeDialog = new TimePickerDialog (
                requireContext(),
                (timePicker, hour, minute) -> setClosingTime(hour, minute),
                this.closingTimeMinutes / 60, this.closingTimeMinutes % 60, true
        );

        // Activate them when clicking on the edit texts:
        this.etOpeningTime.setOnClickListener(_v -> openingTimeDialog.show());
        this.etClosingTime.setOnClickListener(_v -> closingTimeDialog.show());
    }

    private void setOpeningTime(int hour, int minute) {
        // Save the minutes:
        this.openingTimeMinutes = hour * 60 + minute;
        // Set the opening edit text:
        this.etOpeningTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
    }

    private void setClosingTime(int hour, int minute) {
        // Save the minutes:
        this.closingTimeMinutes = hour * 60 + minute;
        // Set the closing edit text:
        this.etClosingTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
    }

    private void initTextWatchers() {
        // Initialize the validation function map:
        if (this.validationFunctions == null)
            this.validationFunctions = new HashMap<>();

        // Configure the validation functions:
        this.addValidationFunctions();

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

    private void addValidationFunctions() {
        // Add validation functions for all inputs:
        this.validationFunctions.put(this.etCompanyName, InputValidation::validateFirstName);
        this.validationFunctions.put(this.etBranchPassword, InputValidation::validatePassword);
        this.validationFunctions.put(this.etOpeningTime, timePicked -> {
            if (timePicked.isEmpty())
                return Result.failure(Constants.MANDATORY_INPUT_ERROR);
            else if (this.openingTimeMinutes >= this.closingTimeMinutes)
                return Result.failure("Must be before closing time");
            else
                return Result.success(null);
        });
        this.validationFunctions.put(this.etClosingTime, timePicked -> {
            if (timePicked.isEmpty())
                return Result.failure(Constants.MANDATORY_INPUT_ERROR);
            else if (this.openingTimeMinutes >= this.closingTimeMinutes)
                return Result.failure("Must be after opening time");
            else
                return Result.success(null);
        });
    }

    private TextInputLayout[] getInputLayouts() {
        return new TextInputLayout[] {
                tilCompanyName, tilBranchPassword, tilOpeningTime, tilClosingTime
        };
    }

    private EditText[] getEditTexts() {
        return new EditText[] {
                etCompanyName, etBranchPassword, etOpeningTime, etClosingTime
        };
    }

    private void initTextLayouts(View parent) {
        this.tilCompanyName = parent.findViewById(R.id.fragBusinessInput1TilCompanyName);
        this.tilBranchPassword = parent.findViewById(R.id.fragBusinessInput1TilBranchPassword);
        this.tilOpeningTime = parent.findViewById(R.id.fragBusinessInput1TilOpeningTime);
        this.tilClosingTime = parent.findViewById(R.id.fragBusinessInput1TilClosingTime);
    }

    private void initEditTexts(View parent) {
        this.etCompanyName = parent.findViewById(R.id.fragBusinessInput1EtCompanyName);
        this.etBranchPassword = parent.findViewById(R.id.fragBusinessInput1EtBranchPassword);
        this.etOpeningTime = parent.findViewById(R.id.fragBusinessInput1EtOpeningTime);
        this.etClosingTime = parent.findViewById(R.id.fragBusinessInput1EtClosingTime);
    }

    @Override
    public boolean validateAndSetError() {
        // Get the edit texts and input layouts:
        final EditText[] fields = this.getEditTexts();
        final TextInputLayout[] layouts = this.getInputLayouts();

        // Go over each pair:
        boolean areInputsValid = true;
        Function<String, Result<Void, String>> validator;
        for (int i = 0; i < fields.length; i++) {
            // Apply validation:
            validator = this.validationFunctions.get(fields[i]);
            if (validator == null)
                continue;
            Util.validateAndSetError(layouts[i], fields[i], validator);

            // Update the flag:
            areInputsValid &= layouts[i].getError() == null;
        }

        return areInputsValid;
    }

    @Override
    public Bundle getInputs() {
        return null;
    }
}
