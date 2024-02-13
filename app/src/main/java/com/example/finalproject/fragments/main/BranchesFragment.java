package com.example.finalproject.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.custom_views.OnlineBranchesAdapter;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.util.WrapperLinearLayoutManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

public class BranchesFragment extends Fragment {
    // The connected user:
    private User connectedUser;

    // The recycler view that holds all the branches:
    private RecyclerView rvBranches;

    // The text view that appears if the recycler view is empty:
    private TextView tvBusinessNotFound;

    // The adapter of the recycler view:
    private OnlineBranchesAdapter adapter;

    // The search view that allows the user to search for a specific business:
    private SearchView svBranches;

    // TODO: Implement search view

    // The checkbox that allows the user to search for businesses in their city only:
    private CheckedTextView checkboxMyCity;

    // TODO: Implement city check box

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View parent = inflater.inflate(R.layout.fragment_main_branches, container, false);

        // Load the views:
        this.rvBranches = parent.findViewById(R.id.fragMainBranchesRvBranches);
        this.svBranches = parent.findViewById(R.id.fragMainBranchesSvBusinesses);
        this.checkboxMyCity = parent.findViewById(R.id.fragMainBranchesMyCityCheckBox);
        this.tvBusinessNotFound = parent.findViewById(R.id.fragMainBranchesTvNoBusinessFound);

        // Set the "Business not found" textView's visibility to gone:
        this.tvBusinessNotFound.setVisibility(View.GONE);

        // Initialize layout manager:
        this.rvBranches.setLayoutManager(new WrapperLinearLayoutManager(requireContext()));

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
        this.adapter = new OnlineBranchesAdapter(
                this.connectedUser,
                requireContext(),
                () -> {
                    // Show the "business not found" text view and make the recycler view disappear
                    // (if it doesn't disappear then the text isn't full-screen):
                    this.tvBusinessNotFound.setVisibility(View.VISIBLE);
                    this.rvBranches.setVisibility(View.GONE);
                },
                options
        );

        // Set the adapter for the recycler view:
        this.rvBranches.setAdapter(this.adapter);
    }

    public void setUser(User connectedUser) {
        this.connectedUser = connectedUser;
    }

}
