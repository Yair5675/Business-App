package com.example.finalproject.custom_views;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.adapters.EmployeeViewsAdapter;
import com.example.finalproject.database.online.collections.Employee;

import java.util.ArrayList;
import java.util.List;

public class RoleColumnView extends LinearLayout {
    // The context of the view:
    private final Context context;

    // The name of the role of the current column:
    private String roleName;

    // The text view that holds the role name:
    private final TextView tvRoleName;

    // The adapter of the recyclerView:
    private EmployeeViewsAdapter adapter;

    // The recycler view that shows the employee views:
    private final RecyclerView rvEmployees;

    // A list of the employee views in the column:
    private final ArrayList<Employee> employees;

    public RoleColumnView(Context context) {
        super(context);

        // Inflate the XML file:
        inflate(context, R.layout.role_column_view, this);

        // Save the context:
        this.context = context;

        // Load the views:
        this.tvRoleName = findViewById(R.id.roleColumnViewTvRoleName);
        this.rvEmployees = findViewById(R.id.roleColumnViewRvEmployees);

        // Initialize the employee list:
        this.employees = new ArrayList<>();

        // Initialize the adapter:
        this.adapter = new EmployeeViewsAdapter(
                // Remove the employee if it is clicked:
                R.color.white, context, this.employees, (view, employee) -> removeEmployee(employee)
        );

        // Set the adapter and a linear layout manager:
        rvEmployees.setAdapter(this.adapter);
        rvEmployees.setLayoutManager(new LinearLayoutManager(context));
    }

    public void addEmployee(Employee employee) {
        // Add a new employee to the adapter:
        this.adapter.addEmployee(employee);
    }

    public void setEditable(boolean isEditable) {
        // Change the onEmployeeClicked listener according to the isEditable variable:
        this.adapter = new EmployeeViewsAdapter(
                R.color.white, this.context, this.employees,
                isEditable ? (view, employee) -> removeEmployee(employee) : null
        );
        this.rvEmployees.setAdapter(this.adapter);
    }

    public boolean containsEmployee(Employee employee) {
        for (Employee e : this.employees)
            if (e.equals(employee))
                return true;
        return false;
    }

    public void removeEmployee(Employee employee) {
        // Get the index of the employee view:
        final int index = this.employees.indexOf(employee);
        if (index >= 0 && index < this.employees.size())
            this.adapter.removeEmployee(index);
    }

    public String getRole() {
        return this.roleName;
    }

    public void setRole(String roleName) {
        // Save the role:
        this.roleName = roleName;

        // Set the role name:
        this.tvRoleName.setText(this.roleName);
    }

    public List<Employee> getEmployees() {
        return this.employees;
    }
}
