package com.example.finalproject.custom_views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.database.online.StorageUtil;
import com.example.finalproject.database.online.collections.Employee;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class OnlineEmployeeAdapter extends FirestoreRecyclerAdapter<Employee, OnlineEmployeeAdapter.EmployeeVH> {
    // The context of the recyclerView:
    private final Context context;

    public OnlineEmployeeAdapter(Context context, @NonNull FirestoreRecyclerOptions<Employee> options) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull EmployeeVH holder, int position, @NonNull Employee employee) {
        // Set the employee's image:
        StorageUtil.loadImgFromStorage(
                this.context, employee.getImagePath(), holder.imgEmployee, R.drawable.guest
        );

        // Set the employee's full name:
        holder.tvFullName.setText(employee.getFullName());

        // Show the crown image only if the employee is a manager:
        holder.imgCrown.setVisibility(employee.isManager() ? View.VISIBLE : View.GONE);
    }

    @NonNull
    @Override
    public EmployeeVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the row_employee.xml file:
        final View rowView = LayoutInflater.from(this.context).inflate(R.layout.row_employee, parent, false);
        return new EmployeeVH(rowView);
    }

    public static class EmployeeVH extends RecyclerView.ViewHolder {
        // The image view of the user:
        private final ImageView imgEmployee;

        // The text view holding the user's full name:
        private final TextView tvFullName;

        // The crown image view that appears if the user is a manager:
        private final ImageView imgCrown;

        public EmployeeVH(@NonNull View itemView) {
            super(itemView);

            // Load the views:
            this.imgEmployee = itemView.findViewById(R.id.rowEmployeeImage);
            this.tvFullName = itemView.findViewById(R.id.rowEmployeeTvFullName);
            this.imgCrown = itemView.findViewById(R.id.rowEmployeeImgCrown);
        }
    }
}
