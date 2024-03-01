package com.example.finalproject.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.adapters.ScreenSlideAdapter;
import com.example.finalproject.custom_views.ShiftView;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.Employee;
import com.example.finalproject.database.online.collections.Shift;
import com.example.finalproject.database.online.collections.Worker;
import com.example.finalproject.fragments.shifts.DayShiftsFragment;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Date;
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

        // Load the reference to the database:
        this.db = FirebaseFirestore.getInstance();

        // Load the branch from the intent:
        this.loadBranchFromIntent();

        // Load the date from the intent:
        this.loadDateFromIntent();

        // Load the roles from the intent:
        this.loadRolesFromIntent();

        // Load the views:
        this.pbLoading = findViewById(R.id.actShiftsPbLoading);
        this.tabLayout = findViewById(R.id.actShiftsTabLayout);
        this.toolbar = findViewById(R.id.actShiftsToolbar);
        this.pager = findViewById(R.id.actShiftsPager);
        this.btnSaveShifts = findViewById(R.id.actShiftsBtnSaveShifts);

        // Set the title and subtitle for the toolbar:
        this.toolbar.setTitle(String.format(Locale.getDefault(), "%s's shifts", this.branch.getCompanyName()));
        final Date date = Util.getDateFromLocalDate(this.firstDayDate);
        this.toolbar.setSubtitle("Sun " + Constants.DATE_FORMAT.format(date));

        // Set the onClickListener for the save shifts button:
        this.btnSaveShifts.setOnClickListener(_v -> this.saveShifts());

        // Load until the employees and roles are loaded:
        this.setLoading(true);

        // Load employees:
        this.loadEmployees(() -> {
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

    private void initDayShiftsFragments() {
        this.fragments = new DayShiftsFragment[7];
        for (int i = 0; i < this.fragments.length; i++) {
            this.fragments[i] = DayShiftsFragment.newInstance(
                    this, i, this.firstDayDate.plusDays(i), this.branch, this.employeeList, this.rolesList
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

    private void loadRolesFromIntent() {
        final Intent intent = getIntent();
        if (intent != null) {
            this.rolesList = intent.getStringArrayListExtra("roles");
        }
    }

    private void saveShifts() {
        // Show the progress bar and hide the save shifts button:
        this.pbLoading.setVisibility(View.VISIBLE);
        this.btnSaveShifts.setVisibility(View.GONE);

        // Delete all shifts:
        this.deleteAllShifts(unused -> {
            // Go over every shifts fragment:
            for (DayShiftsFragment fragment : this.fragments) {
                // Get the shifts summary:
                final List<ShiftView.PackagedShift> packagedShifts = fragment.getPackagedShifts();

                // Save them:
                this.saveShiftsRecursive(packagedShifts, 0);
            }
        }, e -> {
            // Log the error and alert the user:
            Log.e(TAG, "Failed to delete shifts", e);
            Toast.makeText(this, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
            this.btnSaveShifts.setVisibility(View.VISIBLE);
            this.pbLoading.setVisibility(View.GONE);
        });

    }

    private void deleteAllShifts(
            OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
       deleteAllShifts(0, onSuccessListener, onFailureListener);
    }

    private void deleteAllShifts(int index, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        if (index == 7)
            onSuccessListener.onSuccess(null);

        final Date date = Util.getDateFromLocalDate(this.firstDayDate.plusDays(index));
        // Get all shifts from the branch at the date:
        this.db
                .collection(String.format("branches/%s/shifts", this.branch.getBranchId()))
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(shiftDocs -> {
                    // Create a write batch:
                    final WriteBatch batch = this.db.batch();
                    for (DocumentSnapshot shiftDoc : shiftDocs)
                        batch.delete(shiftDoc.getReference());
                    batch.commit().addOnSuccessListener(unused -> deleteAllShifts(index + 1, onSuccessListener, onFailureListener)).addOnFailureListener(onFailureListener);
                })
                .addOnFailureListener(onFailureListener);
    }

    private void saveShiftsRecursive(List<ShiftView.PackagedShift> packagedShifts, int index) {
        // Check that all shifts were saved:
        if (index >= packagedShifts.size()) {
            Toast.makeText(this, "All shifts were saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Skip over shifts without employees:
        final ShiftView.PackagedShift currentShift = packagedShifts.get(index);
        if (currentShift.WORKERS.isEmpty())
            saveShiftsRecursive(packagedShifts, index + 1);
        else
            // Save the current shift:
            this.saveShift(
                    currentShift,
                    unused -> saveShiftsRecursive(packagedShifts, index + 1),
                    e -> {
                        // Log the error and alert the user:
                        Log.e(TAG, "Failed to save shift", e);
                        Toast.makeText(this, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
                        this.btnSaveShifts.setVisibility(View.VISIBLE);
                        this.pbLoading.setVisibility(View.GONE);
                    }
            );
    }

    private void saveShift(
            ShiftView.PackagedShift packagedShift,
            OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // TODO: Change the way shifts are saved according to the new design
        // Create the shift object from the packaged shift:
        final Shift shift = new Shift();
        shift.setStartingTime(packagedShift.STARTING_TIME);
        shift.setEndingTime(packagedShift.ENDING_TIME);
        shift.setCompanyName(this.branch.getCompanyName());
        shift.setBranchId(this.branch.getBranchId());

        // Create a new batch:
        final WriteBatch batch = this.db.batch();

        // Save the shift object in the database with an auto-generated ID:
        final DocumentReference shiftRef = this.db.collection(String.format(
                "branches/%s/shifts", this.branch.getBranchId()
        )).document();
        shift.setShiftId(shiftRef.getId());
        batch.set(shiftRef, shift, SetOptions.merge());

        // Set the workers in the database:
        for (Worker worker : packagedShift.WORKERS) {
            DocumentReference workerRef = shiftRef.collection("workers").document(worker.getUid());
            batch.set(workerRef, worker, SetOptions.merge());
        }

        // Commit the branch:
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
}