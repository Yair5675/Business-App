package com.example.finalproject.custom_views.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.database.online.StorageUtil;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.database.online.collections.notifications.EmployeeActionNotification;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

public class OnlineApplicationsAdapter extends OnlineAdapter<EmployeeActionNotification, OnlineApplicationsAdapter.ApplicationVH> implements View.OnClickListener {
    // The current branch that the applications are referring to:
    private final Branch currentBranch;

    // A reference to firestore database:
    private final FirebaseFirestore dbRef;

    // Tag for debugging purposes:
    private static final String TAG = "OnlineApplicationsAdapter";

    public OnlineApplicationsAdapter(
            Context context, Branch currentBranch, Runnable onEmptyCallback, Runnable onNotEmptyCallback,
            @NonNull FirestoreRecyclerOptions<EmployeeActionNotification> options
    ) {
        super(context, onEmptyCallback, onNotEmptyCallback, options);
        this.currentBranch = currentBranch;
        this.dbRef = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ApplicationVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the XML file:
        final View rowView = LayoutInflater.from(this.context).inflate(R.layout.row_application, parent, false);
        return new ApplicationVH(rowView);
    }

    @Override
    protected void onBindViewHolder(@NonNull ApplicationVH holder, int position, @NonNull EmployeeActionNotification notification) {
        // Set the user's name:
        holder.tvUserName.setText(notification.getUserName());

        // Set the user's image:
        this.setUserImgOnHolder(holder, notification.getUid());

        // Set onClickListeners for the accept and reject buttons:
        holder.btnAccept.setOnClickListener(this);
        holder.btnReject.setOnClickListener(this);
    }

    private void setUserImgOnHolder(ApplicationVH holder, String uid) {
        // Show the progress bar until the image is loaded:
        holder.pbLoading.setVisibility(View.VISIBLE);

        // Get the image with a query:
        this.dbRef.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Get the user object:
                    final User user = documentSnapshot.toObject(User.class);

                    // Set the image in the imageView:
                    if (user != null)
                        StorageUtil.loadUserImgFromStorage(this.context, user, holder.userImg, R.drawable.guest);
                    else
                        Log.e(TAG, "Failed to convert document to User object");

                    // Hide the progress bar:
                    holder.pbLoading.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    // Log the error and hide the progress bar:
                    Log.e(TAG, "Failed to get user", e);
                    holder.pbLoading.setVisibility(View.GONE);
                });
    }

    @Override
    public void onClick(View view) {
        // TODO: Call the appropriate cloud function for each type of notification
    }

    public static class ApplicationVH extends RecyclerView.ViewHolder {
        // The imageView holding the user's image:
        private final ImageView userImg;

        // The textView holding the user's full name:
        private final TextView tvUserName;

        // The accept and reject buttons:
        private final Button btnAccept, btnReject;

        // The progress bar shown when either button is pressed:
        private final ProgressBar pbLoading;

        public ApplicationVH(@NonNull View itemView) {
            super(itemView);

            // Load the views:
            this.userImg = itemView.findViewById(R.id.rowApplicationUserImage);
            this.tvUserName = itemView.findViewById(R.id.rowApplicationTvFullName);
            this.btnAccept = itemView.findViewById(R.id.rowApplicationBtnAccept);
            this.btnReject = itemView.findViewById(R.id.rowApplicationBtnReject);
            this.pbLoading = itemView.findViewById(R.id.rowApplicationPbLoading);
        }
    }
}
