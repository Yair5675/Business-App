package com.example.finalproject.fragments.main;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.example.finalproject.util.Util;

import java.util.Locale;

import pl.droidsonroids.gif.GifImageButton;

public class PersonalFragment extends Fragment implements View.OnClickListener {
    // A reference to the online database:
    private OnlineDatabase db;

    // The currently connected user (null if no user is connected):
    private @Nullable User connectedUser;

    // The image-view holding the profile picture of the user:
    private ImageView imgUser;

    // A callback that will be run if the fragment deletes the current user:
    private final Runnable onUserDeleted;

    // The progress bar that will appear when the fragment is loading:
    private ProgressBar pbActivityLoading;

    // The textView which greets the user:
    private TextView tvUserGreeting;

    // The crown image that appears if the user is an admin:
    private ImageView imgAdminCrown;

    // The 'Edit Account' button and its description:
    private GifImageButton btnEditAccount;
    private TextView tvEditAccountDesc;

    // The 'Delete Account' button and its description:
    private GifImageButton btnDeleteAccount;
    private TextView tvDeleteAccountDesc;

    // Tag for debugging purposes:
    private static final String TAG = "PersonalFragment";

    public PersonalFragment(@Nullable User connectedUser, Runnable onUserDeleted) {
        this.connectedUser = connectedUser;
        this.onUserDeleted = onUserDeleted;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the personal fragment XML:
        final View parent = inflater.inflate(R.layout.fragment_personal, container, false);

        // Loading the various views of the fragment:
        this.imgUser = parent.findViewById(R.id.fragPersonalImgUser);
        this.imgAdminCrown = parent.findViewById(R.id.fragPersonalImgAdminCrown);
        this.pbActivityLoading = parent.findViewById(R.id.fragPersonalPbActivityLoading);
        this.tvUserGreeting = parent.findViewById(R.id.fragPersonalTvUserGreeting);
        this.btnEditAccount = parent.findViewById(R.id.fragPersonalImgBtnEdit);
        this.btnDeleteAccount = parent.findViewById(R.id.fragPersonalImgBtnDelete);
        this.tvEditAccountDesc = parent.findViewById(R.id.fragPersonalTvEditBtn);
        this.tvDeleteAccountDesc = parent.findViewById(R.id.fragPersonalTvDeleteBtn);

        // Initialize the database reference:
        this.db = OnlineDatabase.getInstance();

        // Show the progress bar until the fragment is fully initialized:
        this.pbActivityLoading.setVisibility(View.VISIBLE);

        // Initialize with or without a user (depends on the given User object):
        if (this.connectedUser != null)
            initWithUser(this.connectedUser);
        else
            initWithoutUser();

        // Set OnClickListeners:
        this.btnEditAccount.setOnClickListener(this);
        this.btnDeleteAccount.setOnClickListener(this);

        return parent;
    }

    private void initWithUser(@NonNull User user) {
        // Save the user:
        this.connectedUser = user;

        // Fix the user's email:
        this.db.fixUserEmail(this.connectedUser);

        // Change the greeting:
        this.tvUserGreeting.setText(
                String.format(
                        Locale.getDefault(),
                        "Hello, %s!",
                        user.getName()
                )
        );

        // Show the user's image:
        this.db.loadUserImgFromStorage(requireContext(), user, this.imgUser, R.drawable.guest);

        // Show the crown image if the user is the admin:
        this.imgAdminCrown.setVisibility(user.isAdmin() ? View.VISIBLE : View.GONE);

        // Show the 'Edit Account' and 'Delete Account' buttons:
        this.changeButtonsVisibility(View.VISIBLE);

        // Hide the progress bar:
        this.pbActivityLoading.setVisibility(View.GONE);
    }

    private void initWithoutUser() {
        // Set the user to null:
        this.connectedUser = null;

        // Setting the default picture for guests:
        Util.setCircularImage(requireContext(), this.imgUser, R.drawable.guest);

        // Setting the default greeting:
        this.tvUserGreeting.setText(R.string.act_main_user_greeting_default_txt);

        // Make the admin crown disappear:
        this.imgAdminCrown.setVisibility(View.GONE);

        // Make the 'Edit Account' and 'Delete Account' disappear:
        this.changeButtonsVisibility(View.GONE);

        // Hide the progress bar:
        this.pbActivityLoading.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        // Getting the ID:
        final int ID = view.getId();

        if (ID == R.id.actMainImgBtnEdit) {
            // Open the input activity but send the connected user in the intent:
            Intent intent = new Intent(requireContext(), InputActivity.class);
            intent.putExtra("user", this.connectedUser);
            startActivity(intent);
            requireActivity().finish();
        }
        else if (ID == R.id.actMainImgBtnDelete) {
            // Open the dialog to delete a user:
            this.activateDeleteDialog();
        }
    }

    private void changeButtonsVisibility(int visibility) {
        this.btnEditAccount.setVisibility(visibility);
        this.btnDeleteAccount.setVisibility(visibility);
        this.tvEditAccountDesc.setVisibility(visibility);
        this.tvDeleteAccountDesc.setVisibility(visibility);
    }

    private void activateDeleteDialog() {
        // Create the delete dialog:
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setCancelable(false)
                .setTitle("Delete account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Delete", (dialogInterface, i) -> {
                    // Make the buttons disappear and show the progress bar:
                    changeButtonsVisibility(View.GONE);
                    pbActivityLoading.setVisibility(View.VISIBLE);

                    // Delete the account:
                    this.db.deleteCurrentUser(this.connectedUser, unused -> {
                        // Initialize the fragment without the user:
                        initWithoutUser();
                        Toast.makeText(requireContext(), "Your account was deleted", Toast.LENGTH_SHORT).show();

                        // Activate the callback:
                        this.onUserDeleted.run();
                    }, exception -> {
                        changeButtonsVisibility(View.VISIBLE);
                        pbActivityLoading.setVisibility(View.GONE);
                        Log.e(TAG, "Failed to delete current user", exception);
                        Toast.makeText(requireContext(), "Failed to delete your account", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Cancel", null);

        // Show the dialog:
        builder.create().show();
    }
}
