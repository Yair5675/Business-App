package com.example.finalproject.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.finalproject.R;
import com.example.finalproject.adapters.ScreenSlideAdapter;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.Employee;
import com.example.finalproject.fragments.shifts.DayShiftsFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ShiftsActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {
    // A reference to the online database:
    private FirebaseFirestore db;

    // The branch whose shifts are being set:
    private Branch branch;

    // The date of the first day in the week that's set:
    private LocalDate firstDayDate;

    // The progress bar that will be shown when the activity is loading:
    private ProgressBar pbLoading;

    // The toolbar shown at the top of the activity:
    private Toolbar toolbar;

    // The tab layout displaying the current day:
    private TabLayout tabLayout;

    // The pager displaying the fragments:
    private ViewPager2 pager;

    // The adapter of the pager:
    private ScreenSlideAdapter pagerAdapter;

    // A list of roles in the branch:
    private List<String> rolesList;

    // A list of employees in the branch:
    private List<Employee> employeeList;

    // An array of fragments for each day of the week:
    private DayShiftsFragment[] fragments;

    // Tag for debugging purposes:
    private static final String TAG = "ShiftsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shifts);

        // Load the branch from the intent:
        this.loadBranchFromIntent();

        // Load the date from the intent:
        this.loadDateFromIntent();

        // Load the views:
        this.pbLoading = findViewById(R.id.actShiftsPbLoading);
        this.tabLayout = findViewById(R.id.actShiftsTabLayout);
        this.toolbar = findViewById(R.id.actShiftsToolbar);
        this.pager = findViewById(R.id.actShiftsPager);

        // Set the title for the toolbar:
        this.toolbar.setTitle(String.format(Locale.getDefault(), "%s's shifts", this.branch.getCompanyName()));

        // Load until the employees and roles are loaded:
        this.setLoading(true);

        // Load employees:
        this.loadEmployees(() -> {
            // Load roles:
            this.loadRoles(() -> {
                // Initialize fragments:
                this.initDayShiftsFragments();

                // Initialize the adapter:
                this.initPagerAdapter();

                // Initialize tab layout:
                this.initTabLayout();

                // Initialize swipe listener:
                this.initSwipeListener();

                // Stop loading:
                this.setLoading(false);
            });
        });
    }

    private void setLoading(boolean isLoading) {
        this.pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        this.pager.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        this.tabLayout.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void loadEmployees(Runnable onSuccessRunnable) {
        // Get all employees in the branch:
        this.db = FirebaseFirestore.getInstance();
        db.collection(String.format("branches/%s/employees", this.branch.getBranchId())).get()
                .addOnSuccessListener(queryDocuments -> {
                    // Convert the documents to Employee objects:
                    final List<Employee> employees = new LinkedList<>();
                    for (QueryDocumentSnapshot document : queryDocuments) {
                        final Employee employee = document.toObject(Employee.class);
                        employees.add(employee);
                    }
                    this.employeeList = employees;

                    // Run the callback:
                    onSuccessRunnable.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Couldn't load employees", e);
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadRoles(Runnable onSuccessRunnable) {
        // Get all roles in the branch:
        this.db.collection(String.format("branches/%s/roles", this.branch.getBranchId())).get()
                .addOnSuccessListener(queryDocuments -> {
                    // Get role name through the documents' ID:
                    final List<String> roles = new LinkedList<>();
                    for (QueryDocumentSnapshot document : queryDocuments)
                        roles.add(document.getId());
                    this.rolesList = roles;

                    // Run the callback:
                    onSuccessRunnable.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Couldn't load roles", e);
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void initDayShiftsFragments() {
        this.fragments = new DayShiftsFragment[7];
        for (int i = 0; i < this.fragments.length; i++) {
            this.fragments[i] = DayShiftsFragment.newInstance(
                    this, i, this.branch, this.employeeList, this.rolesList
            );
        }
    }

    private void initPagerAdapter() {
        this.pagerAdapter = new ScreenSlideAdapter(this, this.fragments);
        this.pager.setAdapter(this.pagerAdapter);
    }

    private void initTabLayout() {
        new TabLayoutMediator(this.tabLayout, this.pager,
                // Set to the day of the week:
                ((tab, position) -> tab.setText(DayOfWeek.of(position).getDisplayName(TextStyle.SHORT, Locale.US)))
        ).attach();
    }

    private void initSwipeListener() {
        this.tabLayout.addOnTabSelectedListener(this);
        this.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Set the tab layout:
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    private void loadBranchFromIntent() {
        final Intent intent = getIntent();
        if (intent != null) {
            final Serializable branch = intent.getSerializableExtra("branch");
            if (branch instanceof Branch)
                this.branch = (Branch) branch;
        }
    }

    private void loadDateFromIntent() {
        final Intent intent = getIntent();
        if (intent != null) {
            final int day = intent.getIntExtra("day", 0);
            final int month = intent.getIntExtra("month", 0);
            final int year = intent.getIntExtra("year", 0);
            this.firstDayDate = LocalDate.of(year, month, day);
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        // Set the toolbar:
        final LocalDate date = this.firstDayDate.plusDays(tab.getPosition());
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(tab.getText() + " d/M/y");
        this.toolbar.setSubtitle(formatter.format(date));

        // Move the view pager:
        this.pager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}