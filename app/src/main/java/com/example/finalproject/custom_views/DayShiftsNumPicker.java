package com.example.finalproject.custom_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.finalproject.R;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * A view that represents a single day in the week and allows the user to pick the number of shifts
 * in that day.
 */
public class DayShiftsNumPicker extends LinearLayout {

    // The shifts number picker:
    private NumberPicker npShiftsNum;

    // The maximum amount of shifts for a day:
    private static final int MAX_DAILY_SHIFTS = 3;

    public DayShiftsNumPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attr) {
        // Inflate the XML file:
        inflate(context, R.layout.day_shifts_num_picker, this);

        // Initialize the number picker:
        this.npShiftsNum = findViewById(R.id.dayShiftsNumPickerNpShifts);
        this.npShiftsNum.setMinValue(0);
        this.npShiftsNum.setMaxValue(MAX_DAILY_SHIFTS);
        Log.d("DayShiftsNumPicker", "init called");

        // The text view:
        final TextView tvDayOfWeek = findViewById(R.id.dayShiftsNumPickerTvDayOfWeek);

        // Get the attributes:
        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attr, R.styleable.DayShiftsNumPicker, 0 ,0
        );
        try {
            // Get the text size:
            final float textSize = typedArray.getDimension(R.styleable.DayShiftsNumPicker_android_textSize, -1);
            if (textSize != -1)
                tvDayOfWeek.setTextSize(textSize);

            // Get the number of shifts:
            final int shiftsNum = typedArray.getInt(R.styleable.DayShiftsNumPicker_shiftsNum, -1);
            if (shiftsNum != -1)
                this.npShiftsNum.setValue(shiftsNum);

            // Get the day:
            int dayIdx = typedArray.getInt(R.styleable.DayShiftsNumPicker_dayOfWeek, -1);
            if (dayIdx != -1)
                tvDayOfWeek.setText(DayOfWeek.of(dayIdx).getDisplayName(TextStyle.SHORT, Locale.US));

        } finally {
            // Release resources:
            typedArray.recycle();
        }
    }

    public int getShiftNum() {
        return this.npShiftsNum.getValue();
    }

    public void setShiftNum(int shiftNum) {
        this.npShiftsNum.setValue(shiftNum);
    }
}
