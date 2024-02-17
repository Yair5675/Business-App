package com.example.finalproject.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.ImprovedTextWatcher;
import com.example.finalproject.util.InputValidation;
import com.example.finalproject.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddRoleDialog {
    // The context of the dialog:
    private final Context context;

    // The dialog that this class represent:
    private final Dialog dialog;

    // The ID of the branch that the role is added to:
    private final String branchId;

    // A reference to the online database:
    private final FirebaseFirestore dbRef;

    // The input fields that receive the role name:
    private final TextInputLayout tilRoleName;
    private final TextInputEditText etRoleName;

    // The button that submits the role name:
    private final Button btnSubmit;

    // Tag for debugging purposes:
    private static final String TAG = "AddRoleDialog";

    public AddRoleDialog(Context context, String branchId) {
        // Inflate the dialog XML file:
        this.dialog = new Dialog(context);
        this.dialog.setContentView(R.layout.dialog_add_role);

        // Initialize the database reference:
        this.dbRef = FirebaseFirestore.getInstance();

        // Save the branch ID:
        this.branchId = branchId;

        // Save the context:
        this.context = context;

        // Set the width of the dialog to 90% the screen, and its height to minimal:
        final Resources res = context.getResources();
        final int width = (int) (res.getDisplayMetrics().widthPixels * 0.9);
        final int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        Window window;
        if ((window = this.dialog.getWindow()) != null)
            window.setLayout(width, height);

        // Load the views in the dialog:
        this.tilRoleName = this.dialog.findViewById(R.id.dialogAddRoleTilRoleName);
        this.etRoleName = this.dialog.findViewById(R.id.dialogAddRoleEtRoleName);
        this.btnSubmit = this.dialog.findViewById(R.id.dialogAddRoleBtnSubmit);

        // Initialize the text watcher for the role name input field:
        this.initTextWatcher();

        // Initialize the submit button onClickListener:
        this.initSubmitListener();
    }

    public void show() {
        // Clear out previous text:
        this.etRoleName.setText("");
        this.tilRoleName.setError(null);

        // Show the dialog:
        this.dialog.show();
    }

    private void initTextWatcher() {
        this.etRoleName.addTextChangedListener(
                (ImprovedTextWatcher)
                        (_c, _i, _i1, _i2) ->
                                Util.validateAndSetError(
                                        this.tilRoleName,
                                        this.etRoleName,
                                        InputValidation::validateRoleName
                                ));
    }

    private void initSubmitListener() {
        this.btnSubmit.setOnClickListener(_v -> {
            // Validate the input:
            Util.validateAndSetError(this.tilRoleName, this.etRoleName, InputValidation::validateRoleName);

            // Check if there is an error:
            if (this.tilRoleName.getError() != null)
                return;

            final String roleName = Util.getTextFromEt(this.etRoleName);


            // Check if there is a similar branch:
            final OnFailureListener onFailureListener = e -> {
                Log.e(TAG, "Failed to add role", e);
                if (e.getMessage() != null && e.getMessage().equals(Constants.SIMILAR_ROLE_FOUND_ERROR))
                    Toast.makeText(context, Constants.SIMILAR_ROLE_FOUND_ERROR, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            };
            this.validateSimilarRole(
                    roleName,
                    unused -> {
                        // Create the new role:
                        final Map<String, String> data = new HashMap<>();
                        data.put("roleName", roleName);
                        this.dbRef
                                .document(String.format("branches/%s/roles/%s", this.branchId, roleName))
                                .set(data)
                                .addOnSuccessListener(unused1 -> {
                                    // Alert the user:
                                    Toast.makeText(context, "Role added successfully", Toast.LENGTH_SHORT).show();
                                    this.dialog.dismiss();
                                })
                                .addOnFailureListener(onFailureListener);
                    }, onFailureListener
            );
        });
    }

    private void validateSimilarRole(
            String roleName,
            OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        this.dbRef
                .document(String.format("branches/%s/roles/%s", this.branchId, roleName))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists())
                        onFailureListener.onFailure(new Exception(Constants.SIMILAR_ROLE_FOUND_ERROR));
                    else
                        onSuccessListener.onSuccess(null);
                })
                .addOnFailureListener(onFailureListener);
    }
}
