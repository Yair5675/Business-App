package com.example.finalproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.database.online.StorageUtil;
import com.example.finalproject.database.online.collections.Employee;

import java.util.List;
import java.util.function.BiConsumer;

public class EmployeeViewsAdapter extends RecyclerView.Adapter<EmployeeViewsAdapter.EmployeeViewVH>{
    // The context of the adapter:
    private final Context context;

    // The list of employees that the adapter presents:
    private final List<Employee> employees;

    // The ID of the employee's name text color (if null, default color is given):
    private final @Nullable @ColorRes Integer nameTextColorRes;

    // A callback that will be activated once an employee is clicked. The callback receives the view
    // that was clicked and the employee it corresponds to:
    private final @Nullable BiConsumer<View, Employee> onEmployeeClickedListener;

    public EmployeeViewsAdapter(
            @Nullable Integer nameTextColorRes, Context context, List<Employee> employees,
            @Nullable BiConsumer<View, Employee> onEmployeeClickedListener
    ) {
        // Load parameters:
        this.context = context;
        this.employees = employees;
        this.nameTextColorRes = nameTextColorRes;
        this.onEmployeeClickedListener = onEmployeeClickedListener;
    }

    @NonNull
    @Override
    public EmployeeViewVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create the view holder:
        final View itemView = LayoutInflater.from(this.context).inflate(R.layout.employee_view, parent, false);
        return new EmployeeViewVH(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewVH holder, int position) {
        // Get the current employee:
        final Employee currentEmployee = this.employees.get(position);

        // Set the image:
        StorageUtil.loadImgFromStorage(
                this.context, currentEmployee.getImagePath(), holder.userImg, R.drawable.guest
        );

        // Set the user's name:
        holder.tvUserName.setText(currentEmployee.getFullName());

        // Set the text color if one was given:
        if (this.nameTextColorRes != null)
            holder.tvUserName.setTextColor(this.context.getColor(this.nameTextColorRes));

        // Set the on employee clicked listener (if it isn't null):
        if (this.onEmployeeClickedListener != null)
            holder.itemView.setOnClickListener(
                // Call with the current view and position:
                view -> this.onEmployeeClickedListener.accept(view, currentEmployee)
            );
    }

    public Employee getEmployee(int index) {
        return this.employees.get(index);
    }

    public void addEmployee(Employee employee) {
        this.employees.add(employee);
        this.notifyItemInserted(this.getItemCount() - 1);
    }

    public void removeEmployee(int index) {
        this.employees.remove(index);
        this.notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return this.employees.size();
    }

    public static class EmployeeViewVH extends RecyclerView.ViewHolder {
        // The image view showing the employee's picture:
        private final ImageView userImg;

        // The text view showing the user's name:
        private final TextView tvUserName;

        public EmployeeViewVH(@NonNull View itemView) {
            super(itemView);

            // Load the views:
            this.userImg = itemView.findViewById(R.id.employeeViewImgUser);
            this.tvUserName = itemView.findViewById(R.id.employeeViewTvName);
        }
    }
}
