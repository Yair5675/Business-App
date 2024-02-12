package com.example.finalproject.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.custom_views.OnlineEmployeeAdapter;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.Employee;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.util.EmployeeActions;
import com.example.finalproject.util.WrapperLinearLayoutManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;

public class BranchActivity extends AppCompatActivity {
    // The current user connected to the app:
    private User currentUser;

    // The current branch that is being displayed:
    private Branch currentBranch;

    // The text view displaying the branch's name:
    private TextView tvCompanyName;

    // The text view that tells the user if the branch is currently opened or closed:
    private TextView tvCurrentOpenness;

    // The text view that shows the working hours of the branch:
    private TextView tvWorkingHours;

    // The text view that shows the branch's address:
    private TextView tvAddress;

    // The adapter for the recycler view:
    private OnlineEmployeeAdapter adapter;

    // The recycler view that shows the branch's employees:
    private RecyclerView rvEmployees;

    // The button that allows the user to leave the branch:
    private Button btnLeave;

    // The button that allows the user to apply to the branch:
    private Button btnApply;

    // The user's status in the branch:
    private EmployeeStatus employeeStatus;
    private enum EmployeeStatus {
        MANAGER,
        EMPLOYED,
        UNEMPLOYED
    }

    // Tag for debugging purposes:
    private static final String TAG = "BranchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch);

        // Load the views of the activity:
        this.tvCompanyName = findViewById(R.id.actBranchTvCompanyName);
        this.tvCurrentOpenness = findViewById(R.id.actBranchTvCurrentOpenness);
        this.tvWorkingHours = findViewById(R.id.actBranchTvWorkingTimes);
        this.tvAddress = findViewById(R.id.actBranchTvAddress);
        this.rvEmployees = findViewById(R.id.actBranchRvEmployees);
        this.btnApply = findViewById(R.id.actBranchBtnApplyToBusiness);
        this.btnLeave = findViewById(R.id.actBranchBtnLeaveBranch);

        // Initialize layout manager:
        this.rvEmployees.setLayoutManager(new WrapperLinearLayoutManager(this));

        // Load the user from the given intent:
        this.loadUserFromIntent();

        // Load the branch from the given intent:
        this.loadBranchFromIntent();

        // Load the info from the branch:
        this.loadInfoFromBranch();

        // Initialize the recycler view adapter:
        this.initAdapter();

        // Set initial employee status to "employed":
        this.setEmployeeStatus(EmployeeStatus.UNEMPLOYED);

        // Load the back button callback:
        this.loadBackButtonCallback();

        // Activate the listener to check if the user is a manager in this branch:
        this.listenToEmployeeStatus();
    }

    private void listenToEmployeeStatus() {
        // Get a reference to the database:
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db
                .collection("branches")
                .document(this.currentBranch.getBranchId())
                .collection("employees")
                .document(this.currentUser.getUid())
                // Add a snapshot listener:
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
        // Save the status:
        this.employeeStatus = status;

        // Update the adapter:
        this.adapter.setIsManager(status == EmployeeStatus.MANAGER);

        // Show the "leave branch" button if the user is employed at the branch or the "apply to
        // branch" if they aren't:
        final boolean isEmployed = status != EmployeeStatus.UNEMPLOYED;
        this.btnApply.setVisibility(isEmployed ? View.GONE : View.VISIBLE);
        this.btnLeave.setVisibility(isEmployed ? View.VISIBLE : View.GONE);
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

    private void initAdapter() {
        // Get a reference to the database:
        final FirebaseFirestore dbRef = FirebaseFirestore.getInstance();

        // Create the recyclerView's options:
        final Query query = dbRef
                .collection("branches")
                .document(this.currentBranch.getBranchId())
                .collection("employees");
        FirestoreRecyclerOptions<Employee> options = new FirestoreRecyclerOptions.Builder<Employee>()
                .setLifecycleOwner(this)
                .setQuery(query, Employee.class)
                .build();

        // Create and set the adapter for the recycler view:
        this.adapter = new OnlineEmployeeAdapter(
                this.employeeStatus == EmployeeStatus.MANAGER,
                this,
                this.getEmployeesActions(),
                options
        );
        this.rvEmployees.setAdapter(this.adapter);
    }

    private EmployeeActions getEmployeesActions() {
        // TODO: Implement this function
        return null;
    }

    private void loadInfoFromBranch() {
        // Set the company name:
        this.tvCompanyName.setText(this.currentBranch.getCompanyName());

        // Set the current openness:
        if (isBranchOpen()) {
            this.tvCurrentOpenness.setText(R.string.act_branch_opened_msg);
            this.tvCurrentOpenness.setTextColor(Color.GREEN);
        }
        else {
            this.tvCurrentOpenness.setText(R.string.act_branch_closed_msg);
            this.tvCurrentOpenness.setTextColor(Color.RED);
        }

        // Set the opening and closing time:
        final int openHour = this.currentBranch.getOpeningTime() / 60,
                openMinute = this.currentBranch.getOpeningTime() % 60,
                closedHour = this.currentBranch.getClosingTime() / 60,
                closedMinute = this.currentBranch.getClosingTime() % 60;
        this.tvWorkingHours.setText(String.format(
                Locale.getDefault(), "Opened during: %d:%02d - %d:%02d",
                openHour, openMinute, closedHour, closedMinute
        ));

        // Set the address:
        this.tvAddress.setText(this.currentBranch.getFullAddress());
    }

    private boolean isBranchOpen() {
        final Calendar calendar = Calendar.getInstance();
        final int currentMinutes = 60 * calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE);
        return this.currentBranch.getClosingTime() > currentMinutes &&
                currentMinutes >= this.currentBranch.getOpeningTime();
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