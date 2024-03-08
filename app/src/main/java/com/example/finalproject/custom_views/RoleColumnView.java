package com.example.finalproject.custom_views;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.adapters.EmployeeViewsAdapter;
import com.example.finalproject.database.online.collections.Employee;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class RoleColumnView extends LinearLayout {
    // The context of the view:
    private final Context context;

    // The name of the role of the current column:
    private String roleName;

    // The text view that holds the role name:
    private final TextView tvRoleName;

    // The recycler view that holds employees:
    private final RecyclerView rvEmployees;

    // The adapter of the recyclerView:
    private EmployeeViewsAdapter adapter;

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
        this.adapter = new EmployeeViewsAdapter(this.context, this.employees, null);

        // Set the adapter and a linear layout manager:
        this.rvEmployees.setAdapter(this.adapter);
        this.rvEmployees.setLayoutManager(new LinearLayoutManager(this.context));
    }

    public void addEmployee(Employee employee) {
        // Add a new employee to the adapter:
        this.adapter.addEmployee(employee);
    }

    public boolean containsEmployee(Employee employee) {
        for (Employee e : this.employees)
            if (e.equals(employee))
                return true;
        return false;
    }

    public void setOnEmployeeClickedListener(@Nullable BiConsumer<View, Employee> onEmployeeClickedListener) {
        this.adapter = new EmployeeViewsAdapter(this.context, this.employees, onEmployeeClickedListener);
        this.rvEmployees.setAdapter(this.adapter);
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
