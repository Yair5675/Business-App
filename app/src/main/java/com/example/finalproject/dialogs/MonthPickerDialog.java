package com.example.finalproject.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.finalproject.R;

import java.time.LocalDate;

public class MonthPickerDialog extends DialogFragment implements DialogInterface.OnClickListener{
    // The number pickers that allow the user to choose a month and year:
    private NumberPicker npMonth, npYear;

    // A listener that will be called once a month is selected:
    private @Nullable OnMonthSelectedListener onMonthSelectedListener;

    public interface OnMonthSelectedListener {
        /**
         * A callback method that will be called once the user has picked a year and month and
         * confirmed their selection.
         * @param year The year that the user has picked.
         * @param month The month that the user has picked. The month is 0 based, meaning January is
         *              0 and December is 11.
         */
        void onMonthSelected(int year, int month);
    }

    // TODO: Create a way to limit the selection, as in min and max month and year

    // Keys for the savedInstanceState bundle:
    private static final String MONTH_BUNDLE_KEY = "month";
    private static final String YEAR_BUNDLE_KEY = "year";

    // A constant that will be used if nothing was selected:
    private static final int NOT_SELECTED = -1;

    // A constant dictating the maximum amount of year allowed to the past/future:
    private static final int MAX_YEARS_RANGE = 50;

    public void setOnMonthSelectedListener(@Nullable OnMonthSelectedListener listener) {
        this.onMonthSelectedListener = listener;
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Get the context:
        final Context context = getContext();

        // Inflate the custom layout and load its views:
        final View parent = LayoutInflater.from(context).inflate(R.layout.dialog_month_picker, null);
        this.npMonth = parent.findViewById(R.id.dialogMonthPickerNpMonth);
        this.npYear = parent.findViewById(R.id.dialogMonthPickerNpYear);

        // Load the previous state:
        this.loadOldState(savedInstanceState);

        // Limit the month and year pickers:
        this.npMonth.setMinValue(1);
        this.npMonth.setMaxValue(12);

        final int currentYear = LocalDate.now().getYear();
        this.npYear.setMinValue(currentYear - MAX_YEARS_RANGE);
        this.npYear.setMaxValue(currentYear + MAX_YEARS_RANGE);

        // Set up the dialog:
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_month_picker_title)
                .setPositiveButton("Confirm", this)
                .setNegativeButton("Cancel", this)
                .setView(parent);

        // Return it:
        return builder.create();
    }

    private void loadOldState(@Nullable Bundle savedInstanceState) {
        // Set the month and year if they have been saved:
        if (savedInstanceState != null) {
            final int month = savedInstanceState.getInt(MONTH_BUNDLE_KEY, NOT_SELECTED);
            final int year = savedInstanceState.getInt(YEAR_BUNDLE_KEY, NOT_SELECTED);

            if (month != NOT_SELECTED)
                this.npMonth.setValue(month + 1);
            else
                this.setCurrentMonth();

            if (year != NOT_SELECTED)
                this.npYear.setValue(year);
            else
                this.setCurrentMonth();
        }
        // If the state isn't saved, set the current month and year:
        else {
            this.setCurrentMonth();
            this.setCurrentYear();
        }
    }

    private void setCurrentMonth() {
        final LocalDate now = LocalDate.now();
        this.npMonth.setValue(now.getMonthValue()); // Here the value is from 1 to 12
    }

    private void setCurrentYear() {
        final LocalDate now = LocalDate.now();
        this.npYear.setValue(now.getYear());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current month and year:
        outState.putInt(MONTH_BUNDLE_KEY, this.getMonth());
        outState.putInt(YEAR_BUNDLE_KEY, this.getYear());
    }

    /**
     * Gets the selected month from the number picker.
     * @return The month that the user selected. The returned value is 0 based, meaning 0 is January
     *         and 11 is December.
     */
    private int getMonth() {
        return this.npMonth.getValue() - 1;
    }

    private int getYear() {
        return this.npYear.getValue();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        // If it was the positive button:
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // Call the listener if it was configured:
            if (this.onMonthSelectedListener != null)
                this.onMonthSelectedListener.onMonthSelected(this.getYear(), this.getMonth());

            // Close the dialog:
            this.dismiss();
        }
        else if (which == DialogInterface.BUTTON_NEGATIVE) {
            // Dismiss the dialog:
            this.dismiss();
        }
    }
}
