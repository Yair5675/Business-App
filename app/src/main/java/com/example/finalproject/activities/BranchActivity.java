package com.example.finalproject.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.custom_views.adapters.ScreenSlideAdapter;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.Employee;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.branch.ApplicationsFragment;
import com.example.finalproject.fragments.branch.EmployeesFragment;
import com.example.finalproject.util.EmployeeStatus;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;

public class BranchActivity extends AppCompatActivity {
    // The current user connected to the app:
    private User currentUser;

    // The current branch that is being displayed:
    private Branch currentBranch;

    // The employees fragment:
    private EmployeesFragment employeesFragment;

    // The applications fragment:
    private ApplicationsFragment applicationsFragment;

    // The view pager that allows the user to swipe between fragments:
    private ViewPager2 pager;

    // The user's status in the branch:
    private EmployeeStatus employeeStatus;

    // Tag for debugging purposes:
    private static final String TAG = "BranchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch);

        // Load the user from the given intent:
        this.loadUserFromIntent();

        // Load the branch from the given intent:
        this.loadBranchFromIntent();

        // Set the company name using the branch:
        ((TextView) findViewById(R.id.actBranchTvCompanyName)).setText(this.currentBranch.getCompanyName());

        // Create the employees fragment:
        this.employeesFragment = new EmployeesFragment(this.currentUser, this.currentBranch);

        // Create the applications fragment:
        this.applicationsFragment = new ApplicationsFragment(this.currentBranch);

        // Load the view pager:
        this.pager = findViewById(R.id.actBranchPager);
        this.initPagerAdapter();

        // Set the initial employee status to unemployed:
        this.employeeStatus = EmployeeStatus.UNEMPLOYED;

        // Listen to the current user's status in the current branch:
        this.initStatusListener();

        // Load the back button callback:
        this.loadBackButtonCallback();

        // Set the toolbar:
        final Toolbar toolbar = findViewById(R.id.actBranchToolbar);
        this.setSupportActionBar(toolbar);

        // Show the back button on the toolbar:
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // Set the title of the toolbar:
        final TextView tvTitle = findViewById(R.id.actBranchTvToolbarTitle);
        tvTitle.setText(this.currentBranch.getCompanyName());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Get the id of the item:
        final int ID = item.getItemId();

        // Configure back navigation with the toolbar:
        if (ID == android.R.id.home) {
            finish();
            return true;
        }

        // If it's another item, use super call:
        return super.onOptionsItemSelected(item);
    }

    private void initStatusListener() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("branches")
                .document(this.currentBranch.getBranchId())
                .collection("employees")
                .document(this.currentUser.getUid())
                .addSnapshotListener(this, (value, error) -> {
                    // Check the error:
                    if (error != null) {
                        Log.e(TAG, "Listen failed", error);
                        return;
                    }

                    // Check if the user isn't employed at the branch:
                    EmployeeStatus status;
                    if (value == null || !value.exists())
                        status = EmployeeStatus.UNEMPLOYED;

                    else {
                        // Convert to employee:
                        final Employee employee = value.toObject(Employee.class);

                        // Check if the employee is a manager:
                        if (employee == null)
                            status = EmployeeStatus.UNEMPLOYED;
                        else if (employee.isManager())
                            status = EmployeeStatus.MANAGER;
                        else
                            status = EmployeeStatus.EMPLOYED;
                    }

                    // Change the status for the activity:
                    this.setEmployeeStatus(status);
                });
    }

    private void setEmployeeStatus(EmployeeStatus status) {
        // Save the current status:
        this.employeeStatus = status;

        // Only allow the user to scroll between fragments if they are managers:
        if (this.employeeStatus != EmployeeStatus.MANAGER) {
            this.pager.setCurrentItem(0);
            this.pager.setUserInputEnabled(false);
        }
        else
            this.pager.setUserInputEnabled(true);

        // Set the status in the employees fragment:
        this.employeesFragment.setEmployeeStatus(status);
    }

    private void initPagerAdapter() {
        // Initialize the adapter and prevent the user from swiping at first:
        ScreenSlideAdapter adapter = new ScreenSlideAdapter(this, this.getFragments());
        this.pager.setAdapter(adapter);
    }

    private Fragment[] getFragments() {
        return new Fragment[] { this.employeesFragment, this.applicationsFragment };
    }

    private void loadUserFromIntent() {
        // Get the intent:
        final Intent intent = getIntent();

        // Check that a user was indeed given:
        if (intent.hasExtra("user")) {
            Serializable user = intent.getSerializableExtra("user");
            // Check type (also null checker):
            if (user instanceof User)
                this.currentUser = (User) user;
        }
    }

    private void loadBranchFromIntent() {
        // Get the intent:
        final Intent intent = getIntent();

        // Check that a branch was indeed given:
        if (intent.hasExtra("branch")) {
            Serializable branch = intent.getSerializableExtra("branch");
            // Check type (also null checker):
            if (branch instanceof Branch)
                this.currentBranch = (Branch) branch;
        }
    }

    private void loadBackButtonCallback() {
        // Define the callback:
        final OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        // Add the callback:
        getOnBackPressedDispatcher().addCallback(callback);
    }

}