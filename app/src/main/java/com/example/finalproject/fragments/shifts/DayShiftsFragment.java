package com.example.finalproject.fragments.shifts;

import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.custom_views.ShiftView;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.Employee;
import com.example.finalproject.database.online.collections.Shift;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DayShiftsFragment extends Fragment {
    // Whether the user can change this fragment's shifts or not:
    private boolean isEditable;

    // The branch whose shifts are set:
    private Branch branch;

    // The maximum amount of shifts in the day:
    private int maxShifts;

    // The date of the shift fragment:
    private LocalDate date;

    // The employee view that was selected and the employee in it:
    private @Nullable View pressedView;
    private @Nullable Employee selectedEmployee;

    // The roles available in the branch:
    private ArrayList<String> roles;

    // The list that holds the shift views:
    private List<ShiftView> shiftViews;

    // The layout that holds the shift views:
    private LinearLayout shiftsLayout;

    // The button that adds a new shift:
    private Button btnAddShift;

    // The fragment initialization parameter keys:
    private static final String DATE_KEY = "date";
    private static final String BRANCH_KEY = "branch";
    private static final String MAX_SHIFTS_KEY = "maxShifts";
    private static final String ROLES_KEY = "roles";
    private static final String IS_EDITABLE_KEY = "isEditable";


    public DayShiftsFragment() {
        // Required empty public constructor

        // Initialize shifts list:
        this.shiftViews = new LinkedList<>();
    }

    /**
     * A factory method serving as a replacement for the constructor.
     * @return A new instance of fragment DayShiftsFragment.
     */
    public static DayShiftsFragment newInstance(
            int maxShifts, LocalDate date, Branch branch, ArrayList<String> roles, boolean isEditable
    ) {
        // Create the fragment instance:
        final DayShiftsFragment fragment = new DayShiftsFragment();

        // Enter the parameters into a bundle and set it as the fragment's arguments:
        final Bundle args = new Bundle();
        args.putInt(MAX_SHIFTS_KEY, maxShifts);
        args.putSerializable(DATE_KEY, date);
        args.putSerializable(BRANCH_KEY, branch);
        args.putStringArrayList(ROLES_KEY, roles);
        args.putBoolean(IS_EDITABLE_KEY, isEditable);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the arguments:
        final Bundle args = getArguments();
        if (args != null) {
            final Serializable dateSer = args.getSerializable(DATE_KEY), branchSer = args.getSerializable(BRANCH_KEY);
            if (dateSer instanceof LocalDate)
                this.date = (LocalDate) dateSer;
            if (branchSer instanceof Branch)
                this.branch = (Branch) branchSer;
            this.maxShifts = args.getInt(MAX_SHIFTS_KEY, 3);
            this.roles = args.getStringArrayList(ROLES_KEY);
            this.isEditable = args.getBoolean(IS_EDITABLE_KEY, false);
        }
    }

    public void onEmployeeViewSelected(View pressedView, Employee employee) {
        // Clear the background of the previous employee:
        if (this.pressedView != null)
            this.pressedView.setBackgroundResource(android.R.color.background_light);

        // Save the current employee view and set their background to blue:
        this.selectedEmployee = employee;
        this.pressedView = pressedView;
        this.pressedView.setBackgroundResource(R.color.row_highlight);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment:
        final View parent = inflater.inflate(R.layout.fragment_day_shifts, container, false);

        // Load the views:
        this.btnAddShift = parent.findViewById(R.id.fragDayShiftsBtnAddShift);
        this.shiftsLayout = parent.findViewById(R.id.fragDayShiftsShiftsLayout);

        // Set an onClickListener for the button:
        this.btnAddShift.setOnClickListener(_v -> this.addShift());

        // Reload the shift views to the shift layout:
        this.setShiftViews(this.shiftViews);

        // If the fragment isn't editable, hide the add shift button:
        this.btnAddShift.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        return parent;
    }

    public void setShiftViews(List<ShiftView> shiftViews) {
        // Save the shift views and add click listeners for them:
        this.shiftViews = shiftViews;

        // Check that the views were initialized:
        if (this.shiftsLayout == null || this.btnAddShift == null)
            return;

        this.shiftsLayout.removeAllViews();
        // Add the shift views but keep the maximum amount:
        for (int i = 0; i < shiftViews.size() && i < this.maxShifts; i++) {
            this.shiftsLayout.addView(shiftViews.get(i));

            // Set the background color of the shift views:
            shiftViews.get(i).setBackgroundResource(i % 2 == 0 ? R.color.light_gray : R.color.white);
        }

        // Add click listeners for the shift views:
        if (this.isEditable)
            this.shiftViews.forEach(this::setClickListeners);

        // Prevent the user from adding more shifts if the added number is the max number:
        this.btnAddShift.setVisibility(this.canAddShifts() ? View.VISIBLE : View.GONE);
    }

    private boolean canAddShifts() {
        return this.maxShifts > this.shiftViews.size();
    }

    private void addShift() {
        // Add the new shift to the layout:
        final ShiftView newShift = new ShiftView(requireContext());
        newShift.setRoles(this.roles);
        this.shiftViews.add(newShift);
        this.shiftsLayout.addView(newShift);

        // Set margins:
        final int MARGINS = 10;
        ((LinearLayout.LayoutParams) newShift.getLayoutParams()).setMargins(0, MARGINS, 0, MARGINS);

        this.setClickListeners(newShift);

        // Refresh the shifts:
        this.refreshShifts();
    }

    private void setClickListeners(ShiftView newShift) {
        // Set on role clicked listener:
        newShift.setOnRoleClickedListener(roleColumnView -> {
            // If the user selected an employee, add them to the role:
            if (this.selectedEmployee != null && this.pressedView != null) {
                // Check if the employee appears in the shift:
                if (newShift.containsEmployee(this.selectedEmployee))
                    Toast.makeText(requireContext(), "Employee already works in that shift", Toast.LENGTH_SHORT).show();
                // If not add them:
                else
                    roleColumnView.addEmployee(this.selectedEmployee);

                // Remove the selection:
                this.pressedView.setBackgroundResource(android.R.color.background_light);
                this.pressedView = null;
                this.selectedEmployee = null;
            }
        });

        // Set time picker dialogs when clicking on the shift view's text views:
        this.initTimePickers(newShift);

        // Set long click listener:
        newShift.setOnLongClickListener(shiftView -> {
            // Delete the shift view:
            if (shiftView instanceof ShiftView)
                this.deleteShift((ShiftView) shiftView);
            return true;
        });
    }

    private void initTimePickers(ShiftView shiftView) {
        // Create the time picker dialogs:
        final TimePickerDialog startingTimeDialog = new TimePickerDialog(
                requireContext(),
                (timePicker, hour, minute) -> this.setShiftStartTime(hour, minute, shiftView),
                shiftView.getStartTime() / 60, shiftView.getStartTime() % 60, true
        );
        final TimePickerDialog endingTimeDialog = new TimePickerDialog (
                requireContext(),
                (timePicker, hour, minute) -> this.setShiftEndTime(hour, minute, shiftView),
                shiftView.getEndTime() / 60, shiftView.getEndTime() % 60, true
        );

        // Activate them when clicking on the text views in the shift view:
        shiftView.getTvStartTime().setOnClickListener(_v -> startingTimeDialog.show());
        shiftView.getTvEndTime().setOnClickListener(_v -> endingTimeDialog.show());
    }

    private void setShiftEndTime(int hour, int minute, ShiftView shiftView) {
        // Get the minimum out of the received time and the branch's closing time:
        final int endTime = Math.min(60 * hour + minute, this.branch.getClosingTime());

        // Check that the end time is after the start time:
        if (endTime <= shiftView.getStartTime()) {
            Toast.makeText(requireContext(), "Shift's end time must be after start time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the index of the shiftView in the list:
        final int shiftIdx = this.shiftViews.indexOf(shiftView);

        // If it's not in the list end the function:
        if (shiftIdx == -1) return;

        // Change the shift's time:
        shiftView.setEndTime(endTime);

        // If it's the last shift, no more changes are needed:
        if (shiftIdx != this.shiftViews.size() - 1) {
            // Remove any overlapping with next shifts, and delete them if there isn't enough time:
            int timeRemaining = endTime - this.shiftViews.get(shiftIdx + 1).getStartTime();
            for (int i = shiftIdx + 1; i < this.shiftViews.size() && timeRemaining > 0; i++) {
                final ShiftView currentShift = this.shiftViews.get(i);
                final int totalTime = currentShift.getEndTime() - currentShift.getStartTime();
                if (timeRemaining >= totalTime) {
                    this.deleteShift(currentShift);
                    timeRemaining -= totalTime;
                    i--;  // An item was removed - adjust indices
                }
                else {
                    // Set start time and update time picker:
                    currentShift.setStartTime(currentShift.getStartTime() + timeRemaining);
                    this.initTimePickers(currentShift);
                    timeRemaining = 0;
                }
            }
        }
    }

    private void setShiftStartTime(int hour, int minute, ShiftView shiftView) {
        // Get the maximum out of the received time and the branch's opening time:
        final int startTime = Math.max(60 * hour + minute, this.branch.getOpeningTime());

        // Check that the start time is before the end time:
        if (startTime >= shiftView.getEndTime()) {
            Toast.makeText(requireContext(), "Shift's start time must be before end time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the index of the shiftView in the list:
        final int shiftIdx = this.shiftViews.indexOf(shiftView);

        // If it's not in the list end the function:
        if (shiftIdx == -1) return;

        // Change the shift's time:
        shiftView.setStartTime(startTime);

        // If it's the first shift, no more changes are needed:
        if (shiftIdx != 0) {
            // Remove any overlapping with previous shifts, and delete them if there isn't enough time:
            int timeRemaining = this.shiftViews.get(shiftIdx - 1).getEndTime() - startTime;
            for (int i = shiftIdx - 1; i >= 0 && timeRemaining > 0; i--) {
                final ShiftView currentShift = this.shiftViews.get(i);
                final int totalTime = currentShift.getEndTime() - currentShift.getStartTime();
                if (timeRemaining >= totalTime) {
                    this.deleteShift(currentShift);
                    timeRemaining -= totalTime;
                }
                else {
                    // Set end time and update time picker:
                    currentShift.setEndTime(currentShift.getEndTime() - timeRemaining);
                    this.initTimePickers(currentShift);
                    timeRemaining = 0;
                }
            }
        }
    }

    private void refreshShifts() {
        // Prevent the user from adding more shifts if the added number is the max number:
        this.btnAddShift.setVisibility(this.canAddShifts() ? View.VISIBLE : View.GONE);

        // If there are no shifts, don't do anything:
        if (this.shiftViews.isEmpty())
            return;

        // Set the shifts in a way that each occupy the same time:
        final int totalTime = this.branch.getClosingTime() - this.branch.getOpeningTime();
        final int sharedTime = totalTime / this.shiftViews.size();

        // Reset all shifts' time:
        int startTime = this.branch.getOpeningTime();
        for (int i = 0; i < this.shiftViews.size(); i++) {
            // Get the current shift:
            final ShiftView currentShift = this.shiftViews.get(i);

            // Set the shift's time:
            currentShift.setStartTime(startTime);
            currentShift.setEndTime(startTime + sharedTime);
            startTime += sharedTime;

            // Set the color:
            currentShift.setBackgroundResource(i % 2 == 0 ? R.color.light_gray : R.color.white);
        }
    }

    public void deleteShift(ShiftView shiftView) {
        // Remove the shift view from the layout:
        this.shiftViews.remove(shiftView);
        this.shiftsLayout.removeView(shiftView);

        // A shift was removed so a new one can be created:
        this.btnAddShift.setVisibility(View.VISIBLE);
    }

    public List<Shift> getPackagedShifts() {
        // Form the packaged shifts list and return it:
        final List<Shift> shifts = new LinkedList<>();
        for (ShiftView shiftView : this.shiftViews) {
            shifts.addAll(shiftView.getShifts(this.date, this.branch));
        }
        return shifts;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Clear the shifts layout before the view is destroyed:
        this.shiftsLayout.removeAllViews();
    }
}