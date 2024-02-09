package com.example.finalproject.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.database.online.collections.Branch;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;

public class BranchActivity extends AppCompatActivity {
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

    // The recycler view that shows the branch's employees:
    private RecyclerView rvEmployees;

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

        // Load the branch from the given intent:
        this.loadBranchFromIntent();

        // Load the info from the branch:
        this.loadInfoFromBranch();

        // Load the back button callback:
        this.loadBackButtonCallback();
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

        // TODO: Implement an employee online adapter for a recycler view and set it
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