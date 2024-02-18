package com.example.finalproject.custom_views;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.database.online.collections.Employee;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class RoleColumnView extends LinearLayout {
    // The name of the role of the current column:
    private String roleName;

    // The text view that holds the role name:
    private final TextView tvRoleName;

    // The layout that holds the employee views:
    private final LinearLayout employeesLayout;

    // A list of the employee views in the column:
    private final HashSet<EmployeeView> employeeViews;

    public RoleColumnView(Context context) {
        super(context);

        // Inflate the XML file:
        inflate(context, R.layout.role_column_view, this);

        // Load the views:
        this.tvRoleName = findViewById(R.id.roleColumnViewTvRoleName);
        this.employeesLayout = findViewById(R.id.roleColumnViewEmployeesLayout);

        // Initialize the employee list:
        this.employeeViews = new HashSet<>();
    }

    public void addEmployeeView(EmployeeView employeeView) {
        // Add the view to the list:
        if (this.employeeViews.add(employeeView)) {
            // If it's a new employee, add it to the column:
            this.employeesLayout.addView(employeeView);

            // Set the onClickListener of the view - if it's touched it is removed:
            employeeView.setOnClickListener(view -> {
                if (view instanceof EmployeeView)
                    this.removeEmployeeView((EmployeeView) view);
            });
        }
    }

    public void removeEmployeeView(EmployeeView employeeView) {
        // Remove the view from the list:
        if (this.employeeViews.remove(employeeView))
            this.employeesLayout.removeView(employeeView);
    }

    public String getRole() {
        return this.roleName;
    }

    public void setRole(String roleName) {
        this.roleName = roleName;
    }

    public List<Employee> getEmployees() {
        final LinkedList<Employee> employees = new LinkedList<>();
        for (EmployeeView employeeView : this.employeeViews)
            employees.add(employeeView.getEmployee());
        return employees;
    }
}