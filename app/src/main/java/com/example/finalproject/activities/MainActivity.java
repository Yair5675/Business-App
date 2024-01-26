package com.example.finalproject.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.example.finalproject.R;
import com.example.finalproject.database.online.OnlineDatabase;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.util.Util;

import java.util.Locale;

import pl.droidsonroids.gif.GifImageButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // A reference to the online database:
    private OnlineDatabase db;

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

        // TODO: Check if the user is the admin and set the crown accordingly

        // Show the 'Edit Account' and 'Delete Account' buttons:
        this.btnEditAccount.setVisibility(View.VISIBLE);
        this.btnDeleteAccount.setVisibility(View.VISIBLE);
        this.tvEditAccountDesc.setVisibility(View.VISIBLE);
        this.tvDeleteAccountDesc.setVisibility(View.VISIBLE);

        // Hide the progress bar:
        this.pbActivityLoading.setVisibility(View.GONE);
    }

    private void initWithoutUser() {
        // Setting the default picture for guests:
        Util.setCircularImage(this, this.imgUser, R.drawable.guest);

        // Setting the default greeting:
        this.tvUserGreeting.setText(R.string.act_main_user_greeting_default_txt);

        // Make the admin crown disappear:
        this.imgAdminCrown.setVisibility(View.GONE);

        // Make the 'Edit Account' and 'Delete Account' disappear:
        this.btnEditAccount.setVisibility(View.GONE);
        this.btnDeleteAccount.setVisibility(View.GONE);
        this.tvEditAccountDesc.setVisibility(View.GONE);
        this.tvDeleteAccountDesc.setVisibility(View.GONE);

        // Hide the progress bar:
        this.pbActivityLoading.setVisibility(View.GONE);
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
        // If they want to log in to a user, create the login dialog:
        else if (ID == R.id.menuUsersItemSignIn)
            activateSignInPage();

        // If they want to log out:
        else if (ID == R.id.menuUsersItemDisconnect) {
            // TODO: Disconnect the user
        }

        // If they want to read the "About Us" dialog:
        else if (ID == R.id.menuUsersItemAbout) {
            this.activateAboutDialog();
        }

        // If they want to see the users:
        else if (ID == R.id.menuUsersItemShowUsers) {
            final Intent intent = new Intent(this, UsersActivity.class);
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

    private void activateSignInPage() {
        // TODO: Open up the sign in page using FireBase authentication built-in UI
    }

    @Override
    public void onClick(View view) {
        // Getting the ID:
        final int ID = view.getId();

        if (ID == R.id.actMainImgBtnEdit) {
            Intent intent = new Intent(this, InputActivity.class);
            startActivity(intent);
            finish();
        }
        else if (ID == R.id.actMainImgBtnDelete) {
            // Open the dialog to delete a user:
            this.activateDeleteDialog();
        }
    }

    private void activateDeleteDialog() {
        // TODO: Complete the deletion dialog
    }
}