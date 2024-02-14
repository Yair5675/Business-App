package com.example.finalproject.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.branch.EmployeesFragment;

import java.io.Serializable;

public class BranchActivity extends AppCompatActivity {
    // The current user connected to the app:
    private User currentUser;

    // The current branch that is being displayed:
    private Branch currentBranch;

    // The employees fragment:
    private EmployeesFragment employeesFragment;

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

        // Load the employees activity:
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.actBranchFragmentContainer, this.employeesFragment).commit();

        // Load the back button callback:
        this.loadBackButtonCallback();

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