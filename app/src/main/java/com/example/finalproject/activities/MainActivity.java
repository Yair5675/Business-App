package com.example.finalproject.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.custom_views.LoginDialog;
import com.example.finalproject.database.online.OnlineDatabase;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.main.BranchesFragment;
import com.example.finalproject.fragments.main.PersonalFragment;
import com.example.finalproject.fragments.input.user.UserRegistrationForm;

public class MainActivity extends AppCompatActivity {
    // A reference to the online database:
    private OnlineDatabase db;

    // The view pager that allows the user to swipe between fragments:
    private ViewPager2 pager;

    // A reference to the personal fragment:
    private PersonalFragment personalFragment;

    // A reference to the branches fragment:
    private BranchesFragment branchesFragment;

    // The connected user:
    private User connectedUser;

    // The login dialog (reusable):
    private LoginDialog loginDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the online database reference:
        this.db = OnlineDatabase.getInstance();

        // Initialize the personal fragment without a user:
        this.personalFragment = new PersonalFragment(
                this, null, this::disconnectUser
        );

        // Initialize the branches fragment:
        this.branchesFragment = new BranchesFragment();

        // Initialize the view pager:
        this.pager = findViewById(R.id.actMainPager);
        this.initPagerAdapter();

        // Get the current user:
        this.db.getCurrentUser(
                this::connectUser, e -> {
                    this.connectedUser = null;
                    supportInvalidateOptionsMenu();
                }
        );

        // Initialize the login dialog:
        this.loginDialog = new LoginDialog(
                this, getResources(),
                this::connectUser
        );
    }

    private void connectUser(User user) {
        // Set the user:
        this.connectedUser = user;
        this.personalFragment.setConnectedUser(user);
        this.branchesFragment.setUser(user);

        // Update the menu:
        supportInvalidateOptionsMenu();

        // Allow the user to swipe:
        this.pager.setUserInputEnabled(true);
    }

    private void disconnectUser() {
        // Disconnect the user:
        this.connectedUser = null;
        this.db.disconnectUser();

        // Update the personal fragment:
        this.personalFragment.setConnectedUser(null);

        // Update the pager:
        this.pager.setUserInputEnabled(false);
        this.pager.setCurrentItem(getPersonalFragmentIndex());

        // Update the menu:
        supportInvalidateOptionsMenu();
    }

    private void initPagerAdapter() {
        // Initialize the adapter and prevent the user from swiping at first:
        ScreenSlideAdapter adapter = new ScreenSlideAdapter(this);
        this.pager.setAdapter(adapter);
        this.pager.setUserInputEnabled(false);
        this.pager.setCurrentItem(getPersonalFragmentIndex());
    }

    private Fragment[] getFragments() {
        return new Fragment[] { this.personalFragment, this.branchesFragment };
    }

    private int getPersonalFragmentIndex() {
        final Fragment[] fragments = getFragments();
        int index;
        for (index = 0; index < fragments.length; index++) {
            if (fragments[index] == personalFragment)
                return index;
        }

        // The code should never reach this point:
        return index;
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
            // Create the registration form and set it for the input activity:
            final UserRegistrationForm registrationForm = new UserRegistrationForm(getResources());
            InputActivity.CurrentInput.setCurrentInputForm(registrationForm);

            // Launch the input activity:
            final Intent intent = new Intent(this, InputActivity.class);
            startActivity(intent);
            finish();
        }
        // If they want to log in to a user, show the login dialog:
        else if (ID == R.id.menuUsersItemSignIn)
            this.loginDialog.show();

        // If they want to log out:
        else if (ID == R.id.menuUsersItemDisconnect)
            this.disconnectUser();

        // If they want to verify the email:
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
            intent.putExtra("user", this.connectedUser);
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

    private class ScreenSlideAdapter extends FragmentStateAdapter {
        // The fragments inside the main activity:
        private final Fragment[] fragments;

        public ScreenSlideAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            this.fragments = getFragments();
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return this.fragments[position];
        }

        @Override
        public int getItemCount() {
            return this.fragments.length;
        }
    }
}