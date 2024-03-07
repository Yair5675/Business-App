package com.example.finalproject.custom_views;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.Employee;
import com.example.finalproject.database.online.collections.Shift;
import com.example.finalproject.util.Util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ShiftView extends LinearLayout {
    // The context of the view:
    private final Context context;

    // The layout that holds the role columns:
    private final LinearLayout rolesLayout;

    // The starting and ending time of the shift (in minutes since midnight):
    private int startTime, endTime;

    // The text views that display the starting and ending time of the shift:
    private final TextView tvStartTime, tvEndTime;

    // The list holding all role columns in the shift:
    private final ArrayList<RoleColumnView> roleColumns;

    public ShiftView(Context context) {
        super(context);

        // Inflate the XML file:
        inflate(context, R.layout.shift_view, this);

        // Save the context:
        this.context = context;

        // Load the inner views:
        this.rolesLayout = findViewById(R.id.shiftViewRolesLayout);
        this.tvStartTime = findViewById(R.id.shiftViewTvStart);
        this.tvEndTime = findViewById(R.id.shiftViewTvEnd);

        // Initialize the role columns list:
        this.roleColumns = new ArrayList<>();
    }

    public void clearRoles() {
        for (RoleColumnView roleColumn : this.roleColumns)
            this.rolesLayout.removeView(roleColumn);
        this.roleColumns.clear();
    }

    public void setOnRoleClickedListener(Consumer<RoleColumnView> onRoleClickedListener) {
        // Set the listener for all columns:
        for (RoleColumnView roleColumn : this.roleColumns)
            roleColumn.setOnClickListener(view -> {
                if (view instanceof RoleColumnView)
                    onRoleClickedListener.accept((RoleColumnView) view);
            });
    }

    public boolean containsEmployee(Employee employee) {
        for (RoleColumnView roleColumn : this.roleColumns) {
            if (roleColumn.containsEmployee(employee))
                return true;
        }
        return false;
    }

    public void setRoles(List<String> roleNames) {
        // Clear previous roles:
        this.clearRoles();

        for (String roleName : roleNames) {
            // Create a new role column:
            final RoleColumnView roleColumn = new RoleColumnView(this.context);
            roleColumn.setRole(roleName);

            // Add them to the layout:
            this.roleColumns.add(roleColumn);
            this.rolesLayout.addView(roleColumn);
        }
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public TextView getTvStartTime() {
        return tvStartTime;
    }

    public TextView getTvEndTime() {
        return tvEndTime;
    }

    public void setStartTime(int startTimeMinutes) {
        this.startTime = startTimeMinutes;
        this.tvStartTime.setText(String.format(Locale.getDefault(),
                "Starting at: %02d:%02d", startTime / 60, startTime % 60)
        );
    }

    public void setEndTime(int endTimeMinutes) {
        this.endTime = endTimeMinutes;
        this.tvEndTime.setText(String.format(Locale.getDefault(),
                "Ending at: %02d:%02d", endTime / 60, endTime % 60)
        );
    }

    public List<Shift> getShifts(LocalDate localDate, Branch branch) {
        // Initialize a list to hold all the shifts:
        final List<Shift> shifts = new ArrayList<>(this.roleColumns.size() * 2);

        // Add a shift for every employee in every role:
        for (RoleColumnView roleColumn : this.roleColumns) {
            for (Employee employee : roleColumn.getEmployees()) {
                final Shift shift = new Shift(
                        employee.getUid(), branch.getBranchId(), employee.getFullName(),
                        branch.getCompanyName(), roleColumn.getRole(),
                        Util.getDateFromLocalDate(localDate), this.startTime, this.endTime
                );
                shifts.add(shift);
            }
        }

        return shifts;
    }
}
