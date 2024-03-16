package com.example.finalproject.adapters.online;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.activities.BranchActivity;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.database.online.collections.Workplace;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

public class OnlineWorkplacesAdapter extends OnlineAdapter<Workplace, OnlineWorkplacesAdapter.WorkplaceVH> {
    // The user whose workplaces are shown:
    private final User user;

    // Tag for debugging purposes:
    private static final String TAG = "OnlineWorkplacesAdapter";

    public OnlineWorkplacesAdapter(User user, Context context, Runnable onEmptyCallback, Runnable onNotEmptyCallback, @NonNull FirestoreRecyclerOptions<Workplace> options) {
        super(context, onEmptyCallback, onNotEmptyCallback, options);
        this.user = user;
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

    public class WorkplaceVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        // A reference to the online database:
        private final FirebaseFirestore db;

        // The views in the item:
        private final TextView tvCompanyName, tvStatus, tvAddress, tvActive;
        private final ProgressBar pbLoading;
        private final Button btnShiftsHistory;

        public WorkplaceVH(@NonNull View itemView) {
            super(itemView);

            // Load the database reference:
            this.db = FirebaseFirestore.getInstance();

            // Load the views:
            this.tvCompanyName = itemView.findViewById(R.id.rowWorkplaceTvName);
            this.tvStatus = itemView.findViewById(R.id.rowWorkplaceTvStatus);
            this.tvAddress = itemView.findViewById(R.id.rowWorkplaceTvAddress);
            this.tvActive = itemView.findViewById(R.id.rowWorkplaceTvActive);
            this.btnShiftsHistory = itemView.findViewById(R.id.rowWorkplaceBtnShiftsHistory);
            this.pbLoading = itemView.findViewById(R.id.rowWorkplacePbLoading);

            // Set the on click listener of the entire row:
            this.itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view != this.itemView)
                return;

            // Get the current workplace:
            final int index = getAbsoluteAdapterPosition();
            if (index < 0 || index >= getItemCount())
                return;
            final Workplace workplace = getItem(index);

            // Get the branch of the current workplace:
            this.setLoading(true);
            this.db.collection("branches").document(workplace.getBranchId()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        // Convert to branch (if the document doesn't exist we'll get null):
                        final Branch branch = documentSnapshot.toObject(Branch.class);
                        if (branch == null) {
                            Log.e(TAG, "Workplace's document does not exist as a branch");
                            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            // Stop loading:
                            this.setLoading(false);

                            // Send the branch and user to the branch activity:
                            final Intent intent = new Intent(context, BranchActivity.class);
                            intent.putExtra("user", user);
                            intent.putExtra("branch", branch);

                            // Go to the activity:
                            context.startActivity(intent);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load branch", e);
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                        this.setLoading(false);
                    });
        }

        private void setLoading(boolean loading) {
            this.pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            this.btnShiftsHistory.setVisibility(loading ? View.GONE : View.VISIBLE);
        }
    }
}
