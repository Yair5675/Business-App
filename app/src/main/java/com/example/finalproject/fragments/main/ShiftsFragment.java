package com.example.finalproject.fragments.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.adapters.OnlineUserShiftsAdapter;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.database.online.collections.UserShift;
import com.example.finalproject.util.WrapperLinearLayoutManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ShiftsFragment extends Fragment {
    // The user whose shifts are displayed:
    private User user;

    // The recycler view that shows the shifts:
    private RecyclerView rvUserShifts;

    // The text view that appears in case there are no shifts:
    private TextView tvNoShifts;

    public ShiftsFragment() {
        // Required empty public constructor
    }

    public static ShiftsFragment newInstance(User user) {
        final ShiftsFragment fragment = new ShiftsFragment();
        fragment.user = user;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        // Get a reference to the database:
        final FirebaseFirestore dbRef = FirebaseFirestore.getInstance();

        // Create the recyclerView's options:
        final Query query = dbRef
                .collectionGroup("user_shifts")
                .whereEqualTo("uid", this.user.getUid());
        FirestoreRecyclerOptions<UserShift> options = new FirestoreRecyclerOptions.Builder<UserShift>()
                .setLifecycleOwner(this)
                .setQuery(query, UserShift.class)
                .build();

        // Create the adapter and set the options:
        final OnlineUserShiftsAdapter adapter = new OnlineUserShiftsAdapter(
                requireContext(),
                () -> {
                    this.rvUserShifts.setVisibility(View.GONE);
                    this.tvNoShifts.setVisibility(View.VISIBLE);
                },
                () -> {
                    this.rvUserShifts.setVisibility(View.VISIBLE);
                    this.tvNoShifts.setVisibility(View.GONE);
                },
                options
        );

        // Set the adapter for the recycler view:
        this.rvUserShifts.setAdapter(adapter);
    }
}