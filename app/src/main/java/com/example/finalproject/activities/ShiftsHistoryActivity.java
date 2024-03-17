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
import com.example.finalproject.util.WrapperLinearLayoutManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ShiftsHistoryActivity extends AppCompatActivity {
    // The ID of the user whose shifts are shown (mandatory):
    private String uid;

    // A reference to the online database:
    private FirebaseFirestore db;

    // The ID of the branch whose shifts are shown (optional, will show all shifts of the user if
    // not given):
    private @Nullable String branchId;

    // The month and year selected to be shown (equal to the ALL_TIMES constant if they weren't
    // selected):
    private int month, year;

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

        // TODO: Add a month picker

        // Load the views:
        this.rvShifts = findViewById(R.id.actShiftsHistoryRvShifts);
        this.btnSelectMonth = findViewById(R.id.actShiftsHistoryBtnSelectMonth);
        this.tvShowingMonth = findViewById(R.id.actShiftsHistoryTvShowingMonth);
        this.imgCancelSelection = findViewById(R.id.actShiftsHistoryImgCancelSelection);

        // Initialize the adapter:
        this.initAdapter();

        // Set layout manager for the recycler view:
        this.rvShifts.setLayoutManager(new WrapperLinearLayoutManager(this));

        // Initialize the month and year to show the entire history:
        this.setSelectedTime(ALL_TIMES, ALL_TIMES);
    }

    private void initAdapter() {
        // Make the query (show all shifts initially):
        final Query query;
        if (this.branchId == null)
            query = this.db.collection("shifts")
                    .whereEqualTo(Shift.UID, this.uid)
                    .orderBy(Shift.SHIFT_DATE);
        else
            query = this.db.collection("shifts")
                    .whereEqualTo(Shift.UID, this.uid)
                    .whereEqualTo(Shift.BRANCH_ID, this.branchId)
                    .orderBy(Shift.SHIFT_DATE);

        // Form the adapter options:
        final FirestoreRecyclerOptions<Shift> options = new FirestoreRecyclerOptions.Builder<Shift>()
                .setLifecycleOwner(this)
                .setQuery(query, Shift.class)
                .build();

        // Initialize the adapter and set it to the recycler view;
        this.adapter = new OnlineShiftsAdapter(
                true, false, this,
                () -> {
                    this.rvShifts.setVisibility(View.GONE);
                    this.tvNoShiftsFound.setVisibility(View.VISIBLE);
                },
                () -> {
                    this.rvShifts.setVisibility(View.VISIBLE);
                    this.tvNoShiftsFound.setVisibility(View.VISIBLE);
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

    private void setSelectedTime(int month, int year) {
        // TODO: Update the adapter
        // If the user wants to show every shift:
        if (month == ALL_TIMES || year == ALL_TIMES) {
            this.tvShowingMonth.setText(R.string.act_shifts_history_showing_all_times);
            this.imgCancelSelection.setVisibility(View.GONE);
        }
        else {
            // Save the month and year:
            this.month = month;
            this.year = year;

            // TODO: Get the month in text and set the showing month text view

            // Show the cancel image:
            this.imgCancelSelection.setVisibility(View.VISIBLE);
        }
    }
}