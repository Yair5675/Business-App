package com.example.finalproject.fragments.shifts;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.finalproject.R;
import com.example.finalproject.custom_views.EmployeeView;
import com.example.finalproject.custom_views.ShiftView;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.Employee;

import java.util.LinkedList;
import java.util.List;

public class DayShiftsFragment extends Fragment {
    // The branch whose shifts are set:
    private Branch branch;

    // The index of the day that the fragment sets shifts for (0 - Sunday):
    private int dayIndex;

    // The roles available in the branch:
    private List<String> roles;

    // The list that holds the shift views:
    private List<ShiftView> shiftViews;

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
            Context context, int dayIndex, Branch branch, List<Employee> employees, List<String> roles
    ) {
        // Create the fragment instance:
        final DayShiftsFragment fragment = new DayShiftsFragment();

        // Set the roles in the branch:
        fragment.roles = roles;

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
        for (EmployeeView employeeView : this.employeeViews)
            this.employeesLayout.addView(employeeView);

        // Reload the shift views to the shift layout:
        for (ShiftView shiftView : this.shiftViews)
            this.shiftsLayout.addView(shiftView);
        return parent;
    }

    private void addShift() {
        // Set the shifts in a way that each occupy the same time:
        final int totalTime = this.branch.getClosingTime() - this.branch.getOpeningTime();
        final int sharedTime = this.shiftViews.isEmpty() ? totalTime : totalTime / this.shiftViews.size();

        // Add the new shift to the layout:
        final ShiftView newShift = new ShiftView(requireContext());
        newShift.setRoles(this.roles);
        this.shiftViews.add(newShift);
        this.shiftsLayout.addView(newShift);

        // Reset all shifts' time:
        int startTime = this.branch.getOpeningTime();
        for (ShiftView shiftView : this.shiftViews) {
            shiftView.setStartTime(startTime);
            shiftView.setEndTime(startTime + sharedTime);
            startTime += sharedTime;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Clear the layouts before the view is destroyed:
        this.employeesLayout.removeAllViews();
        this.shiftsLayout.removeAllViews();
    }
}