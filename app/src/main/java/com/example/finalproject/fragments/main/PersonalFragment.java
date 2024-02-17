package com.example.finalproject.fragments.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalproject.R;
import com.example.finalproject.activities.InputActivity;
import com.example.finalproject.database.online.OnlineDatabase;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.input.user.UserUpdateForm;
import com.example.finalproject.util.Constants;

import pl.droidsonroids.gif.GifImageButton;

public class PersonalFragment extends Fragment implements View.OnClickListener {
    // The context of the fragment:
    private final Context context;

    // A reference to the online database:
    private OnlineDatabase db;

    // The layout holding all the edit texts:
    private LinearLayout detailsLayout;

    // The text view that appears when the user is not connected:
    private TextView tvDisconnectedMessage;

    // The edit texts saving the user's info:
    private EditText etName, etBirthdate, etPhoneNumber, etAddress, etEmail, etPassword;

    // The currently connected user (null if no user is connected):
    private @Nullable User connectedUser;

    // A callback that will be run if the fragment deletes the current user:
    private final Runnable onUserDeleted;

    // The progress bar that will appear when the fragment is loading:
    private ProgressBar pbActivityLoading;

    // The layout holding the two buttons:
    private LinearLayout buttonsLayout;

    // The 'Edit Account' button and its description:
    private GifImageButton btnEditAccount;
    private TextView tvEditAccountDesc;

    // The 'Delete Account' button and its description:
    private GifImageButton btnDeleteAccount;
    private TextView tvDeleteAccountDesc;

    // Tag for debugging purposes:
    private static final String TAG = "PersonalFragment";

    public PersonalFragment(@NonNull Context context, @Nullable User connectedUser, Runnable onUserDeleted) {
        this.context = context;
        this.setConnectedUser(connectedUser);
        this.onUserDeleted = onUserDeleted;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the personal fragment XML:
        final View parent = inflater.inflate(R.layout.fragment_main_personal, container, false);

        // Loading the various views of the fragment:
        this.etName = parent.findViewById(R.id.fragMainPersonalEtFullName);
        this.etBirthdate = parent.findViewById(R.id.fragMainPersonalEtBirthdate);
        this.etPhoneNumber = parent.findViewById(R.id.fragMainPersonalEtPhoneNumber);
        this.etAddress = parent.findViewById(R.id.fragMainPersonalEtAddress);
        this.etEmail = parent.findViewById(R.id.fragMainPersonalEtEmail);
        this.etPassword = parent.findViewById(R.id.fragMainPersonalEtPassword);

        this.detailsLayout = parent.findViewById(R.id.fragMainPersonalDetailsLayout);
        this.buttonsLayout = parent.findViewById(R.id.fragMainPersonalButtonsLayout);

        this.pbActivityLoading = parent.findViewById(R.id.fragMainPersonalPbLoading);
        this.tvDisconnectedMessage = parent.findViewById(R.id.fragMainPersonalTvNothingToSee);

        this.btnEditAccount = parent.findViewById(R.id.fragPersonalImgBtnEdit);
        this.btnDeleteAccount = parent.findViewById(R.id.fragPersonalImgBtnDelete);
        this.tvEditAccountDesc = parent.findViewById(R.id.fragPersonalTvEditBtn);
        this.tvDeleteAccountDesc = parent.findViewById(R.id.fragPersonalTvDeleteBtn);

        // Initialize the database reference:
        this.db = OnlineDatabase.getInstance();

        // Show the progress bar until the fragment is fully initialized:
        this.pbActivityLoading.setVisibility(View.VISIBLE);

        // Initialize the fragment with the given connected user:
        if (this.connectedUser != null)
            initWithUser(this.connectedUser);
        else
            initWithoutUser();

        // Set OnClickListeners:
        this.btnEditAccount.setOnClickListener(this);
        this.btnDeleteAccount.setOnClickListener(this);

        return parent;
    }

