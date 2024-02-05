package com.example.finalproject.custom_views;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.finalproject.R;

import java.util.Arrays;

public class WeekShiftsNumPicker extends LinearLayout {
    public static final String SHIFTS_NUM_KEY = "shiftsNum";
    public static final String SUPER_STATE_KEY = "superState";
    // An array of seven day shifts num picker:
    private final DayShiftsNumPicker[] dayShiftsNumPickers = new DayShiftsNumPicker[7];

    public WeekShiftsNumPicker(Context context) {
        super(context);
        init(context);
    }

    public WeekShiftsNumPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WeekShiftsNumPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Inflate the XML file:
        inflate(context, R.layout.week_shifts_num_picker, this);

        // Load the shift num pickers:
        this.dayShiftsNumPickers[0] = findViewById(R.id.weekShiftsNumPickerSunday);
        this.dayShiftsNumPickers[1] = findViewById(R.id.weekShiftsNumPickerMonday);
        this.dayShiftsNumPickers[2] = findViewById(R.id.weekShiftsNumPickerTuesday);
        this.dayShiftsNumPickers[3] = findViewById(R.id.weekShiftsNumPickerWednesday);
        this.dayShiftsNumPickers[4] = findViewById(R.id.weekShiftsNumPickerThursday);
        this.dayShiftsNumPickers[5] = findViewById(R.id.weekShiftsNumPickerFriday);
        this.dayShiftsNumPickers[6] = findViewById(R.id.weekShiftsNumPickerSaturday);
    }

    public int[] getShiftsNum() {
        return Arrays.stream(this.dayShiftsNumPickers)
                .mapToInt(DayShiftsNumPicker::getShiftNum)
                .toArray();
    }

    public void setShiftsNum(int[] shiftsNum) {
        if (shiftsNum.length == this.dayShiftsNumPickers.length) {
            for (int i = 0; i < this.dayShiftsNumPickers.length; i++)
                this.dayShiftsNumPickers[i].setShiftNum(shiftsNum[i]);
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        // Put the shifts in a bundle:
        final Bundle bundle = new Bundle();
        bundle.putIntArray(SHIFTS_NUM_KEY, this.getShiftsNum());

        // Save the super state and return the bundle:
        bundle.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Verify the saved state (also implicit null check):
        if (state instanceof Bundle) {
            final Bundle stateBundle = (Bundle) state;

            // Set the shifts:
            int[] shiftsNum;
            if ((shiftsNum = stateBundle.getIntArray(SHIFTS_NUM_KEY)) != null)
                this.setShiftsNum(shiftsNum);

            // Load super state:
            super.onRestoreInstanceState(stateBundle.getParcelable(SUPER_STATE_KEY));
        }
    }
}
