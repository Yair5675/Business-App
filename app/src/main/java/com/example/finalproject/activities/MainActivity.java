package com.example.finalproject.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.dialogs.LoginDialog;
import com.example.finalproject.adapters.ScreenSlideAdapter;
import com.example.finalproject.database.online.OnlineDatabase;
import com.example.finalproject.database.online.StorageUtil;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.input.business.BusinessRegistrationForm;
import com.example.finalproject.fragments.main.BranchesFragment;
import com.example.finalproject.fragments.main.PersonalFragment;
import com.example.finalproject.fragments.input.user.UserRegistrationForm;
import com.example.finalproject.fragments.main.ShiftsFragment;
import com.example.finalproject.fragments.main.WorkplaceFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // A reference to the online database:
    private OnlineDatabase db;

    // The image view holding the user's image:
    private ImageView imgUser;

    // The text view greeting the user:
    private TextView tvUserGreeting;

    // TODO: Update the text view that says "Good morning" to "Good evening" or other time greetings

    // TODO: Add another activity that receives a user and a branch they work at, and presents the
    //  entire shifts history of the user in this branch.
    // The adapter for the view pager:
    private ScreenSlideAdapter adapter;

    // The view pager that allows the user to swipe between fragments:
    private ViewPager2 pager;

    // A reference to the personal fragment:
    private PersonalFragment personalFragment;

    // A reference to the workplace fragment:
    private WorkplaceFragment workplaceFragment;

    // A reference to the branches fragment:
    private BranchesFragment branchesFragment;

    // A reference to the shifts fragment:
    private ShiftsFragment shiftsFragment;

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

        // Load the user's image view and greeting text view:
        this.imgUser = findViewById(R.id.actMainImgUser);
        this.tvUserGreeting = findViewById(R.id.actMainTvUserGreeting);

        // Initialize the personal fragment without a user:
        this.personalFragment = new PersonalFragment(
                this, null, this::initWithoutUser
        );

        // Initialize the branches fragment:
        this.branchesFragment = new BranchesFragment();

        // Initialize the workplaces fragment:
        this.workplaceFragment = WorkplaceFragment.newInstance(null);

        // Initialize the shifts fragment:
        this.shiftsFragment = ShiftsFragment.newInstance(null);

        // Initialize the view pager:
        this.pager = findViewById(R.id.actMainPager);
        this.initPagerAdapter();

        // Get the current user:
        this.db.getCurrentUser(
                this::initWithUser, _e -> this.initWithoutUser()
        );

        // Initialize the login dialog:
        this.loginDialog = new LoginDialog(
                this, getResources(),
                this::initWithUser
        );

        // Set the toolbar:
        this.setSupportActionBar(findViewById(R.id.actMainToolbar));

        // Configure what happens when the back button is pressed:
        this.initBackPress();
    }

    private void initBackPress() {
        final OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Create an alert dialog asking if the want to exit:
                final AlertDialog exitDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.act_main_exit_dialog_title)
                        .setMessage(R.string.act_main_exit_dialog_body)
                        .setPositiveButton("Yes", (dialogInterface, i) -> finish())
                        .setNegativeButton("No", ((dialogInterface, i) -> {}))
                        .create();

                // Show the exit dialog:
                exitDialog.show();
            }
        };
        // Add the callback:
        getOnBackPressedDispatcher().addCallback(callback);
    }

    private void initWithUser(User user) {
        // Set the user:
        this.connectedUser = user;

        // Update the fragments:
        this.personalFragment.setConnectedUser(user);
        this.branchesFragment.setUser(user);
        this.workplaceFragment.setUser(user);
        this.shiftsFragment.setUser(user);

        // Update the user's image:
        StorageUtil.loadUserImgFromStorage(this, user, this.imgUser, R.drawable.guest);

        // Update the user's greeting:
        this.tvUserGreeting.setText(String.format(
                Locale.getDefault(), "Hello %s!", user.getName())
        );

        // Update the menu:
        supportInvalidateOptionsMenu();

        // Allow the user to swipe:
        this.pager.setUserInputEnabled(true);
    }

    private void initWithoutUser() {
        // Disconnect the user:
        this.connectedUser = null;
        this.db.disconnectUser();

        // Update the personal fragment:
        this.personalFragment.setConnectedUser(null);

        // Set the displayed image to a guest image:
        this.imgUser.setImageResource(R.drawable.guest);

        // Restore the user's greeting to its default value:
        this.tvUserGreeting.setText(R.string.act_main_user_greeting_default_txt);

        // Update the pager:
        this.pager.setUserInputEnabled(false);
        this.pager.setCurrentItem(getPersonalFragmentIndex());

        // Update the menu:
        supportInvalidateOptionsMenu();
    }

    private void initPagerAdapter() {
        // Initialize the adapter and prevent the user from swiping at first:
        this.adapter = new ScreenSlideAdapter(this, this.getFragments());
        this.pager.setAdapter(this.adapter);
        this.pager.setUserInputEnabled(false);
        this.pager.setCurrentItem(getPersonalFragmentIndex());
    }

    private Fragment[] getFragments() {
        return new Fragment[] { this.personalFragment, this.branchesFragment, this.workplaceFragment, this.shiftsFragment };
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
        menu.findItem(R.id.menuUsersItemAddBusiness).setVisible(isUserLoggedIn);
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
            InputActivity.setCurrentInputForm(registrationForm);

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
            this.initWithoutUser();

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

        // If they want to add their own business:
        else if (ID == R.id.menuUsersItemAddBusiness) {
            // Create the registration form and set it in the input activity:
            final BusinessRegistrationForm form = new BusinessRegistrationForm(getResources(), this.connectedUser);
            InputActivity.setCurrentInputForm(form);

            // Go to the input activity:
            Intent intent = new Intent(this, InputActivity.class);
            startActivity(intent);
            finish();
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
}