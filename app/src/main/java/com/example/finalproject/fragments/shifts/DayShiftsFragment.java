package com.example.finalproject.fragments.shifts;

import android.app.TimePickerDialog;
import android.content.Context;
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
import com.example.finalproject.custom_views.EmployeeView;
import com.example.finalproject.custom_views.ShiftView;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.Employee;
import com.example.finalproject.database.online.collections.Shift;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

public class DayShiftsFragment extends Fragment {
    // The branch whose shifts are set:
    private Branch branch;

    // The index of the day that the fragment sets shifts for (0 - Sunday):
    private int dayIndex;

    // The date of the shift fragment:
    private LocalDate date;

    // The employee view that was selected:
    private @Nullable EmployeeView selectedEmployee;

    // The roles available in the branch:
    private List<String> roles;

    // The list that holds the shift views:
    private final List<ShiftView> shiftViews;

    // The list that holds the employee views:
    private List<EmployeeView> employeeViews;

    // The layout that holds the employee views:
    private LinearLayout employeesLayout;

    // The layout that holds the shift views:
    private LinearLayout shiftsLayout;

    // The button that adds a new shift:
    private Button btnAddShift;

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
            Context context, int dayIndex, LocalDate date, Branch branch, List<Employee> employees, List<String> roles
    ) {
        // Create the fragment instance:
        final DayShiftsFragment fragment = new DayShiftsFragment();

        // Set the roles in the branch:
        fragment.roles = roles;

        // Set the date:
        fragment.date = date;

        // Set the branch:
        fragment.branch = branch;

        // Set day index:
        fragment.dayIndex = dayIndex;

        // Set the employee views in the fragment:
        fragment.employeeViews = createEmployeeViews(context, employees);

        return fragment;
    }

    private static List<EmployeeView> createEmployeeViews(Context context, List<Employee> employees) {
        // Create the employee views list:
        final List<EmployeeView> employeeViews = new LinkedList<>();
        for (Employee employee : employees) {
            // Create the employee view from the employee:
            final EmployeeView employeeView = new EmployeeView(context);
            employeeView.setEmployee(employee);
            employeeViews.add(employeeView);
        }

        return employeeViews;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment:
        final View parent = inflater.inflate(R.layout.fragment_day_shifts, container, false);

        // Load the views:
        this.btnAddShift = parent.findViewById(R.id.fragDayShiftsBtnAddShift);
        this.shiftsLayout = parent.findViewById(R.id.fragDayShiftsShiftsLayout);
        this.employeesLayout = parent.findViewById(R.id.fragDayShiftsEmployeesLayout);

        // Set an onClickListener for the button:
        this.btnAddShift.setOnClickListener(_v -> this.addShift());

        // Reload the employee views to the employees layout:
        for (EmployeeView employeeView : this.employeeViews) {
            this.employeesLayout.addView(employeeView);

            // Set an onClickListener to each employee view:
            employeeView.setOnClickListener(view -> {
                if (view instanceof EmployeeView) {
                    // Clear the background of the previous employee:
                    if (this.selectedEmployee != null)
                        this.selectedEmployee.setBackgroundResource(android.R.color.background_light);

                    // Save the current employee view and set their background to blue:
                    this.selectedEmployee = (EmployeeView) view;
                    this.selectedEmployee.setBackgroundResource(R.color.row_highlight);
                }
            });
        }

        // Reload the shift views to the shift layout:
        for (ShiftView shiftView : this.shiftViews)
            this.shiftsLayout.addView(shiftView);

        // Prevent the user from adding more shifts if the added number is the max number:
        this.btnAddShift.setVisibility(this.canAddShifts() ? View.VISIBLE : View.GONE);
        return parent;
    }

    private boolean canAddShifts() {
        return this.branch.getDailyShiftsNum().get(this.dayIndex) > this.shiftViews.size();
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

        // Set on role clicked listener:
        newShift.setOnRoleClickedListener(roleColumnView -> {
            // If the user selected an employee, add them to the role:
            if (this.selectedEmployee != null) {
                // Check if the employee appears in the shift:
                if (newShift.containsEmployee(this.selectedEmployee.getEmployee()))
                    Toast.makeText(requireContext(), "Employee already works in that shift", Toast.LENGTH_SHORT).show();
                // If not add them:
                else
                    roleColumnView.addEmployee(this.selectedEmployee.getEmployee());

                // Remove the selection:
                this.selectedEmployee.setBackgroundResource(android.R.color.background_light);
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

        // Refresh the shifts:
        this.refreshShifts();
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
            shifts.addAll(shiftView.getShifts(this.date));
        }
        return shifts;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Clear the layouts before the view is destroyed:
        this.employeesLayout.removeAllViews();
        this.shiftsLayout.removeAllViews();
    }
}