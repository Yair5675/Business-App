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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.custom_views.adapters.ScreenSlideAdapter;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.Employee;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.branch.ApplicationsFragment;
import com.example.finalproject.fragments.branch.EmployeesFragment;
import com.example.finalproject.fragments.input.business.BusinessUpdateForm;
import com.example.finalproject.util.EmployeeStatus;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;

public class BranchActivity extends AppCompatActivity {
    // The current user connected to the app:
    private User currentUser;

    // The current branch that is being displayed:
    private Branch currentBranch;

    // A reference to the online database:
    private FirebaseFirestore dbRef;

    // The employees fragment:
    private EmployeesFragment employeesFragment;

    // The title of the activity:
    private TextView tvTitle;

    // The title of the toolbar:
    private TextView tvToolbarTitle;

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

        // Initialize the database reference:
        this.dbRef = FirebaseFirestore.getInstance();

        // Load the user from the given intent:
        this.loadUserFromIntent();

        // Load the branch from the given intent:
        this.loadBranchFromIntent();

        // Set the company name using the branch:
        this.tvTitle = findViewById(R.id.actBranchTvCompanyName);
        this.tvTitle.setText(this.currentBranch.getCompanyName());

        // Create the employees fragment:
        this.employeesFragment = new EmployeesFragment(this.currentUser, this.currentBranch);

        // Create the applications fragment:
        this.applicationsFragment = new ApplicationsFragment(this.currentBranch);

        // Load the view pager:
        this.pager = findViewById(R.id.actBranchPager);
        this.initPagerAdapter();

        // Set the initial employee status to unemployed:
        this.employeeStatus = EmployeeStatus.UNEMPLOYED;

        // Listen to the branch document:
        this.initBranchListener();

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
        this.tvToolbarTitle = findViewById(R.id.actBranchTvToolbarTitle);
        this.tvToolbarTitle.setText(this.currentBranch.getCompanyName());
    }

    private void initBranchListener() {
        this.dbRef
                .collection("branches")
                .document(this.currentBranch.getBranchId())
                .addSnapshotListener(this, (branchDocument, error) -> {
                    if (error != null) {
                        // Log any error that occurred:
                        Log.e(TAG, "Error listening to branch", error);

                        // Go back to the main activity:
                        Toast.makeText(this, "Couldn't load branch", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    // Check if the branch was deleted:
                    else if (branchDocument == null || !branchDocument.exists()) {
                        Toast.makeText(this, "The branch was deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    // If it was updated, update it in the fragments:
                    else {
                        final Branch branch = branchDocument.toObject(Branch.class);
                        if (branch != null)
                            this.setCurrentBranch(branch);
                        else
                            Log.e(TAG, "Couldn't convert document to Branch object");
                    }
                });
    }

    private void setCurrentBranch(Branch branch) {
        this.currentBranch = branch;
        this.employeesFragment.setCurrentBranch(branch);
        this.applicationsFragment.setBranch(branch);
        this.tvToolbarTitle.setText(branch.getCompanyName());
        this.tvTitle.setText(branch.getCompanyName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the branch menu XML file:
        getMenuInflater().inflate(R.menu.branch_menu, menu);

        // Show The items only if the current user is a manager:
        final boolean isManager = this.employeeStatus == EmployeeStatus.MANAGER;
        menu.findItem(R.id.menuBranchItemEdit).setVisible(isManager);
        menu.findItem(R.id.menuBranchItemDelete).setVisible(isManager);
        menu.findItem(R.id.menuBranchItemSetShifts).setVisible(isManager);

        return true;
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
        // If the manager wants to update the branch:
        else if (ID == R.id.menuBranchItemEdit) {
            // Create the update form and set it in the input activity:
            BusinessUpdateForm updateForm = new BusinessUpdateForm(
                    this.currentBranch, this.currentUser, getResources()
            );
            InputActivity.CurrentInput.setCurrentInputForm(updateForm);

            // Go to the input activity:
            Intent intent = new Intent(this, InputActivity.class);
            startActivity(intent);
            finish();
        }
        // TODO: Implement delete and set shifts items too

        // If it's another item, use super call:
        return super.onOptionsItemSelected(item);
    }

    private void initStatusListener() {
        this.dbRef.collection("branches")
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