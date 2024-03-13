package com.example.finalproject.adapters.online;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.database.online.collections.Workplace;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class OnlineWorkplacesAdapter extends OnlineAdapter<Workplace, OnlineWorkplacesAdapter.WorkplaceVH> {
    public OnlineWorkplacesAdapter(Context context, Runnable onEmptyCallback, Runnable onNotEmptyCallback, @NonNull FirestoreRecyclerOptions<Workplace> options) {
        super(context, onEmptyCallback, onNotEmptyCallback, options);
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkplaceVH holder, int position, @NonNull Workplace workplace) {
        // Set the company name:
        holder.tvCompanyName.setText(workplace.getCompanyName());

        // Set the employee status:
        if (workplace.isManager())
            holder.tvStatus.setText(R.string.row_workplace_manager_status);
        else
            holder.tvStatus.setText(R.string.row_workplace_employee_status);

        // Set the address:
        final String fullAddress = String.format("%s %s %s", workplace.getCountry(), workplace.getCity(), workplace.getAddress());
        holder.tvAddress.setText(fullAddress);

        // Set the activeness of the workplace:
        final @StringRes int activeRes;
        final @DrawableRes int iconRes;
        if (workplace.isActive()) {
            activeRes = R.string.row_workplace_active_txt;
            iconRes = R.drawable.green_circle;
        }
        else {
            activeRes = R.string.row_workplace_inactive_txt;
            iconRes = R.drawable.red_circle;
        }
        holder.tvActive.setText(activeRes);
        holder.tvActive.setCompoundDrawablesWithIntrinsicBounds(0, iconRes, 0, 0);

        // TODO: Make the "Shifts History" button go to the shifts history activity once the activity
        //  is made
    }

    @NonNull
    @Override
    public OnlineWorkplacesAdapter.WorkplaceVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the XML file:
        final View itemView = LayoutInflater.from(this.context).inflate(R.layout.row_workplace, parent, false);
        return new WorkplaceVH(itemView);
    }

    public static class WorkplaceVH extends RecyclerView.ViewHolder {
        // The views in the item:
        private final TextView tvCompanyName, tvStatus, tvAddress, tvActive;
        private final Button btnShiftsHistory;

        public WorkplaceVH(@NonNull View itemView) {
            super(itemView);

            // Load the views:
            this.tvCompanyName = itemView.findViewById(R.id.rowWorkplaceTvName);
            this.tvStatus = itemView.findViewById(R.id.rowWorkplaceTvStatus);
            this.tvAddress = itemView.findViewById(R.id.rowWorkplaceTvAddress);
            this.tvActive = itemView.findViewById(R.id.rowWorkplaceTvActive);
            this.btnShiftsHistory = itemView.findViewById(R.id.rowWorkplaceBtnShiftsHistory);
        }
    }
}
