package com.example.finalproject.fragments.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.activities.InputActivity;
import com.example.finalproject.custom_views.OnlineBranchesAdapter;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.input.business.BusinessRegistrationForm;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

public class BranchesFragment extends Fragment {
    // The connected user:
    private User connectedUser;

    // The recycler view that holds all the branches:
    private RecyclerView rvBranches;

    // The adapter of the recycler view:
    private OnlineBranchesAdapter adapter;

    // The search view that allows the user to search for a specific business:
    private SearchView svBranches;

    // The checkbox that allows the user to search for businesses in their city only:
    private CheckedTextView checkboxMyCity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View parent = inflater.inflate(R.layout.fragment_main_branches, container, false);

        // Load the views:
        this.rvBranches = parent.findViewById(R.id.fragMainBranchesRvBranches);
        this.svBranches = parent.findViewById(R.id.fragMainBranchesSvBusinesses);
        this.checkboxMyCity = parent.findViewById(R.id.fragMainBranchesMyCityCheckBox);

        // Initialize layout manager:
        this.rvBranches.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize the adapter:
        this.initAdapter();

        return parent;
    }

    private void initAdapter() {
        // Get a reference to the database:
        final FirebaseFirestore dbRef = FirebaseFirestore.getInstance();

        // Create the recyclerView's options:
        FirestoreRecyclerOptions<Branch> options = new FirestoreRecyclerOptions.Builder<Branch>()
                .setLifecycleOwner(this)
                .setQuery(dbRef.collection("branches"), Branch.class)
                .build();

        // Create the adapter and set the options:
        this.adapter = new OnlineBranchesAdapter(requireContext(), options);

        // Set the adapter for the recycler view:
        this.rvBranches.setAdapter(this.adapter);
    }

    public void setUser(User connectedUser) {
        this.connectedUser = connectedUser;
    }

}
