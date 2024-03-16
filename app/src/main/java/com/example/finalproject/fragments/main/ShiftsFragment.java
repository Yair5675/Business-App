package com.example.finalproject.fragments.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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
        final Query query = this.db
                .collection("shifts")
                .whereEqualTo(Shift.UID, this.user.getUid())
                .whereGreaterThanOrEqualTo(Shift.SHIFT_DATE, new Date())
                .orderBy(Shift.SHIFT_DATE);

        // Create the firestore options and add to the adapter:
        // TODO: Hide shifts which have passed today in real time
        final FirestoreRecyclerOptions<Shift> options = new FirestoreRecyclerOptions.Builder<Shift>()
                .setLifecycleOwner(this)
                .setQuery(query, Shift.class)
                .build();
        this.adapter = new OnlineShiftsAdapter(
                true, requireContext(),
                () -> {
                    this.rvUserShifts.setVisibility(View.GONE);
                    this.tvNoShifts.setVisibility(View.VISIBLE);
                    },
                () -> {
                    this.rvUserShifts.setVisibility(View.VISIBLE);
                    this.tvNoShifts.setVisibility(View.GONE);
                    },
                options);
        this.rvUserShifts.setAdapter(this.adapter);
    }
}