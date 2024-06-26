package com.example.finalproject.adapters.online;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.activities.ShiftsHistoryActivity;
import com.example.finalproject.database.online.StorageUtil;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.Employee;
import com.example.finalproject.util.EmployeeActions;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class OnlineEmployeeAdapter extends OnlineAdapter<Employee, OnlineEmployeeAdapter.EmployeeVH> {
    // A flag indicating that the employee options menu should appear for managers:
    private boolean showEmployeeMenu;

    // The branch whose employees are displayed:
    private final Branch branch;

    // The current user's ID:
    private final String currentUserId;

    // Whether the user viewing the employees is a manager or not:
    private boolean isManager;

    // Employee actions for the menu:
    private final EmployeeActions employeeActions;

    // The context of the recyclerView:
    private final Context context;

    public OnlineEmployeeAdapter(
            boolean isManager, boolean showEmployeeMenu, String currentUserId, Branch branch,
            Context context, Runnable onEmptyCallback, Runnable onNotEmptyCallback,
            EmployeeActions employeeActions,
            @NonNull FirestoreRecyclerOptions<Employee> options
    ) {
        super(context, onEmptyCallback, onNotEmptyCallback, options);
        this.showEmployeeMenu = showEmployeeMenu;
        this.isManager = isManager;
        this.branch = branch;
        this.currentUserId = currentUserId;
        this.employeeActions = employeeActions;
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setShowEmployeeMenu(boolean showEmployeeMenu) {
        this.showEmployeeMenu = showEmployeeMenu;
        this.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setIsManager(boolean isManager) {
        this.isManager = isManager;
        this.notifyDataSetChanged();
    }

    public int getManagersCount() {
        int count = 0;
        for (int i = 0; i < this.getItemCount(); i++) {
            if (this.getItem(i).isManager())
                count++;
        }
        return count;
    }

    @Override
    protected void onBindViewHolder(@NonNull EmployeeVH holder, int position, @NonNull Employee employee) {
        // Change the background color if it's the current user:
        @ColorInt int color;
        final Resources res = this.context.getResources();
        if (employee.getUid().equals(this.currentUserId))
            color = res.getColor(R.color.row_highlight, this.context.getTheme());
        else
            color = res.getColor(R.color.sand, this.context.getTheme());
        holder.rowLayout.setBackgroundColor(color);

        // Set the employee's image:
        StorageUtil.loadImgFromStorage(
                this.context, employee.getImagePath(), holder.imgEmployee, R.drawable.guest
        );

        // Set the employee's full name:
        holder.tvFullName.setText(employee.getFullName());

        // Show the crown image only if the employee is a manager:
        holder.imgCrown.setVisibility(employee.isManager() ? View.VISIBLE : View.GONE);

        // Show the more image only if the employee is an admin and not the current user:
        final boolean isCurrentUser = this.currentUserId.equals(employee.getUid());
        holder.imgMore.setVisibility(this.branch.isActive() && this.isManager && !isCurrentUser ? View.VISIBLE : View.GONE);

        // Show the menu only if the "showEmployeeMenu" flag is true and the branch is active:
        if (this.branch.isActive() && this.isManager && this.showEmployeeMenu)
            holder.imgMore.setOnClickListener((_v) -> holder.popupMenu.show());
        else
            holder.imgMore.setOnClickListener(null);

        // Show menu items according to certain conditions:
        holder.menuItemShiftsHistory.setVisible(this.isManager);
        holder.menuItemPromote.setVisible(this.isManager && !employee.isManager());
        holder.menuItemDemote.setVisible(this.isManager && employee.isManager());
        holder.menuItemFire.setVisible(this.isManager);

        // If the manager pressed on the shifts history menu item, go to the history activity:
        holder.menuItemShiftsHistory.setOnMenuItemClickListener(item -> {
            ShiftsHistoryActivity.startActivity(this.context, employee.getUid(), this.branch.getBranchId());
            return true;
        });
    }

    @NonNull
    @Override
    public EmployeeVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the row_employee.xml file:
        final View rowView = LayoutInflater.from(this.context).inflate(R.layout.row_employee, parent, false);
        return new EmployeeVH(rowView);
    }

    public class EmployeeVH extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {
        // The popup menu that will appear when an employee is clicked:
        private final PopupMenu popupMenu;

        // The layout of the row:
        private final LinearLayout rowLayout;

        // The image view of the user:
        private final ImageView imgEmployee;

        // The text view holding the user's full name:
        private final TextView tvFullName;

        // The crown image view that appears if the user is a manager:
        private final ImageView imgCrown;

        // The "more options" image:
        private final ImageView imgMore;

        // The menu items:
        private final MenuItem menuItemShiftsHistory, menuItemPromote, menuItemDemote, menuItemFire;

        public EmployeeVH(@NonNull View itemView) {
            super(itemView);

            // Load the views:
            this.rowLayout = itemView.findViewById(R.id.rowEmployeeMainLayout);
            this.imgEmployee = itemView.findViewById(R.id.rowEmployeeImage);
            this.tvFullName = itemView.findViewById(R.id.rowEmployeeTvFullName);
            this.imgCrown = itemView.findViewById(R.id.rowEmployeeImgCrown);
            this.imgMore = itemView.findViewById(R.id.rowEmployeeImgMore);

            // Create popup menu:
            this.popupMenu = new PopupMenu(context, this.imgMore, Gravity.END);
            popupMenu.inflate(R.menu.employee_menu);
            popupMenu.setOnMenuItemClickListener(this);

            // Load the menu items:
            final Menu menu = popupMenu.getMenu();
            this.menuItemShiftsHistory = menu.findItem(R.id.menuEmployeeItemShiftsHistory);
            this.menuItemPromote = menu.findItem(R.id.menuEmployeeItemPromote);
            this.menuItemDemote = menu.findItem(R.id.menuEmployeeItemDemote);
            this.menuItemFire = menu.findItem(R.id.menuEmployeeItemFire);

            // Activate it when the more image is pressed:
            this.imgMore.setOnClickListener((_v) -> popupMenu.show());
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            // Get the item's ID:
            final int ID = menuItem.getItemId();

            // Get the current employee:
            final Employee employee = getItem(getAbsoluteAdapterPosition());

            // Activate the employee actions:
            if (ID == R.id.menuEmployeeItemPromote) {
                employeeActions.promote(employee);
                return true;
            }
            else if (ID == R.id.menuEmployeeItemDemote) {
                employeeActions.demote(employee);
                return true;
            }
            else if (ID == R.id.menuEmployeeItemFire) {
                employeeActions.fire(employee);
                return true;
            }

            else
                return false;
        }
    }
}
