package com.example.finalproject.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.adapters.EmployeeViewsAdapter;
import com.example.finalproject.adapters.ScreenSlideAdapter;
import com.example.finalproject.broadcast_receivers.OnInternetConnectivityChanged;
import com.example.finalproject.custom_views.ShiftView;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.Employee;
import com.example.finalproject.database.online.collections.Shift;
import com.example.finalproject.fragments.shifts.DayShiftsFragment;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ShiftsActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, OnInternetConnectivityChanged {
    // Whether the user can edit the shifts displayed here or not:
    private boolean isEditable;

    // A reference to the online database:
    private FirebaseFirestore db;

    // The shifts that were previously set by another manager and are changed:
    private List<Shift> previousShifts;

    // The branch whose shifts are being set:
    private Branch branch;

    // The dialog that appears when there is no wifi:
    private Dialog noInternetDialog;

    // The date of the first day in the week that's set:
    private LocalDate firstDayDate;

    // The progress bar that will be shown when the activity is loading:
    private ProgressBar pbLoading;

    // The button that allows the user to save the shifts:
    private Button btnSaveShifts;

    // The toolbar shown at the top of the activity:
    private Toolbar toolbar;

    // The tab layout displaying the current day:
    private TabLayout tabLayout;

    // The pager displaying the fragments:
    private ViewPager2 pager;

    // The adapter of the pager:
    private ScreenSlideAdapter pagerAdapter;

    // A list of roles in the branch:
    private ArrayList<String> rolesList;

    // The recycler view that shows all the employees:
    private RecyclerView rvEmployees;

    // A list of employees in the branch:
    private List<Employee> employeeList;

    // An array of fragments for each day of the week:
    private DayShiftsFragment[] fragments;

    // Tag for debugging purposes:
    private static final String TAG = "ShiftsActivity";

    public static void startShiftsActivity(
            Context context, Branch branch, LocalDate startWeek, ArrayList<String> roles,
            boolean isEditable
    ) {
        final Intent intent = new Intent(context, ShiftsActivity.class)
                .putExtra(Constants.ACT_SHIFTS_BRANCH_KEY, branch)
                .putExtra(Constants.ACT_SHIFTS_START_WEEK_KEY, startWeek)
                .putExtra(Constants.ACT_SHIFTS_ROLES_KEY, roles)
                .putExtra(Constants.ACT_SHIFTS_IS_EDITABLE_KEY, isEditable);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shifts);

        // Load the reference to the database:
        this.db = FirebaseFirestore.getInstance();

        // Load the editable attribute from the intent:
        this.loadEditableFromIntent();

        // Load the branch from the intent:
        this.loadBranchFromIntent();

        // Load the date from the intent:
        this.loadDateFromIntent();

        // Load the roles from the intent:
        this.loadRolesFromIntent();

        // Load the views:
        this.rvEmployees = findViewById(R.id.actShiftsRvEmployeeViews);
        this.pbLoading = findViewById(R.id.actShiftsPbLoading);
        this.tabLayout = findViewById(R.id.actShiftsTabLayout);
        this.toolbar = findViewById(R.id.actShiftsToolbar);
        this.pager = findViewById(R.id.actShiftsPager);
        this.btnSaveShifts = findViewById(R.id.actShiftsBtnSaveShifts);

        // Set the title and subtitle for the toolbar:
        this.initToolbar();

        // Set the onClickListener for the save shifts button:
        this.btnSaveShifts.setOnClickListener(_v -> this.saveShifts());

        // If the activity isn't editable, hide the employees recycler view and the save shifts
        // button:
        this.rvEmployees.setVisibility(this.isEditable ? View.VISIBLE : View.GONE);
        this.btnSaveShifts.setVisibility(this.isEditable ? View.VISIBLE : View.GONE);

        // Load until the employees and roles are loaded:
        this.setLoading(true);

        // Load employees:
        this.loadEmployees(() -> {
            // Initialize the employee views' recyclerview:
            this.initEmployeesRv();

            // Initialize fragments:
            this.initDayShiftsFragments();

            // Initialize the adapter:
            this.initPagerAdapter();

            // Initialize tab layout:
            this.initTabLayout();

            // Initialize swipe listener:
            this.initSwipeListener();

            // Load shifts that were already set in the week:
            this.loadPreviousShifts(shiftDocuments -> {
                // Save the previous shifts in a list:
                this.previousShifts = shiftDocuments.getDocuments()
                        .stream()
                        .map(shiftDocument -> shiftDocument.toObject(Shift.class))
                        .collect(Collectors.toList());

                // Set shift views in all fragments:
                this.setPreviousShiftsInFragments();

                // Stop loading:
                this.setLoading(false);
            }, e -> {
                // Initialize an empty "previous shifts" list to avoid null pointer exceptions:
                this.previousShifts = new ArrayList<>(0);

                // Log the error and stop loading:
                Log.e(TAG, "Failed to load previous shifts", e);
                this.setLoading(false);
            });
        });
    }

    private void initToolbar() {
        this.toolbar.setTitle(String.format(Locale.getDefault(), "%s's shifts", this.branch.getCompanyName()));
        final Date date = Util.getDateFromLocalDate(this.firstDayDate);
        this.toolbar.setSubtitle("Sun " + Constants.DATE_FORMAT.format(date));
        this.setSupportActionBar(this.toolbar);

        // Show back button on the toolbar:
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Check for the back button:
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setPreviousShiftsInFragments() {
        // Get the shift views from the previous shifts:
        final Map<LocalDate, List<ShiftView>> dateShiftMap = ShiftView.getShiftViewsFromShifts(
                this, this.previousShifts, this.employeeList, this.rolesList
        );

        // Set the for each fragment:
        for (int i = 0; i < this.fragments.length; i++) {
            final LocalDate currentDate = this.firstDayDate.plusDays(i);
            final List<ShiftView> shiftViews = dateShiftMap.get(currentDate);
            if (shiftViews != null)
                this.fragments[i].setShiftViews(shiftViews);
        }
    }

    private void loadPreviousShifts(OnSuccessListener<QuerySnapshot> onSuccessListener, OnFailureListener onFailureListener) {
        // Make a query of all shifts in this week:
        final Date weekStart = Util.getDateFromLocalDate(this.firstDayDate);
        final Date weekEnd = Util.getDateFromLocalDate(this.firstDayDate.plusWeeks(1));
        this.db.collection("shifts")
                .whereEqualTo(Shift.BRANCH_ID, this.branch.getBranchId())
                .whereGreaterThanOrEqualTo(Shift.STARTING_TIME, weekStart)
                .whereLessThan(Shift.STARTING_TIME, weekEnd)
                .get()
                .addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }

    private void initEmployeesRv() {
        // Set an adapter for it:
        final EmployeeViewsAdapter adapter = new EmployeeViewsAdapter(
                this, this.employeeList,
                (view, employee) -> {
                    // Set the employee as the selected employee in all fragments:
                    for (DayShiftsFragment fragment : this.fragments)
                        fragment.onEmployeeViewSelected(view, employee);
                });
        this.rvEmployees.setAdapter(adapter);

        // Set a horizontal layout manager:
        this.rvEmployees.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false)
        );
    }

    private void setLoading(boolean isLoading) {
        this.pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        this.pager.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        this.tabLayout.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void loadEmployees(Runnable onSuccessRunnable) {
        // Get all employees in the branch:
        this.db.collection(String.format("branches/%s/employees", this.branch.getBranchId())).get()
                .addOnSuccessListener(queryDocuments -> {
                    // Convert the documents to Employee objects:
                    this.employeeList = queryDocuments
                            .getDocuments()
                            .stream()
                            .map(document -> document.toObject(Employee.class))
                            .collect(Collectors.toList());

                    // Run the callback:
                    onSuccessRunnable.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Couldn't load employees", e);
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void initDayShiftsFragments() {
        this.fragments = new DayShiftsFragment[7];
        for (int i = 0; i < this.fragments.length; i++) {
            this.fragments[i] = DayShiftsFragment.newInstance(
                    this.branch.getDailyShiftsNum().get(i), this.firstDayDate.plusDays(i),
                    this.branch, this.rolesList, this.isEditable
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
                ((tab, position) -> tab.setText(
                        // Adjust for 1 meaning Monday in DayOfWeek:
                        DayOfWeek.of(position == 0 ? 7 : position).getDisplayName(TextStyle.SHORT, Locale.US)
                ))
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

    private void loadEditableFromIntent() {
        final Intent intent = getIntent();
        if (intent != null)
            this.isEditable = intent.getBooleanExtra(Constants.ACT_SHIFTS_IS_EDITABLE_KEY, false);
    }

    private void loadBranchFromIntent() {
        final Intent intent = getIntent();
        if (intent != null) {
            final Serializable branch = intent.getSerializableExtra(Constants.ACT_SHIFTS_BRANCH_KEY);
            if (branch instanceof Branch)
                this.branch = (Branch) branch;
        }
    }

    private void loadDateFromIntent() {
        final Intent intent = getIntent();
        if (intent != null) {
            final Serializable firstDaySer = intent.getSerializableExtra(Constants.ACT_SHIFTS_START_WEEK_KEY);
            if (firstDaySer instanceof LocalDate)
                this.firstDayDate = (LocalDate) firstDaySer;
            else {
                if (firstDaySer != null)
                    Log.e(TAG, "Invalid date given. Expected LocalDate, got " + firstDaySer.getClass().getName());
                else
                    Log.e(TAG, "No date was given");
                finish();
            }
        }
    }

    private void loadRolesFromIntent() {
        final Intent intent = getIntent();
        if (intent != null) {
            this.rolesList = intent.getStringArrayListExtra(Constants.ACT_SHIFTS_ROLES_KEY);
        }
    }

    private void saveShifts() {
        // Show the progress bar and hide the save shifts button:
        this.setLoading(true);

        // Configure callbacks:
        OnSuccessListener<Void> onSuccessListener = unused -> {
            Toast.makeText(this, "All shifts were saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        };
        OnFailureListener onFailureListener = e -> {
            // Log the error and alert the user:
            Log.e(TAG, "Failed to delete shifts", e);
            Toast.makeText(this, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
            this.setLoading(false);
        };

        // Create a batch write:
        final WriteBatch batch = this.db.batch();

        // Delete the previous shifts that were loaded before:
        DocumentReference shiftRef;
        for (Shift shift : this.previousShifts) {
            shiftRef = this.db.collection("shifts").document(shift.getShiftId());
            batch.delete(shiftRef);
        }

        // Add the new shifts under new IDs:
        final CollectionReference shiftsRef = this.db.collection("shifts");
        for (DayShiftsFragment fragment : this.fragments) {
            final List<Shift> shifts = fragment.getPackagedShifts();
            for (Shift shift : shifts) {
                // Create a new reference and set the ID:
                shiftRef = shiftsRef.document();
                shift.setShiftId(shiftRef.getId());
                batch.set(shiftRef, shift);
            }
        }

        // Commit the batch:
        batch.commit().addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        // Set the toolbar:
        final LocalDate date = this.firstDayDate.plusDays(tab.getPosition());
        this.toolbar.setSubtitle(tab.getText() + " " + Constants.DATE_FORMAT.format(Util.getDateFromLocalDate(date)));

        // Move the view pager:
        this.pager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onInternetAvailable() {
        // Create the dialog if it wasn't created already:
        if (this.noInternetDialog == null)
            this.noInternetDialog = Util.getNoInternetDialog(this);
        this.noInternetDialog.dismiss();
    }

    @Override
    public void onInternetUnavailable() {
        // Create the dialog if it wasn't created already:
        if (this.noInternetDialog == null)
            this.noInternetDialog = Util.getNoInternetDialog(this);
        this.noInternetDialog.show();
    }
}