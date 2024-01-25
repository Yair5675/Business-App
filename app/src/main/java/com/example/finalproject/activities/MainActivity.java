package com.example.finalproject.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.database.local.AppDatabase;
import com.example.finalproject.database.local.SharedPreferenceHandler;
import com.example.finalproject.database.local.entities.City;
import com.example.finalproject.database.local.entities.User;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.Result;
import com.example.finalproject.util.Util;

import java.io.FileNotFoundException;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // The profile picture of the user:
    private ImageView imgUser;

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

    // A reference to the shared preferences handler:
    private SharedPreferenceHandler spHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Loading the various views of the activity:
        this.imgUser = findViewById(R.id.actMainImgUser);
        this.imgAdminCrown = findViewById(R.id.actMainImgAdminCrown);
        this.tvUserGreeting = findViewById(R.id.actMainTvUserGreeting);
        this.btnEditAccount = findViewById(R.id.actMainImgBtnEdit);
        this.btnDeleteAccount = findViewById(R.id.actMainImgBtnDelete);
        this.tvEditAccountDesc = findViewById(R.id.actMainTvEditBtn);
        this.tvDeleteAccountDesc = findViewById(R.id.actMainTvDeleteBtn);
        
        // Initialize the database reference:
        final AppDatabase db = AppDatabase.getInstance(this);

        // Initialize the shared preferences handler:
        this.spHandler = SharedPreferenceHandler.getInstance(this);

        // Check if a user is saved in the shared preference for automatic log in:
        final Result<Long, String> idResult = this.spHandler.getLong("id");
        if (idResult.isOk())
            AppDatabase.connectUser(db.userDao().getUserById(idResult.getValue()));

        // Initializing the activity according to if a user is logged in:
        this.userConnectivityChanged();

        // Set OnClickListeners:
        this.btnEditAccount.setOnClickListener(this);
        this.btnDeleteAccount.setOnClickListener(this);

        // Load countries if they hadn't been loaded already:
        this.loadCities();

    }

    private void initDefault() {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the users menu file:
        getMenuInflater().inflate(R.menu.users_menu, menu);

        // Hide certain items according to if a user is logged in:
        final boolean isUserLoggedIn = AppDatabase.isUserLoggedIn();
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
            // Disconnect and delete the shared preferences:
            AppDatabase.disconnect();
            this.spHandler.remove("id");
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

        userConnectivityChanged();

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

    public void userConnectivityChanged() {
        if (AppDatabase.isUserLoggedIn())
            initWithUser();
        else
            initDefault();
        supportInvalidateOptionsMenu();
    }

    private void initWithUser() {
        // Get the connected user:
        final User user = AppDatabase.getConnectedUser();

        // Get the image bitmap:
        final Result<Bitmap, FileNotFoundException> imageResult = Util.getImage(
                this,
                user.getPictureFileName()
        );

        // Set it as the user's photo if it was loaded successfully:
        if (imageResult.isOk())
            Util.setCircularImage(this, this.imgUser, imageResult.getValue());
        else
            Log.e("MainActivity - initWithUser", "Could not load photo: " + imageResult.getError());

        // Change the greeting:
        this.tvUserGreeting.setText(
                String.format(
                        Locale.getDefault(),
                        "Hello, %s!",
                        user.getName()
                )
        );

        // Show the admin crown if it is an admin:
        this.imgAdminCrown.setVisibility(user.isAdmin() ? View.VISIBLE : View.GONE);

        // Show the 'Edit Account' and 'Delete Account' buttons:
        this.btnEditAccount.setVisibility(View.VISIBLE);
        this.btnDeleteAccount.setVisibility(View.VISIBLE);
        this.tvEditAccountDesc.setVisibility(View.VISIBLE);
        this.tvDeleteAccountDesc.setVisibility(View.VISIBLE);
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
        // Define the onClickListener for the dialog:
        final DialogInterface.OnClickListener onClickListener =
                (dialogInterface, buttonClicked) -> {
                    if (buttonClicked == DialogInterface.BUTTON_POSITIVE) {
                        // Disconnect the user:
                        final User connectedUser = AppDatabase.getConnectedUser();
                        AppDatabase.disconnect();

                        // Delete them from the database:
                        final Result<Void, String> delResult = connectedUser.deleteUser(this);
                        if (delResult.isErr())
                            Log.e("Main delete user", delResult.getError());

                        // Delete them from the shared preferences:
                        this.spHandler.remove("id");

                        // Refresh the activity:
                        userConnectivityChanged();
                    }
                };

        // Build the dialog:
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Confirm", onClickListener)
                .setNegativeButton("Cancel", onClickListener)
                .setCancelable(false);

        // Show the dialog:
        builder.create().show();
    }

    private void loadCities() {
        // Load cities only if they haven't been loaded already:
        final AppDatabase db = AppDatabase.getInstance(this);
        if (db.cityDao().getCitiesCount() == 0) {

            // Load the cities on a separate thread:
            final Thread thread = new Thread(() -> {
                for (String cityName : Constants.CITIES) {
                    // Create the city and add it:
                    final City city = new City(cityName);

                    db.cityDao().insert(city);
                }
            });

            thread.start();
        }
    }
}