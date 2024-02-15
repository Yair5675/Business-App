package com.example.finalproject.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.custom_views.adapters.ScreenSlideAdapter;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.branch.ApplicationsFragment;
import com.example.finalproject.fragments.branch.EmployeesFragment;

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

        // Load the back button callback:
        this.loadBackButtonCallback();

    }

    private void initPagerAdapter() {
        // Initialize the adapter and prevent the user from swiping at first:
        ScreenSlideAdapter adapter = new ScreenSlideAdapter(this, this.getFragments());
        this.pager.setAdapter(adapter);
        this.pager.setCurrentItem(0);
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
                // Go to the main activity:
                final Intent intent = new Intent(BranchActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        // Add the callback:
        getOnBackPressedDispatcher().addCallback(callback);
    }

}