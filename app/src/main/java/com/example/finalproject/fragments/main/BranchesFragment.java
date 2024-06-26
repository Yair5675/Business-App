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

public class BranchesFragment extends Fragment implements View.OnClickListener, SearchView.OnQueryTextListener {
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

        // Make an onClickListener for the city checkbox:
        this.checkboxMyCity.setOnClickListener(this);

        // Set the "Business not found" textView's visibility to gone:
        this.tvBusinessNotFound.setVisibility(View.GONE);

        // Initialize layout manager:
        this.rvBranches.setLayoutManager(new WrapperLinearLayoutManager(requireContext()));

        // Initialize the adapter:
        this.initAdapter();

        return parent;
    }

    private void initAdapter() {
        // View all branches that are active:
        final Query query = this.dbRef.collection("branches")
                .whereEqualTo(Branch.IS_ACTIVE, true);

        // Create the recyclerView's options:
        FirestoreRecyclerOptions<Branch> options = new FirestoreRecyclerOptions.Builder<Branch>()
                .setLifecycleOwner(this)
                .setQuery(query, Branch.class)
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
        this.refreshBranchesSearch();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // If the branch name is empty, show all branches:
        if (newText.isEmpty()) {
            this.refreshBranchesSearch();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        final int ID = view.getId();

        if (ID == R.id.fragMainBranchesMyCityCheckBox) {
            // Toggle the view:
            this.checkboxMyCity.toggle();

            refreshBranchesSearch();
        }
    }

    private void refreshBranchesSearch() {
        // Get every active branch:
        Query query = this.dbRef.collection("branches").whereEqualTo(Branch.IS_ACTIVE, true);

        // Limit to the user's city if the check box is checked:
        if (this.checkboxMyCity.isChecked())
            query = query.whereEqualTo(Branch.CITY, this.connectedUser.getCity())
                    .whereEqualTo(Branch.COUNTRY, this.connectedUser.getCountry());

        // Check if the user searched anything:
        String branchName;
        if (!(branchName = Util.fixNamingCapitalization(this.svBranches.getQuery().toString())).isEmpty())
            query = query
                    .whereGreaterThanOrEqualTo(Branch.COMPANY_NAME, branchName)
                    .whereLessThan(Branch.COMPANY_NAME, branchName + "\uf8ff");

        // Order alphabetically:
        query = query.orderBy(Branch.COMPANY_NAME);

        // Finally, update the options:
        final FirestoreRecyclerOptions<Branch> options = new FirestoreRecyclerOptions.Builder<Branch>()
                .setLifecycleOwner(this)
                .setQuery(query, Branch.class)
                .build();
        this.adapter.updateOptions(options);
    }
}
