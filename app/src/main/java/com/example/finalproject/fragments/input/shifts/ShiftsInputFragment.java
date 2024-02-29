package com.example.finalproject.fragments.input.shifts;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.adapters.EmployeeViewsAdapter;
import com.example.finalproject.database.online.collections.Employee;
import com.example.finalproject.fragments.input.InputFragment;

import java.util.List;

public class ShiftsInputFragment extends InputFragment {
    // The employee that was selected, can be null if no one was selected:
    private @Nullable Employee selectedEmployee;
    private @Nullable View selectedEmployeeView;

    // The list of all employees in the branch:
    private List<Employee> branchEmployees;

    // The recycler view that shows the employees:
    private RecyclerView rvEmployees;

    // The adapter of the employees' recycler view;
    private EmployeeViewsAdapter employeesAdapter;

    // The recycler view that shows the shift views:
    private RecyclerView rvShift;

    public ShiftsInputFragment() {
    }

    public static ShiftsInputFragment newInstance(List<Employee> branchEmployees) {
        final ShiftsInputFragment fragment = new ShiftsInputFragment();
        fragment.branchEmployees = branchEmployees;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the XML file:
        final View parent = inflater.inflate(R.layout.fragment_shifts_input, container, false);

        // Get the context:
        final Context context = requireContext();

        // Load the employees recycler view and set its orientation to horizontal:
        this.rvEmployees = parent.findViewById(R.id.fragShiftsInputRvEmployees);
        this.rvEmployees.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        // Initialize the employees adapter:
        initEmployeesAdapter(context);
        return parent;
    }

    private void initEmployeesAdapter(Context context) {
        this.employeesAdapter = new EmployeeViewsAdapter(
                context,this.branchEmployees,
                (employeeView, index) -> {
                    // Change background color:
                    if (this.selectedEmployeeView != null)
                        this.selectedEmployeeView.setBackgroundResource(android.R.color.background_light);
                    employeeView.setBackgroundResource(R.color.row_highlight);

                    // Get the employee that was selected:
                    this.selectedEmployee = this.employeesAdapter.getEmployee(index);
                    this.selectedEmployeeView = employeeView;
                }
        );
        this.rvEmployees.setAdapter(this.employeesAdapter);
    }

    @Override
    public boolean validateAndSetError() {
        // TODO: Validate shifts
        return false;
    }

    @Override
    public Bundle getInputs() {
        // TODO: Return a bundle with a shift and a list of workers
        return null;
    }
}
