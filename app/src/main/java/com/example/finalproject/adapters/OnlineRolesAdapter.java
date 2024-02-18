package com.example.finalproject.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

public class OnlineRolesAdapter extends OnlineAdapter<String, OnlineRolesAdapter.RoleVH>{
    // Whether the user viewing the employees is a manager or not:
    private boolean isManager;

    // The ID of the branch that the roles belong to:
    private final String branchId;

    // Tag for debugging purposes:
    private static final String TAG = "OnlineRolesAdapter";

    public OnlineRolesAdapter(
            boolean isManager, String branchId, Context context, Runnable onEmptyCallback,
            Runnable onNotEmptyCallback, @NonNull FirestoreRecyclerOptions<String> options
    ) {
        super(context, onEmptyCallback, onNotEmptyCallback, options);
        this.isManager = isManager;
        this.branchId = branchId;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setManager(boolean isManager) {
        this.isManager = isManager;
        notifyDataSetChanged();
    }

    @Override
    protected void onBindViewHolder(@NonNull RoleVH holder, int position, @NonNull String role) {
        // Set the role textView:
        holder.tvRoleName.setText(role);

        // Show the remove button only if the connected user is a manager:
        holder.imgRemove.setVisibility(this.isManager ? View.VISIBLE : View.GONE);
    }

    @NonNull
    @Override
    public RoleVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the row_role.xml file:
        final View rowView = LayoutInflater.from(this.context).inflate(R.layout.row_role, parent, false);
        return new RoleVH(rowView);
    }

    public class RoleVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        // The text view displaying the role name:
        private final TextView tvRoleName;

        // The image allowing a manager to remove a role:
        private final ImageView imgRemove;

        // The progress bar shown while a role is deleted:
        private final ProgressBar pbLoading;

        public RoleVH(@NonNull View itemView) {
            super(itemView);

            // Load the views:
            this.tvRoleName = itemView.findViewById(R.id.rowRoleTvRoleName);
            this.imgRemove = itemView.findViewById(R.id.rowRoleImgRemove);
            this.pbLoading = itemView.findViewById(R.id.rowRolePbLoading);

            // Set onClickListener for the remove image:
            this.imgRemove.setOnClickListener(this);
        }

        private void deleteRole(String role) {
            // Show the progress bar, hide the remove button:
            this.pbLoading.setVisibility(View.VISIBLE);
            this.imgRemove.setVisibility(View.GONE);

            // Get a reference to the online database:
            final FirebaseFirestore dbRef = FirebaseFirestore.getInstance();

            // Delete the role:
            dbRef
                    .collection("branches")
                    .document(branchId)
                    .collection("roles")
                    .document(role)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        // Alert the user:
                        Toast.makeText(context, "Role removed successfully", Toast.LENGTH_SHORT).show();

                        // Show the remove button, hide the progress bar:
                        this.pbLoading.setVisibility(View.GONE);
                        this.imgRemove.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(e -> {
                        // Log the error and alert the user:
                        Log.e(TAG, "Couldn't delete role", e);
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();

                        // Show the remove button, hide the progress bar:
                        this.pbLoading.setVisibility(View.GONE);
                        this.imgRemove.setVisibility(View.VISIBLE);
                    });
        }

        @Override
        public void onClick(View view) {
            // Get the ID and the index of the role that was clicked on:
            final int ID = view.getId();
            final int index = getAbsoluteAdapterPosition();
            if (index < 0 && index >= getItemCount())
                return;

            // If they pressed the remove image:
            if (ID == R.id.rowRoleImgRemove) {
                // Get the current role:
                final String role = getItem(index);
                this.deleteRole(role);
            }
        }
    }
}
