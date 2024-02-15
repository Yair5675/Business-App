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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.database.online.CloudFunctionsHandler;
import com.example.finalproject.database.online.StorageUtil;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.database.online.collections.notifications.EmployeeActionNotification;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class OnlineApplicationsAdapter extends OnlineAdapter<EmployeeActionNotification, OnlineApplicationsAdapter.ApplicationVH> {
    // The current branch that the applications are referring to:
    private final Branch currentBranch;

    // A reference to firestore database:
    private final FirebaseFirestore dbRef;

    // A reference to the cloud functions handler:
    private final CloudFunctionsHandler functionsHandler;

    // Tag for debugging purposes:
    private static final String TAG = "OnlineApplicationsAdapter";

    public OnlineApplicationsAdapter(
            Context context, Branch currentBranch, Runnable onEmptyCallback, Runnable onNotEmptyCallback,
            @NonNull FirestoreRecyclerOptions<EmployeeActionNotification> options
    ) {
        super(context, onEmptyCallback, onNotEmptyCallback, options);
        this.currentBranch = currentBranch;
        this.functionsHandler = CloudFunctionsHandler.getInstance();
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

        // Resolve the applications when the buttons are clicked:
        holder.btnAccept.setOnClickListener(_v -> this.resolveApplication(holder, notification, true));
        holder.btnReject.setOnClickListener(_v -> this.resolveApplication(holder, notification, false));
    }

    private void resolveApplication(ApplicationVH holder, EmployeeActionNotification notification, boolean accepted) {
        // Hide the two buttons:
        holder.btnAccept.setVisibility(View.GONE);
        holder.btnReject.setVisibility(View.GONE);

        // Show the progress bar:
        holder.pbLoading.setVisibility(View.VISIBLE);

        // Call the cloud function:
        this.functionsHandler.resolveApplication(
                notification, this.currentBranch, accepted, false,
                () -> {
                    // Alert the user:
                    String msg = String.format(
                            Locale.getDefault(),
                            "%s was successfully %s",
                            notification.getUserName(),
                            accepted ? "accepted" : "rejected"
                    );
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                },
                e -> {
                    // Show the buttons:
                    holder.btnAccept.setVisibility(View.VISIBLE);
                    holder.btnReject.setVisibility(View.VISIBLE);

                    // Hide the progress bar:
                    holder.pbLoading.setVisibility(View.GONE);

                    // Log the error and alert the user:
                    Log.e(TAG, "Couldn't resolve application", e);
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
        );
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
