package com.example.finalproject.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.dialogs.HelpHomeScreenDialog;
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
import com.example.finalproject.services.ShiftNotificationService;
import com.example.finalproject.util.Permissions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {
    // A reference to the online database:
    private OnlineDatabase db;

    // The image view holding the user's image:
    private ImageView imgUser;

    // The text view greeting the user:
    private TextView tvUserGreeting;

    // The text view showing the time greeting (Good morning / afternoon / etc...):
    private TextView tvUserTimeGreeting;

    // The tab layout allowing the user to conveniently navigate between fragments:
    private TabLayout tabLayout;

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

    // The count down timer that changes the greeting:
    private CountDownTimer greetingTimer;

    // A map connecting the hour of the day to the greeting suiting it:
    private Map<Integer, String> hourlyGreetingsMap;

    // Tag for debugging purposes:
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ask the user for permission to send messages (necessary for other parts of the app):
        this.askPermissions();

        // Initialize the online database reference:
        this.db = OnlineDatabase.getInstance();

        // Load the user's image view and greeting text view:
        this.imgUser = findViewById(R.id.actMainImgUser);
        this.tvUserGreeting = findViewById(R.id.actMainTvUserGreeting);
        this.tvUserTimeGreeting = findViewById(R.id.actMainTvTimeGreeting);

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

        // Initialize the tab layout:
        this.tabLayout = findViewById(R.id.actMainTabLayout);
        this.initTabLayout();

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

        // Initialize the greetings timer:
        this.initGreetingsTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancel the greetings timer:
        if (this.greetingTimer != null)
            this.greetingTimer.cancel();
    }

    private void initGreetingsTimer() {
        // Load the greetings from the resources:
        this.loadGreetings();

        // Set the current greeting:
        this.tvUserTimeGreeting.setText(this.hourlyGreetingsMap.get(this.getCurrentGreetingHour()));

        // Schedule the next switch:
        this.scheduleNextGreetingSwitch();
    }

    private void scheduleNextGreetingSwitch() {
        // Get the next hour:
        final int nextGreetingHour = this.getNextGreetingHour();
        final long nextGreetingMillis = this.getMillisUntilHour(nextGreetingHour);

        // Plan the count down timer (Tick every 10 minutes):
        final long INTERVAL_MILLIS = 1000 * 60 * 10;
        this.greetingTimer = new CountDownTimer(nextGreetingMillis, INTERVAL_MILLIS) {
            @Override
            public void onTick(long millisUntilFinished) {
                final long secondsUntilFinished = millisUntilFinished / 1000;
                Log.i(
                        TAG,
                        String.format(
                                "Changing greeting in %d hours, %d minutes and %d seconds (at %d:00)",
                                secondsUntilFinished / (60 * 60),  // Hours
                                (secondsUntilFinished / 60) % 60,  // Minutes
                                secondsUntilFinished % 60,  // Seconds
                                nextGreetingHour
                        )
                );
            }

            @Override
            public void onFinish() {
                // Change the greeting in the text view:
                tvUserTimeGreeting.setText(hourlyGreetingsMap.get(nextGreetingHour));

                // Schedule the next change:
                scheduleNextGreetingSwitch();
            }
        }.start();
    }

    private long getMillisUntilHour(int hour) {
        // Get the current time:
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        // Set the hour:
        final int prevHour = calendar.get(Calendar.HOUR_OF_DAY);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the new hour is before the current one, it must mean it's the next day:
        if (prevHour > hour)
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        // Calculate difference:
        return calendar.getTimeInMillis() - System.currentTimeMillis();
    }

    private int getCurrentGreetingHour() {
        // Get the current greeting hour:
        final Set<Integer> keys = this.hourlyGreetingsMap.keySet();

        // Get the current hour:
        final Calendar calendar = Calendar.getInstance();
        final Date now = new Date();
        calendar.setTime(now);
        final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        // Get the key of the highest hour before now:
        int currentKey = keys.iterator().next();
        for (int hour : keys) {
            if (hour > currentHour)
                break;
            else
                currentKey = hour;
        }

        return currentKey;
    }

    private int getNextGreetingHour() {
        // Get the current greeting hour:
        final Set<Integer> keys = this.hourlyGreetingsMap.keySet();

        // Get the current hour:
        final Calendar calendar = Calendar.getInstance();
        final Date now = new Date();
        calendar.setTime(now);
        final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        // Loop over them all until you find the suitable one:
        for (int hour : keys) {
            if (hour > currentHour)
                return hour;
        }

        // If no hour was found, return the first hour:
        return keys.iterator().next();
    }

    private void loadGreetings() {
        // Initialize the map:
        this.hourlyGreetingsMap = new TreeMap<>(); // Use a tree map for sorted keys

        // Load the greetings:
        final String[] greetingsArr = getResources().getStringArray(R.array.hourly_greetings_array);

        // Parse the hours and the greeting itself:
        for (String greeting : greetingsArr) {
            final String[] parts = greeting.split(":");
            // Try to parse the hour (avoid exceptions):
            final int hour;
            try {
                // Parse the hour and add it with the greeting to the map:
                hour = Integer.parseInt(parts[0]);
                this.hourlyGreetingsMap.put(hour, parts[1]);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Could not parse greeting hour", e);
            }
        }
    }

    private void askPermissions() {
        if (!Permissions.checkPermissions(this, Manifest.permission.SEND_SMS))
            Permissions.requestPermissions(this, Manifest.permission.SEND_SMS);

        if (!Permissions.checkPermissions(this, Manifest.permission.POST_NOTIFICATIONS))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Permissions.requestPermissions(this, Manifest.permission.POST_NOTIFICATIONS);
            }
    }

    private void initTabLayout() {
        // Set up a mediator between the layout and the pager:
        final String[] titles = this.getFragmentsTitles();
        final @DrawableRes int[] icons = this.getFragmentsIcons();
        new TabLayoutMediator(this.tabLayout, this.pager,
                (tab, position) -> {
                    tab.setText(titles[position]);
                    tab.setIcon(icons[position]);
                }
        ).attach();

        // Set up swiping listeners:
        this.tabLayout.addOnTabSelectedListener(this);
        this.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
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

        // Show the tab layout:
        this.tabLayout.setVisibility(View.VISIBLE);

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

        // Start the notifications service:
        final Intent intent = new Intent(this, ShiftNotificationService.class);
        intent.putExtra(ShiftNotificationService.UID_INTENT_KEY, user.getUid());
        startService(intent);
    }

    private void initWithoutUser() {
        // Disconnect the user:
        this.connectedUser = null;
        this.db.disconnectUser();

        // Hide the tab layout:
        this.tabLayout.setVisibility(View.GONE);

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

        // Stop the notification service:
        final Intent intent = new Intent(this, ShiftNotificationService.class);
        stopService(intent);
    }

    private void initPagerAdapter() {
        // Initialize the adapter and prevent the user from swiping at first:
        final ScreenSlideAdapter adapter = new ScreenSlideAdapter(this, this.getFragments());
        this.pager.setAdapter(adapter);
        this.pager.setUserInputEnabled(false);
        this.pager.setCurrentItem(getPersonalFragmentIndex());
    }

    private Fragment[] getFragments() {
        return new Fragment[] { this.personalFragment, this.branchesFragment, this.workplaceFragment, this.shiftsFragment };
    }

    private String[] getFragmentsTitles() {
        return new String[] { "Personal", "Businesses", "Your Workplaces", "Your Shifts" };
    }

    private @DrawableRes int[] getFragmentsIcons() {
        return new int[] {
                R.drawable.person_icon, R.drawable.add_business_icon, R.drawable.work_icon,
                R.drawable.shift_icon
        };
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
        menu.findItem(R.id.menuUsersItemShiftsHistory).setVisible(isUserLoggedIn);
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
        }

        // If they want to watch their shifts history:
        else if (ID == R.id.menuUsersItemShiftsHistory) {
            // Give null as branch ID to show all branches:
            ShiftsHistoryActivity.startActivity(this, this.connectedUser.getUid(), null);
        }

        // If they want to see the users:
        else if (ID == R.id.menuUsersItemShowUsers) {
            // Send the activity the current user:
            final Intent intent = new Intent(this, UsersActivity.class);
            intent.putExtra("user", this.connectedUser);
            startActivity(intent);
            finish();
        }

        // If they need help in the home screen:
        else if (ID == R.id.menuUsersItemHelpHome)
            // Show the help dialog:
            new HelpHomeScreenDialog().show(getSupportFragmentManager(), null);

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
    public void onTabSelected(TabLayout.Tab tab) {
        this.pager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}