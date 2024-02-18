package com.example.finalproject.custom_views;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.database.online.StorageUtil;
import com.example.finalproject.database.online.collections.Employee;

public class EmployeeView extends LinearLayout {
    // The employee that this view shows:
    private Employee employee;

    // The context of the view:
    private final Context context;

    // The image view that holds the employee's image:
    private final ImageView imgEmployee;

    // The text view that holds the employee's name:
    private final TextView tvEmployeeName;

    public EmployeeView(Context context) {
        super(context);

        // Inflate the employee view layout:
        inflate(context, R.layout.employee_view, this);

        // Save the context:
        this.context = context;

        // Load the views:
        this.imgEmployee = findViewById(R.id.employeeViewImgUser);
        this.tvEmployeeName = findViewById(R.id.employeeViewTvName);
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        // Save the employee:
        this.employee = employee;

        // Set their name:
        this.tvEmployeeName.setText(this.employee.getFullName());

        // Set their image:
        StorageUtil.loadImgFromStorage(this.context, this.employee.getImagePath(), this.imgEmployee, R.drawable.guest);
    }
}