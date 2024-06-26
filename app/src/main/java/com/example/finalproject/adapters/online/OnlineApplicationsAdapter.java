package com.example.finalproject.adapters.online;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.finalproject.util.Permissions;
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

import java.util.ArrayList;
import java.util.Locale;

public class OnlineApplicationsAdapter extends OnlineAdapter<Application, OnlineApplicationsAdapter.ApplicationVH> {
    // The activity that holds the applications adapter:
    private final Activity activity;

    // A reference to the online database:
    private final FirebaseFirestore db;

    // The current branch that the applications are referring to:
    private Branch currentBranch;

    // Tag for debugging purposes:
    private static final String TAG = "OnlineApplicationsAdapter";

    public OnlineApplicationsAdapter(
            Activity activity, Branch currentBranch, Runnable onEmptyCallback, Runnable onNotEmptyCallback,
            @NonNull FirestoreRecyclerOptions<Application> options
    ) {
        super(activity, onEmptyCallback, onNotEmptyCallback, options);
        this.activity = activity;
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

        // Show a dialog asking the user if they want to register the applicant's phone number on
        // their phone:
        holder.layoutUserDetails.setOnClickListener(_v -> this.showSavePhoneDialog(application));
    }

    private void showSavePhoneDialog(Application application) {
        // Create a dialog confirming their desire:
        final AlertDialog savePhoneDialog = new AlertDialog.Builder(this.context)
                .setTitle(R.string.row_application_save_contact_title)
                .setMessage(R.string.row_application_save_contact_msg)
                .setPositiveButton("Yes", ((dialogInterface, i) -> this.saveApplicantPhone(application)))
                .setNegativeButton("No", null)
                .create();
        savePhoneDialog.show();
    }

    private void saveApplicantPhone(Application application) {
        // Check for permissions:
        if (!Permissions.checkPermissions(this.context, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)) {
            Log.i(TAG, "Permission to read/write contacts was not given, asking for it");
            Permissions.requestPermissions(this.activity, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS);
            return;
        }
        // Get content resolver to access the contacts content provider:
        final ContentResolver contentResolver = this.context.getContentResolver();

        // Create a list to hold the operations needed to add a contact:
        final ArrayList<ContentProviderOperation> operationList = new ArrayList<>(3);

        // Add the initially empty contact:
        operationList.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // Add the applicant's name:
        operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, application.getUserFullName())
                .build());

        // Add the applicant's phone number:
        operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, application.getUserPhoneNumber())
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        // Execute the operations:
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
            Log.i(TAG, "Successfully added contract");
            Toast.makeText(context, "Successfully added contract!", Toast.LENGTH_SHORT).show();
        } catch (RemoteException | OperationApplicationException e) {
            Log.e(TAG, "Error saving contract", e);
            Toast.makeText(context, "An error occurred. Try again later", Toast.LENGTH_SHORT).show();
        }
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

            // Send an SMS to the applicant:
            this.sendSMS(application, accepted);
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

    private void sendSMS(Application application, boolean accepted) {
        // Prepare the message:
        final String msg;
        if (accepted)
            msg = String.format(this.context.getString(R.string.application_accepted_text), application.getUserFullName(), this.currentBranch.getCompanyName());
        else
            msg = String.format(this.context.getString(R.string.application_rejected_text), application.getUserFullName(), this.currentBranch.getCompanyName());

        // Validate permission:
        if (Permissions.checkPermissions(this.context, Manifest.permission.SEND_SMS)) {
            // Send the text to the phone number:
            final SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(application.getUserPhoneNumber(), null, msg, null, null);
        }
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

            transaction.update(this.currentBranch.getReference(), Branch.PENDING_APPLICATIONS, Math.max(0, pending - 1));

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

        // The layout of the applicant's details:
        private final LinearLayout layoutUserDetails;

        public ApplicationVH(@NonNull View itemView) {
            super(itemView);

            // Load the views:
            this.userImg = itemView.findViewById(R.id.rowApplicationUserImage);
            this.tvUserName = itemView.findViewById(R.id.rowApplicationTvFullName);
            this.tvPhoneNumber = itemView.findViewById(R.id.rowApplicationTvPhoneNumber);
            this.btnAccept = itemView.findViewById(R.id.rowApplicationBtnAccept);
            this.btnReject = itemView.findViewById(R.id.rowApplicationBtnReject);
            this.pbLoading = itemView.findViewById(R.id.rowApplicationPbLoading);
            this.layoutUserDetails = itemView.findViewById(R.id.rowApplicationLayoutUserDetails);
        }
    }
}
