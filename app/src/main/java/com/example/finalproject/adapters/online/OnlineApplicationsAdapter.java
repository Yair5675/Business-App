package com.example.finalproject.adapters.online;

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
import com.example.finalproject.database.online.StorageUtil;
import com.example.finalproject.database.online.collections.Application;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.Employee;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.database.online.collections.Workplace;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.Locale;

public class OnlineApplicationsAdapter extends OnlineAdapter<Application, OnlineApplicationsAdapter.ApplicationVH> {
    // A reference to the online database:
    private final FirebaseFirestore db;

    // The current branch that the applications are referring to:
    private Branch currentBranch;

    // Tag for debugging purposes:
    private static final String TAG = "OnlineApplicationsAdapter";

    public OnlineApplicationsAdapter(
            Context context, Branch currentBranch, Runnable onEmptyCallback, Runnable onNotEmptyCallback,
            @NonNull FirestoreRecyclerOptions<Application> options
    ) {
        super(context, onEmptyCallback, onNotEmptyCallback, options);
        this.db = FirebaseFirestore.getInstance();
        this.currentBranch = currentBranch;
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

        // Set the user's phone number:
        holder.tvPhoneNumber.setText(application.getUserPhoneNumber());

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

        // Define callbacks:
        final OnSuccessListener<Void> onSuccessListener = unused -> {
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
        };
        final OnFailureListener onFailureListener = e -> {
            // Show the two buttons:
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);

            // Hide the progress bar:
            holder.pbLoading.setVisibility(View.GONE);

            // Alert the user:
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();

            // Log the error:
            Log.e(TAG, "Failed to accept/reject user", e);
        };

        // Accept or reject the application:
        if (accepted)
            this.acceptApplication(application.getUid(), application.getUid(), onSuccessListener, onFailureListener);
        else
            this.rejectApplication(application.getUid(), onSuccessListener, onFailureListener);
    }

    private void acceptApplication(
            String applicationId, String uid,
            OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener
    ) {
        // Create the document references:
        final DocumentReference applicationRef = this.db.document(String.format(
                "branches/%s/applications/%s", this.currentBranch.getBranchId(), applicationId
        )), workplaceRef = this.db.document(String.format(
                "users/%s/workplaces/%s", uid, this.currentBranch.getBranchId()
        )), employeeRef = this.db.document(String.format(
                "branches/%s/employees/%s", this.currentBranch.getBranchId(), uid
        )), userRef = this.db.collection("users").document(uid);

        // Create the workplace object (the manager is not a manager):
        final Workplace workplace = Workplace.fromBranch(this.currentBranch, false);

        // Run the transaction:
        this.db.runTransaction((Transaction.Function<Void>) transaction -> {
            // Get the application and user document. This ensures it is not changed from
            // another device during the transaction:
            final DocumentSnapshot applicationDoc = transaction.get(applicationRef);
            final DocumentSnapshot userDoc = transaction.get(userRef);
            final DocumentSnapshot branchDoc = transaction.get(this.currentBranch.getReference());

            if (!applicationDoc.exists())
                throw new FirebaseFirestoreException("Application document does not exist", FirebaseFirestoreException.Code.NOT_FOUND);
            if (!userDoc.exists())
                throw new FirebaseFirestoreException("User document does not exist", FirebaseFirestoreException.Code.NOT_FOUND);

            final User user = userDoc.toObject(User.class);
            if (user == null)
                throw new FirebaseFirestoreException("Couldn't convert user document to object", FirebaseFirestoreException.Code.INVALID_ARGUMENT);

            if (!branchDoc.exists())
                throw new FirebaseFirestoreException("Branch document does not exist", FirebaseFirestoreException.Code.NOT_FOUND);

            // Set the workplace and employee documents:
            transaction.set(workplaceRef, workplace, SetOptions.merge());
            transaction.set(employeeRef, Employee.fromUser(user, false), SetOptions.merge());

            // Lower the amount of pending applications in the branch (if it's above 0):
            final Long pending = branchDoc.getLong(Branch.PENDING_APPLICATIONS);
            if (pending == null)
                throw new FirebaseFirestoreException("Document doesn't contain pending applications", FirebaseFirestoreException.Code.INVALID_ARGUMENT);

            if (pending > 0)
                transaction.update(this.currentBranch.getReference(), Branch.PENDING_APPLICATIONS, FieldValue.increment(-1));

            // Delete the application:
            transaction.delete(applicationRef);

            return null;
        }).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }

    private void rejectApplication(
            String applicationId, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener
    ) {
        // Get references:
        final DocumentReference applicationRef = this.db.document(String.format(
                "branches/%s/applications/%s", this.currentBranch.getBranchId(), applicationId
        )), branchRef = this.currentBranch.getReference();

        // Make a transaction:
        this.db.runTransaction((Transaction.Function<Void>) transaction -> {
            // Get the branch:
            final DocumentSnapshot branchDoc = transaction.get(branchRef);

            if (!branchDoc.exists())
                throw new FirebaseFirestoreException("Branch document does not exist", FirebaseFirestoreException.Code.NOT_FOUND);

            // Lower the amount of applications if it's above 0:
            final Long pending = branchDoc.getLong(Branch.PENDING_APPLICATIONS);
            if (pending == null)
                throw new FirebaseFirestoreException("Document doesn't contain pending applications", FirebaseFirestoreException.Code.INVALID_ARGUMENT);
            if (pending > 0)
                transaction.update(branchRef, Branch.PENDING_APPLICATIONS, FieldValue.increment(-1));

            // Delete the application:
            transaction.delete(applicationRef);

            return null;
        }).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }

    public static class ApplicationVH extends RecyclerView.ViewHolder {
        // The imageView holding the user's image:
        private final ImageView userImg;

        // The textView holding the user's full name:
        private final TextView tvUserName;

        // The text view holding the user's phone number:
        private final TextView tvPhoneNumber;

        // The accept and reject buttons:
        private final Button btnAccept, btnReject;

        // The progress bar shown when either button is pressed:
        private final ProgressBar pbLoading;

        public ApplicationVH(@NonNull View itemView) {
            super(itemView);

            // Load the views:
            this.userImg = itemView.findViewById(R.id.rowApplicationUserImage);
            this.tvUserName = itemView.findViewById(R.id.rowApplicationTvFullName);
            this.tvPhoneNumber = itemView.findViewById(R.id.rowApplicationTvPhoneNumber);
            this.btnAccept = itemView.findViewById(R.id.rowApplicationBtnAccept);
            this.btnReject = itemView.findViewById(R.id.rowApplicationBtnReject);
            this.pbLoading = itemView.findViewById(R.id.rowApplicationPbLoading);
        }
    }
}
