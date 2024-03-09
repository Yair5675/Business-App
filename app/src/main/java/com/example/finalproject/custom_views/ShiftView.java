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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    /**
     * Generates a map of localDates connected to shiftViews according to a list of shift objects.
     * The returned shift views' roles will be every role found in the shifts given to it.
     * @param context Context of the shift views' creator.
     * @param shifts A list of Shift objects that will be turned into a list of ShiftView objects.
     * @param employeeList A list of employees that appear in the given shifts. Since each shift
     *                     object doesn't hold enough info to recreate an employee (at least,
     *                     without another call to the database), a list of employees is needed
     *                     for adding employee views to the shift views.
     * @return A map connecting the date of the shifts to the shift views that are in that date.
     */
    public static Map<LocalDate, List<ShiftView>> getShiftViewsFromShifts(Context context, List<Shift> shifts, List<Employee> employeeList) {
        // A map between the times of the shifts and a shiftView (allows for quicker lookup):
        final Map<Integer, ShiftView> viewsMap = new HashMap<>();

        // The map that will be returned eventually:
        final Map<LocalDate, List<ShiftView>> dateShiftMap = new HashMap<>();

        // Find all roles:
        final Set<String> roles = getRoles(shifts);

        // Go over the shifts and convert them to shift views:
        for (Shift shift : shifts) {
            // Get the shiftView's key in the map:
            int mapKey = Objects.hash(shift.getShiftDate(), shift.getStartingTime(), shift.getEndingTime());

            // Add a new shiftView to the map if one wasn't there already:
            if (!viewsMap.containsKey(mapKey)) {
                // Create a new shift view with the roles, employee and times of the Shift:
                final ShiftView shiftView = new ShiftView(context);
                shiftView.setRoles(new ArrayList<>(roles));
                shiftView.setStartTime(shift.getStartingTime());
                shiftView.setEndTime(shift.getEndingTime());

                // Add to the maps:
                viewsMap.put(mapKey, shiftView);
                final LocalDate shiftDate = getLocalDateFromDate(shift.getShiftDate());
                final List<ShiftView> shiftViews = dateShiftMap.getOrDefault(shiftDate, new ArrayList<>());
                if (shiftViews != null)
                    shiftViews.add(shiftView);
                dateShiftMap.put(shiftDate, shiftViews);
            }

            // Add the employee to the shiftView:
            addEmployeeToShiftView(viewsMap.get(mapKey), shift, employeeList);
        }

        // Return the shiftViews:
        return dateShiftMap;
    }

    private static LocalDate getLocalDateFromDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static void addEmployeeToShiftView(ShiftView shiftView, Shift shift, List<Employee> employeeList) {
        // Get the employee:
        Employee employee = null;
        for (Employee e : employeeList) {
            if (shift.getUid().equals(e.getUid())) {
                employee = e;
                break;
            }
        }
        if (employee == null)
            return;

        // Add the employee to the role column:
        for (RoleColumnView roleColumn : shiftView.roleColumns) {
            if (roleColumn.getRole().equals(shift.getRoleName())) {
                roleColumn.addEmployee(employee);
                return;
            }
        }
    }

    private static Set<String> getRoles(List<Shift> shifts) {
        return shifts.stream().map(Shift::getRoleName).collect(Collectors.toSet());
    }
}
