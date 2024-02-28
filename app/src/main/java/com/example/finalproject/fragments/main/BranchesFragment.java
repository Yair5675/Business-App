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
import com.example.finalproject.adapters.online.OnlineBranchesAdapter;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.util.Util;
import com.example.finalproject.util.WrapperLinearLayoutManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class BranchesFragment extends Fragment implements SearchView.OnQueryTextListener {
    // A reference to the database:
    private FirebaseFirestore dbRef;

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

    // The checkbox that allows the user to search for businesses in their city only:
    private CheckedTextView checkboxMyCity;

    // TODO: Implement city check box

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View parent = inflater.inflate(R.layout.fragment_main_branches, container, false);

        // Initialize the database reference:
        this.dbRef = FirebaseFirestore.getInstance();

        // Load the views:
        this.rvBranches = parent.findViewById(R.id.fragMainBranchesRvBranches);
        this.svBranches = parent.findViewById(R.id.fragMainBranchesSvBusinesses);
        this.svBranches.setOnQueryTextListener(this);
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
        // Create the recyclerView's options:
        FirestoreRecyclerOptions<Branch> options = new FirestoreRecyclerOptions.Builder<Branch>()
                .setLifecycleOwner(this)
                .setQuery(this.dbRef.collection("branches"), Branch.class)
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
                () -> {
                    // Hide the "business not found" text view and show the recycler view:
                    this.tvBusinessNotFound.setVisibility(View.GONE);
                    this.rvBranches.setVisibility(View.VISIBLE);
                },
                options
        );

        // Set the adapter for the recycler view:
        this.rvBranches.setAdapter(this.adapter);
    }

    public void setUser(User connectedUser) {
        this.connectedUser = connectedUser;
        if (this.adapter != null)
            this.adapter.setUser(connectedUser);
    }

    @Override
    public boolean onQueryTextSubmit(String branchName) {
        // If the branch name is empty, show all branches:
        if (branchName.isEmpty()) {
            final FirestoreRecyclerOptions<Branch> options = new FirestoreRecyclerOptions.Builder<Branch>()
                    .setLifecycleOwner(this)
                    .setQuery(this.dbRef.collection("branches"), Branch.class)
                    .build();
            this.adapter.updateOptions(options);
            return true;
        }

        branchName = Util.fixNamingCapitalization(branchName);

        // Perform a like query:
        final Query query = this.dbRef
                .collection("branches")
                .whereGreaterThanOrEqualTo("companyName", branchName)
                .whereLessThan("companyName", branchName + "\uf8ff")
                .orderBy("city");
        final FirestoreRecyclerOptions<Branch> options = new FirestoreRecyclerOptions.Builder<Branch>()
                .setLifecycleOwner(this)
                .setQuery(query, Branch.class)
                .build();
        this.adapter.updateOptions(options);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // If the branch name is empty, show all branches:
        if (newText.isEmpty()) {
            final FirestoreRecyclerOptions<Branch> options = new FirestoreRecyclerOptions.Builder<Branch>()
                    .setLifecycleOwner(this)
                    .setQuery(this.dbRef.collection("branches"), Branch.class)
                    .build();
            this.adapter.updateOptions(options);
            return true;
        }
        return false;
    }
}