    public void setConnectedUser(@Nullable User connectedUser) {
        // Check that the users are different (for efficiency reasons):
        if (this.connectedUser != connectedUser) {
            this.connectedUser = connectedUser;
            if (isInitialized()) {
                if (connectedUser == null)
                    initWithoutUser();
                else
                    initWithUser(connectedUser);
            }
        }
    }

    private boolean isInitialized() {
        final View[] fragmentViews = {
                this.etName, this.etBirthdate, this.etPhoneNumber, this.etAddress, this.etEmail,
                this.etPassword, this.detailsLayout, this.buttonsLayout, this.pbActivityLoading,
                this.btnEditAccount, this.btnDeleteAccount, this.tvEditAccountDesc,
                this.tvDeleteAccountDesc
        };
        boolean isInitialized = true;
        for (int i = 0; i < fragmentViews.length && isInitialized; i++)
            isInitialized = fragmentViews[i] != null;

        return isInitialized;
    }

    private void initWithUser(@NonNull User user) {
        // Fix the user's email:
        this.db.fixUserEmail(this.connectedUser);

        // Set the info of the user in the edit texts:
        this.etName.setText(user.getFullName());
        this.etBirthdate.setText(Constants.DATE_FORMAT.format(user.getBirthdate()));
        this.etPhoneNumber.setText(user.getPhoneNumber());
        this.etAddress.setText(user.getAddress());
        this.etEmail.setText(user.getEmail());
        this.etPassword.setText(user.getPassword());

        // Hide the disconnected message:
        this.tvDisconnectedMessage.setVisibility(View.GONE);

        // Show the details and buttons layout:
        this.detailsLayout.setVisibility(View.VISIBLE);
        this.buttonsLayout.setVisibility(View.VISIBLE);

        // Hide the progress bar:
        this.pbActivityLoading.setVisibility(View.GONE);
    }

    private void initWithoutUser() {
        // Hide the details and buttons layouts:
        this.detailsLayout.setVisibility(View.GONE);
        this.buttonsLayout.setVisibility(View.GONE);

        // Show the disconnected message:
        this.tvDisconnectedMessage.setVisibility(View.VISIBLE);

        // Hide the progress bar:
        this.pbActivityLoading.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        // Getting the ID:
        final int ID = view.getId();

        if (ID == R.id.fragPersonalImgBtnEdit) {
            if (this.connectedUser != null) {
                // Create the update form and set the user form in it:
                final UserUpdateForm updateForm = new UserUpdateForm(this.connectedUser, getResources());
                InputActivity.setCurrentInputForm(updateForm);

                // Open the input activity:
                Intent intent = new Intent(this.context, InputActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        }
        else if (ID == R.id.fragPersonalImgBtnDelete) {
            // Open the dialog to delete a user:
            this.activateDeleteDialog();
        }
    }

    private void activateDeleteDialog() {
        // Create the delete dialog:
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context)
                .setCancelable(false)
                .setTitle("Delete account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Delete", (dialogInterface, i) -> {
                    // Hide the buttons layout and show the progress bar:
                    this.buttonsLayout.setVisibility(View.GONE);
                    this.pbActivityLoading.setVisibility(View.VISIBLE);

                    // Delete the account:
                    this.db.deleteCurrentUser(this.connectedUser, unused -> {
                        // Initialize the fragment without the user:
                        this.setConnectedUser(null);
                        Toast.makeText(this.context, "Your account was deleted", Toast.LENGTH_SHORT).show();

                        // Activate the callback:
                        this.onUserDeleted.run();
                    }, exception -> {
                        // Show the buttons layout and hide the progress bar:
                        this.buttonsLayout.setVisibility(View.VISIBLE);
                        pbActivityLoading.setVisibility(View.GONE);

                        // Log the error and alert the user:
                        Log.e(TAG, "Failed to delete current user", exception);
                        Toast.makeText(this.context, "Failed to delete your account", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Cancel", null);

        // Show the dialog:
        builder.create().show();
    }
}
