package com.example.finalproject.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.adapters.online.OnlineShiftsAdapter;
import com.example.finalproject.database.online.collections.Shift;
import com.example.finalproject.dialogs.MonthPickerDialog;
import com.example.finalproject.util.WrapperLinearLayoutManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ShiftsHistoryActivity extends AppCompatActivity implements View.OnClickListener {
    // The ID of the user whose shifts are shown (mandatory):
    private String uid;

    // A reference to the online database:
    private FirebaseFirestore db;

    // The ID of the branch whose shifts are shown (optional, will show all shifts of the user if
    // not given):
    private @Nullable String branchId;

    // The dialog that allows the user to choose a specific month for shifts
    private MonthPickerDialog monthPickerDialog;

    // The button that enables the user to select a specific month:
    private Button btnSelectMonth;

    // The recycler view that shows all the shifts:
    private RecyclerView rvShifts;

    // The text view that displays the month which is currently shown (or all times):
    private TextView tvShowingMonth;

    // The text view that appears when there are no recorded shifts:
    private TextView tvNoShiftsFound;

    // The adapter of the shifts recycler view:
    private OnlineShiftsAdapter adapter;

    // The image view that allows the user to deselect their month selection:
    private ImageView imgCancelSelection;

    // A constant indicating the entire shift history should be shown:
    private static final int ALL_TIMES = -1;

    // Keys for the intent:
    private static final String UID_INTENT_KEY = "uid";
    private static final String BRANCH_ID_KEY = "branchId";

    // Tag for debugging purposes:
    private static final String TAG = "ShiftsHistoryActivity";

    /**
     * Starts the shifts history activity from another activity.
     * @param context The context of the calling activity, necessary for moving between them.
     * @param uid The ID of the use whose shifts are displayed. This parameter is mandatory.
     * @param branchId The ID of the branch whose shifts are shown. This parameter is optional,
     *                 and if null the activity will present all shifts of the user.
     */
    public static void startActivity(@NonNull Context context, @NonNull String uid, @Nullable String branchId) {
        // Put the values in the intent:
        final Intent intent = new Intent(context, ShiftsHistoryActivity.class);
        intent.putExtra(UID_INTENT_KEY, uid);
        if (branchId != null)
            intent.putExtra(BRANCH_ID_KEY, branchId);

        // Start the activity with the intent:
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shifts_history);

        // Load the uid and branch ID from the intent:
        this.loadInfoFromIntent();

        // Initialize the database reference:
        this.db = FirebaseFirestore.getInstance();

        // Load the views:
        this.rvShifts = findViewById(R.id.actShiftsHistoryRvShifts);
        this.btnSelectMonth = findViewById(R.id.actShiftsHistoryBtnSelectMonth);
        this.tvShowingMonth = findViewById(R.id.actShiftsHistoryTvShowingMonth);
        this.tvNoShiftsFound = findViewById(R.id.actShiftsHistoryTvNoShiftsFound);
        this.imgCancelSelection = findViewById(R.id.actShiftsHistoryImgCancelSelection);

        // Set an onClickListener for the cancel selection image and selection button:
        this.btnSelectMonth.setOnClickListener(this);
        this.imgCancelSelection.setOnClickListener(this);

        // Initialize the adapter:
        this.initAdapter();

        // Initialize the month picker dialog:
        this.initMonthPicker();

        // Listen to the newest and oldest shift in order to determine the minimal and maximal
        // month:
        this.listenToNewestShift();
        this.listenToOldestShift();

        // Set layout manager for the recycler view:
        this.rvShifts.setLayoutManager(new WrapperLinearLayoutManager(this));

        // Initialize the month and year to show the entire history:
        this.setSelectedMonth(ALL_TIMES, ALL_TIMES);
    }

    private void listenToNewestShift() {
        this.getRelevantShiftDocumentsQuery().limit(1).addSnapshotListener(this, (value, error) -> {
            if (error != null) {
                Log.e(TAG, "Failed to listen to newest shift", error);
                return;
            }
            if (value == null) {
                Log.e(TAG, "Query snapshot value is null without an error");
                return;
            }

            // Get the first shift (should be the only one):
            if (!value.isEmpty()) {
                final Shift shift = value.getDocuments().get(0).toObject(Shift.class);
                if (shift == null) {
                    Log.e(TAG, "Failed to convert document to shift");
                    return;
                }

                // Change the maximum pick in the month picker:
                final Calendar calendar = Calendar.getInstance();
                calendar.setTime(shift.getStartingTime());
                final int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH);
                this.monthPickerDialog.setMaxYear(year);
                this.monthPickerDialog.setMaxMonth(month);
            }
        });
    }

    private void listenToOldestShift() {
        this.getRelevantShiftDocumentsQuery().limitToLast(1).addSnapshotListener(this, (value, error) -> {
            if (error != null) {
                Log.e(TAG, "Failed to listen to oldest shift", error);
                return;
            }
            if (value == null) {
                Log.e(TAG, "Query snapshot value is null without an error");
                return;
            }

            // Get the first shift (should be the only one):
            if (!value.isEmpty()) {
                final Shift shift = value.getDocuments().get(0).toObject(Shift.class);
                if (shift == null) {
                    Log.e(TAG, "Failed to convert document to shift");
                    return;
                }

                // Change the minimum pick in the month picker:
                final Calendar calendar = Calendar.getInstance();
                calendar.setTime(shift.getStartingTime());
                final int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH);
                this.monthPickerDialog.setMinYear(year);
                this.monthPickerDialog.setMinMonth(month);
            }
        });
    }

    private void initMonthPicker() {
        // Create the month picker:
        this.monthPickerDialog = new MonthPickerDialog();

        // Get next week's year and month and set it as the maximum:
        final LocalDate nextWeek = LocalDate.now().plusWeeks(1);
        final int maxYear = nextWeek.getYear(), maxMonth = nextWeek.getMonthValue() - 1; // 1 is January so fix it
        this.monthPickerDialog.setMaxYear(maxYear);
        this.monthPickerDialog.setMaxMonth(maxMonth);

        // Configure the listener:
        this.monthPickerDialog.setOnMonthSelectedListener(this::setSelectedMonth);
    }

    private Query getRelevantShiftDocumentsQuery() {
        final Query query;
        if (this.branchId == null)
            query = this.db.collection("shifts")
                    .whereEqualTo(Shift.UID, this.uid)
                    .orderBy(Shift.STARTING_TIME, Query.Direction.DESCENDING);
        else
            query = this.db.collection("shifts")
                    .whereEqualTo(Shift.UID, this.uid)
                    .whereEqualTo(Shift.BRANCH_ID, this.branchId)
                    .orderBy(Shift.STARTING_TIME, Query.Direction.DESCENDING);
        return query;
    }

    private void initAdapter() {
        // Make the query (show all shifts initially):
        final Query query = this.getRelevantShiftDocumentsQuery();

        // Form the adapter options:
        final FirestoreRecyclerOptions<Shift> options = new FirestoreRecyclerOptions.Builder<Shift>()
                .setLifecycleOwner(this)
                .setQuery(query, Shift.class)
                .build();

        // Initialize the adapter and set it to the recycler view;
        this.adapter = new OnlineShiftsAdapter(
                true, this,
                () -> {
                    this.rvShifts.setVisibility(View.GONE);
                    this.tvNoShiftsFound.setVisibility(View.VISIBLE);
                    // If we are showing shifts from all times and it's still empty, hide the select
                    // month button:
                    if (this.imgCancelSelection.getVisibility() == View.GONE)
                        this.btnSelectMonth.setVisibility(View.GONE);
                },
                () -> {
                    this.rvShifts.setVisibility(View.VISIBLE);
                    this.tvNoShiftsFound.setVisibility(View.GONE);
                    this.btnSelectMonth.setVisibility(View.VISIBLE);
                }, options
        );
        this.rvShifts.setAdapter(this.adapter);
    }

    private void loadInfoFromIntent() {
        // Get the intent:
        final Intent intent = getIntent();

        // Get the UID:
        this.uid = intent.getStringExtra(UID_INTENT_KEY);
        if (this.uid == null) {
            Log.e(TAG, "Uid not given");
            finish();
            return;
        }

        // Get the branch ID:
        this.branchId = intent.getStringExtra(BRANCH_ID_KEY);
    }

    /**
     * Sets the selected month displayed in the activity. If either parameter is equal to the value
     * of ALL_TIMES, no specific month is selected.
     * @param year The year of the shifts that will be displayed.
     * @param month The specific month of the shifts that will be displayed. The value should be 0
     *              based (January is 0 and December is 11).
     */
    private void setSelectedMonth(int year, int month) {
        // If the user wants to show every shift:
        if (month == ALL_TIMES || year == ALL_TIMES) {
            this.tvShowingMonth.setText(R.string.act_shifts_history_showing_all_times);
            this.imgCancelSelection.setVisibility(View.GONE);

            // Show every shift:
            this.showAllShifts();
        }
        else {
            // Show the month and year displayed:
            final String monthName = Month.of(month + 1).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            final String msg = String.format(Locale.getDefault(), "Showing: %s. %d", monthName, year);
            this.tvShowingMonth.setText(msg);

            // Show the cancel image:
            this.imgCancelSelection.setVisibility(View.VISIBLE);

            // Update the adapter:
            this.showSpecificMonth(year, month);
        }
    }

    private void showAllShifts() {
        final Query query = this.getRelevantShiftDocumentsQuery();

        // Update the adapter options:
        final FirestoreRecyclerOptions<Shift> options = new FirestoreRecyclerOptions.Builder<Shift>()
                .setLifecycleOwner(this)
                .setQuery(query, Shift.class)
                .build();

        this.adapter.updateOptions(options);
    }

    private void showSpecificMonth(int year, int month) {
        // Get the dates representing the start and end of the month:
        final Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, 1, 0, 0); // Month should be 0 based
        final Date startMonth = calendar.getTime();
        calendar.set(Calendar.MONTH, (month + 1) % 12);
        final Date endMonth = calendar.getTime();

        // Create a query with theses dates:
        final Query query = this.getRelevantShiftDocumentsQuery()
                .whereGreaterThanOrEqualTo(Shift.STARTING_TIME, startMonth)
                .whereLessThan(Shift.STARTING_TIME, endMonth);

        // Update the adapter options:
        final FirestoreRecyclerOptions<Shift> options = new FirestoreRecyclerOptions.Builder<Shift>()
                .setLifecycleOwner(this)
                .setQuery(query, Shift.class)
                .build();

        this.adapter.updateOptions(options);
    }

    @Override
    public void onClick(View view) {
        final int ID = view.getId();

        if (ID == this.imgCancelSelection.getId()) {
            this.setSelectedMonth(ALL_TIMES, ALL_TIMES);
        }
        else if (ID == this.btnSelectMonth.getId()) {
            // Activate the month picker dialog:
            this.monthPickerDialog.show(getSupportFragmentManager(), "monthPicker");
        }
    }
}