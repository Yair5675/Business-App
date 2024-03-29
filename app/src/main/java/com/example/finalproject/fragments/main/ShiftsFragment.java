package com.example.finalproject.fragments.main;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.adapters.online.OnlineShiftsAdapter;
import com.example.finalproject.database.online.collections.Shift;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.util.WrapperLinearLayoutManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

public class ShiftsFragment extends Fragment {
    // The user whose shifts are displayed:
    private User user;

    // A reference to the online database:
    private final FirebaseFirestore db;

    // The recycler view that shows the shifts:
    private RecyclerView rvUserShifts;

    // The adapter of the recycler view:
    private OnlineShiftsAdapter adapter;

    // The count down timer that will refresh the adapter to clear shifts in real time:
    private @Nullable CountDownTimer shiftRefreshTimer;

    // The text view that appears in case there are no shifts:
    private TextView tvNoShifts;

    public ShiftsFragment() {
        // Required empty public constructor
        this.db = FirebaseFirestore.getInstance();
    }

    public static ShiftsFragment newInstance(User user) {
        final ShiftsFragment fragment = new ShiftsFragment();
        fragment.user = user;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View parent = inflater.inflate(R.layout.fragment_main_shifts, container, false);

        // Load the views:
        this.rvUserShifts = parent.findViewById(R.id.fragMainShiftsRvShifts);
        this.tvNoShifts = parent.findViewById(R.id.fragMainShiftsTvNoShifts);

        // Set linear layout manager:
        this.rvUserShifts.setLayoutManager(new WrapperLinearLayoutManager(requireContext()));

        // Initialize the adapter:
        if (user != null)
            this.initAdapter();

        return parent;
    }

    public void setUser(User user) {
        this.user = user;
    }

    private void initAdapter() {
        // Create a query that shows all future shifts that haven't happened yet, sorted by closest:
        final Date now = new Date();
        final Query query = this.db
                .collection("shifts")
                .whereEqualTo(Shift.UID, this.user.getUid())
                .whereGreaterThan(Shift.STARTING_TIME, now)
                .orderBy(Shift.STARTING_TIME);

        // Create the firestore options and add to the adapter:
        final FirestoreRecyclerOptions<Shift> options = new FirestoreRecyclerOptions.Builder<Shift>()
                .setLifecycleOwner(this)
                .setQuery(query, Shift.class)
                .build();
        this.adapter = new OnlineShiftsAdapter(
                true, requireContext(),
                () -> {
                    this.rvUserShifts.setVisibility(View.GONE);
                    this.tvNoShifts.setVisibility(View.VISIBLE);

                    // Cancel the timer:
                    if (this.shiftRefreshTimer != null) {
                        this.shiftRefreshTimer.cancel();
                        this.shiftRefreshTimer = null;
                    }
                    },
                () -> {
                    this.rvUserShifts.setVisibility(View.VISIBLE);
                    this.tvNoShifts.setVisibility(View.GONE);

                    // Schedule the time to refresh the adapter:
                    this.scheduleAdapterRefresh(this.adapter.getItem(0));
                    },
                options);
        this.rvUserShifts.setAdapter(this.adapter);
    }

    private void scheduleAdapterRefresh(Shift closestShift) {
        // Cancel the current timer if it exists:
        if (this.shiftRefreshTimer != null) {
            this.shiftRefreshTimer.cancel();
            this.shiftRefreshTimer = null;
        }

        // Get the difference in millis:
        final Date now = new Date();
        final long diff = closestShift.getStartingTime().getTime() - now.getTime();

        // Schedule a refresh:
        if (diff > 0) {
            this.shiftRefreshTimer = new CountDownTimer(diff, diff) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    refreshAdapter();

                    // Schedule the next refresh:
                    if (!adapter.isEmpty())
                        scheduleAdapterRefresh(adapter.getItem(0));
                }
            }.start();
        }

    }

    private void refreshAdapter() {
        // Update the adapter's date filter:
        final Query query = this.db
                .collection("shifts")
                .whereEqualTo(Shift.UID, this.user.getUid())
                .whereGreaterThan(Shift.STARTING_TIME, new Date())
                .orderBy(Shift.STARTING_TIME);
        final FirestoreRecyclerOptions<Shift> options = new FirestoreRecyclerOptions.Builder<Shift>()
                .setLifecycleOwner(this)
                .setQuery(query, Shift.class)
                .build();

        this.adapter.updateOptions(options);
    }
}