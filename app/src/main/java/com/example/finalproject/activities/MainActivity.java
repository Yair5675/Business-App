package com.example.finalproject.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.custom_views.LoginDialog;
import com.example.finalproject.database.online.OnlineDatabase;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.util.Util;

import java.util.Locale;

import pl.droidsonroids.gif.GifImageButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // A reference to the online database:
    private OnlineDatabase db;

    // The currently connected user (null if no user is connected):
    private User connectedUser;

    // The profile picture of the user:
    private ImageView imgUser;

    // The progress bar that will appear when the activity is loading:
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

    // The login dialog (it is reusable so it's an attribute to save resources):
    private LoginDialog loginDialog;

    // Tag for debugging purposes:
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Loading the various views of the activity:
        this.imgUser = findViewById(R.id.actMainImgUser);
        this.imgAdminCrown = findViewById(R.id.actMainImgAdminCrown);
        this.pbActivityLoading = findViewById(R.id.actMainPbActivityLoading);
        this.tvUserGreeting = findViewById(R.id.actMainTvUserGreeting);
        this.btnEditAccount = findViewById(R.id.actMainImgBtnEdit);
        this.btnDeleteAccount = findViewById(R.id.actMainImgBtnDelete);
        this.tvEditAccountDesc = findViewById(R.id.actMainTvEditBtn);
        this.tvDeleteAccountDesc = findViewById(R.id.actMainTvDeleteBtn);

        // Initialize the login dialog:
        this.loginDialog = new LoginDialog(this, getResources(), this::initWithUser);
        
        // Initialize the database reference:
        this.db = OnlineDatabase.getInstance();

        // Show the progress bar until the activity is fully initialized:
        this.pbActivityLoading.setVisibility(View.VISIBLE);

        // Try to initialize with a connected user (and initialize without one if no user is
        // connected):
        this.db.getCurrentUser(this::initWithUser, e -> {
            // Log the error:
            Log.e(TAG, "Failed to get connected user", e);

            // Activate the activity without a user:
            initWithoutUser();
        });

        // Set OnClickListeners:
        this.btnEditAccount.setOnClickListener(this);
        this.btnDeleteAccount.setOnClickListener(this);

    }

    private void initWithUser(User user) {
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
        this.db.loadUserImgFromStorage(this, user, this.imgUser, R.drawable.guest);

        // Show the crown image if the user is the admin:
        this.imgAdminCrown.setVisibility(user.isAdmin() ? View.VISIBLE : View.GONE);

        // Show the 'Edit Account' and 'Delete Account' buttons:
        this.changeButtonsVisibility(View.VISIBLE);

        // Hide the progress bar:
        this.pbActivityLoading.setVisibility(View.GONE);

        // Update the menu:
        supportInvalidateOptionsMenu();
    }

    private void initWithoutUser() {
        // Set the user to null:
        this.connectedUser = null;

        // Setting the default picture for guests:
        Util.setCircularImage(this, this.imgUser, R.drawable.guest);

        // Setting the default greeting:
        this.tvUserGreeting.setText(R.string.act_main_user_greeting_default_txt);

        // Make the admin crown disappear:
        this.imgAdminCrown.setVisibility(View.GONE);

        // Make the 'Edit Account' and 'Delete Account' disappear:
        this.changeButtonsVisibility(View.GONE);

        // Hide the progress bar:
        this.pbActivityLoading.setVisibility(View.GONE);

        supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the users menu file:
        getMenuInflater().inflate(R.menu.users_menu, menu);

        // Hide certain items according to if a user is logged in:
        final boolean isUserLoggedIn = this.db.isUserSignedIn();
        menu.findItem(R.id.menuUsersItemSignUp).setVisible(!isUserLoggedIn);
        menu.findItem(R.id.menuUsersItemSignIn).setVisible(!isUserLoggedIn);
        menu.findItem(R.id.menuUsersItemVerification).setVisible(isUserLoggedIn && !this.db.isConnectedUserEmailVerified());
        menu.findItem(R.id.menuUsersItemShowUsers).setVisible(isUserLoggedIn);
        menu.findItem(R.id.menuUsersItemDisconnect).setVisible(isUserLoggedIn);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        // Check which item was clicked:
        final int ID = item.getItemId();

        // If they want to register a new user:
        if (ID == R.id.menuUsersItemSignUp) {
            final Intent intent = new Intent(this, InputActivity.class);
            startActivity(intent);
            finish();
        }
        // If they want to log in to a user, show the login dialog:
        else if (ID == R.id.menuUsersItemSignIn)
            this.loginDialog.show();

        // If they want to log out:
        else if (ID == R.id.menuUsersItemDisconnect) {
            // Disconnect the user:
            this.db.disconnectUser();
            this.initWithoutUser();
            supportInvalidateOptionsMenu();
        }

        // If the want to verify the email:
        else if (ID == R.id.menuUsersItemVerification) {
            this.db.sendVerificationEmail(task -> {
                if (task.isSuccessful())
                    Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
            });
        }

        // If they want to read the "About Us" dialog:
        else if (ID == R.id.menuUsersItemAbout) {
            this.activateAboutDialog();
        }

        // If they want to see the users:
        else if (ID == R.id.menuUsersItemShowUsers) {
            // Send the activity the current user:
            final Intent intent = new Intent(this, UsersActivity.class);
            intent.putExtra("user", connectedUser);
            startActivity(intent);
            finish();
        }

        return true;
    }

    private void activateAboutDialog() {
        // Bind the XML file to the dialog:
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_about);

        // Set the width to 90% of the screen and height to minimal:
        final int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        final int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        Window window;
        if ((window = dialog.getWindow()) != null)
            window.setLayout(width, height);

        // Set cancelable to true:
        dialog.setCancelable(true);

        // Show the dialog:
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        // Getting the ID:
        final int ID = view.getId();

        if (ID == R.id.actMainImgBtnEdit) {
            // Open the input activity but send the connected user in the intent:
            Intent intent = new Intent(MainActivity.this, InputActivity.class);
            intent.putExtra("user", this.connectedUser);
            startActivity(intent);
            finish();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Delete account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Delete", (dialogInterface, i) -> {
                    // Make the buttons disappear and show the progress bar:
                    changeButtonsVisibility(View.GONE);
                    pbActivityLoading.setVisibility(View.VISIBLE);

                    // Delete the account:
                    this.db.deleteCurrentUser(this.connectedUser, unused -> {
                        initWithoutUser();
                        Toast.makeText(MainActivity.this, "Your account was deleted", Toast.LENGTH_SHORT).show();
                    }, exception -> {
                        changeButtonsVisibility(View.VISIBLE);
                        pbActivityLoading.setVisibility(View.GONE);
                        Log.e(TAG, "Failed to delete current user", exception);
                        Toast.makeText(this, "Failed to delete your account", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Cancel", null);

        // Show the dialog:
        builder.create().show();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Before exiting the activity, check if the input activity sent a new email for the user:
        final Intent intent = getIntent();
        if (this.connectedUser != null) {
            if (intent.hasExtra("new email")) {
                final String newEmail = intent.getStringExtra("new email");
                if (newEmail != null) {
                    this.db.logUserIn(
                            newEmail,
                            this.connectedUser.getPassword(),
                            user -> connectedUser = user,
                            e -> Log.e(TAG, "Failed to log in with new email", e)
                    );
                }
            }
            // If there is no new email, refresh the user:
            else {
                this.db.refreshUser(
                        this.connectedUser.getPassword(),
                        user -> connectedUser = user,
                        e -> Log.e(TAG, "Failed to refresh user", e)
                );
            }
        }
    }
}