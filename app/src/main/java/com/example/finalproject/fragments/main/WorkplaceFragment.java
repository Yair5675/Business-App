package com.example.finalproject.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.adapters.online.OnlineWorkplacesAdapter;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.database.online.collections.Workplace;
import com.example.finalproject.util.Util;
import com.example.finalproject.util.WrapperLinearLayoutManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.Serializable;

public class WorkplaceFragment extends Fragment implements SearchView.OnQueryTextListener {
    // The connected user:
    private User user;

    // A reference to the online database:
    private final FirebaseFirestore db;

    // The text view that appears when there are no results in the adapter:
    private TextView tvNoWorkplaces;

    // The recycler view that shows the workplaces to the user:
    private RecyclerView rvWorkplaces;

    // The adapter of the recycler view:
    private OnlineWorkplacesAdapter adapter;

    // Keys for the fragment's arguments bundle:
    private static final String USER_ARG_KEY = "user";

    public WorkplaceFragment() {
        // Public constructor required for fragments, only initializes the database reference:
        this.db = FirebaseFirestore.getInstance();
    }

    public static WorkplaceFragment newInstance(User user) {
        // Initialize the fragment:
        final WorkplaceFragment fragment = new WorkplaceFragment();

        // Set the user as an argument of the fragment:
        final Bundle args = new Bundle();
        args.putSerializable(USER_ARG_KEY, user);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the arguments:
        final Bundle args = getArguments();
        if (args != null) {
            final Serializable userSer = args.getSerializable(USER_ARG_KEY);
            if (userSer instanceof User)
                this.user = (User) userSer;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View parent = inflater.inflate(R.layout.fragment_main_workplaces, container, false);

        // Load the views:
        this.tvNoWorkplaces = parent.findViewById(R.id.fragMainWorkplacesTvNoWorkplacesFound);
        final SearchView svWorkplaces = parent.findViewById(R.id.fragMainWorkplacesSvWorkplaces);
        this.rvWorkplaces = parent.findViewById(R.id.fragMainWorkplacesRvWorkplaces);

        // Initialize the adapter:
        if (this.user != null)
            initAdapter();

        // Initialize a layout manager for the recycler view:
        this.rvWorkplaces.setLayoutManager(new WrapperLinearLayoutManager(requireContext()));

        // Set the current fragment as a query listener for the search view:
        svWorkplaces.setOnQueryTextListener(this);
        return parent;
    }

    public void setUser(User user) {
        this.user = user;
        if (this.adapter == null && this.rvWorkplaces != null)
            initAdapter();
    }

    private void initAdapter() {
        // Initialize a query for all the workplaces of the user (active ones first) ordered
        // alphabetically:
        final Query query = this.db
                .collection(String.format("users/%s/workplaces", this.user.getUid()))
                .orderBy(Workplace.IS_ACTIVE, Query.Direction.DESCENDING)
                .orderBy(Workplace.COMPANY_NAME);

        // Create the options for the adapter:
        final FirestoreRecyclerOptions<Workplace> options = new FirestoreRecyclerOptions.Builder<Workplace>()
                .setLifecycleOwner(this)
                .setQuery(query, Workplace.class)
                .build();

        // Create the adapter and set it for the recycler view:
        this.adapter = new OnlineWorkplacesAdapter(requireContext(), () -> {
            this.rvWorkplaces.setVisibility(View.GONE);
            this.tvNoWorkplaces.setVisibility(View.VISIBLE);
        }, () -> {
            this.rvWorkplaces.setVisibility(View.VISIBLE);
            this.tvNoWorkplaces.setVisibility(View.GONE);
        }, options);
        this.rvWorkplaces.setAdapter(adapter);
    }

    private void showAllWorkplaces() {
        final Query q = this.db
                // Get every workplace of the current user:
                .collection(String.format("users/%s/workplaces", this.user.getUid()))
                // Sort by activeness and alphabetical order:
                .orderBy(Workplace.IS_ACTIVE, Query.Direction.DESCENDING)
                .orderBy(Workplace.COMPANY_NAME);

        // Update the adapter:
        final FirestoreRecyclerOptions<Workplace> options = new FirestoreRecyclerOptions.Builder<Workplace>()
                .setLifecycleOwner(this)
                .setQuery(q, Workplace.class)
                .build();
        this.adapter.updateOptions(options);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // If the query is empty, show all workplaces:
        if (query.isEmpty()) {
            this.showAllWorkplaces();
            return true;
        }

        // Perform a like query:
        final String workplaceName = Util.fixNamingCapitalization(query);
        final Query q = this.db
                .collection(String.format("users/%s/workplaces", this.user.getUid()))
                .whereGreaterThanOrEqualTo(Workplace.COMPANY_NAME, workplaceName)
                .whereLessThan(Workplace.COMPANY_NAME, workplaceName + "\uf8ff")
                // Unfortunately if we perform a greater/less than operations we can sort only by
                // the field we operated on:
                .orderBy(Workplace.COMPANY_NAME);

        final FirestoreRecyclerOptions<Workplace> options = new FirestoreRecyclerOptions.Builder<Workplace>()
                .setLifecycleOwner(this)
                .setQuery(q, Workplace.class)
                .build();
        this.adapter.updateOptions(options);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // If the query is empty, show all workplaces:
        if (newText.isEmpty()) {
            this.showAllWorkplaces();
            return true;
        }
        return false;
    }
}
