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
import com.example.finalproject.database.online.collections.Application;
import com.example.finalproject.database.online.collections.Branch;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.Locale;

public class OnlineApplicationsAdapter extends OnlineAdapter<Application, OnlineApplicationsAdapter.ApplicationVH> {
    // The current branch that the applications are referring to:
    private Branch currentBranch;

    // A reference to the cloud functions handler:
    private final CloudFunctionsHandler functionsHandler;

    // Tag for debugging purposes:
    private static final String TAG = "OnlineApplicationsAdapter";

    public OnlineApplicationsAdapter(
            Context context, Branch currentBranch, Runnable onEmptyCallback, Runnable onNotEmptyCallback,
            @NonNull FirestoreRecyclerOptions<Application> options
    ) {
        super(context, onEmptyCallback, onNotEmptyCallback, options);
        this.currentBranch = currentBranch;
        this.functionsHandler = CloudFunctionsHandler.getInstance();
    }

    public void setCurrentBranch(Branch currentBranch) {
        this.currentBranch = currentBranch;
    }

    @NonNull
    @Override
    public ApplicationVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the XML file:
        final View rowView = LayoutInflater.from(this.context).inflate(R.layout.row_application, parent, false);
        return new ApplicationVH(rowView);
    }

    @Override
    protected void onBindViewHolder(@NonNull ApplicationVH holder, int position, @NonNull Application application) {
        // Set the user's name:
        holder.tvUserName.setText(application.getUserFullName());

        // Set the user's image:
        StorageUtil.loadImgFromStorage(this.context, application.getUserImagePath(), holder.userImg, R.drawable.guest);

        // Resolve the applications when the buttons are clicked:
        holder.btnAccept.setOnClickListener(_v -> this.resolveApplication(holder, application, true));
        holder.btnReject.setOnClickListener(_v -> this.resolveApplication(holder, application, false));
    }

    private void resolveApplication(ApplicationVH holder, Application application, boolean accepted) {
        // Hide the two buttons:
        holder.btnAccept.setVisibility(View.GONE);
        holder.btnReject.setVisibility(View.GONE);

        // Show the progress bar:
        holder.pbLoading.setVisibility(View.VISIBLE);

        // Call the cloud function to accept or reject the user:
        this.functionsHandler.resolveApplication(
                application.getUid(),
                this.currentBranch,
                accepted,
                false, // TODO: Change that later
                () -> {
                    // Show the two buttons:
                    holder.btnAccept.setVisibility(View.VISIBLE);
                    holder.btnReject.setVisibility(View.VISIBLE);

                    // Hide the progress bar:
                    holder.pbLoading.setVisibility(View.GONE);

                    // Alert the user:
                    final String msg = String.format(
                            Locale.getDefault(),
                            "%s was successfully %s",
                            application.getUserFullName(), accepted ? "accepted" : "rejected"
                    );
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }, e -> {
                    // Show the two buttons:
                    holder.btnAccept.setVisibility(View.VISIBLE);
                    holder.btnReject.setVisibility(View.VISIBLE);

                    // Hide the progress bar:
                    holder.pbLoading.setVisibility(View.GONE);

                    // Alert the user:
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();

                    // Log the error:
                    Log.e(TAG, "Failed to accept/reject user", e);
                }
        );
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
