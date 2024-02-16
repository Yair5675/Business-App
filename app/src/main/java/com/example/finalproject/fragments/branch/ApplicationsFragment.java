package com.example.finalproject.fragments.branch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.custom_views.adapters.OnlineApplicationsAdapter;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.notifications.EmployeeActionNotification;
import com.example.finalproject.util.WrapperLinearLayoutManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ApplicationsFragment extends Fragment {
    // The current branch:
    private final Branch branch;

    // The applications recyclerView:
    private RecyclerView rvApplications;

    // The text view that appears when there are no applications for the branch:
    private TextView tvNoApplicationsFound;


    public ApplicationsFragment(Branch branch) {
        this.branch = branch;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the XML file:
        final View parent = inflater.inflate(R.layout.fragment_branch_applications, container, false);

        // Load views:
        this.rvApplications = parent.findViewById(R.id.fragBranchApplicationsRvApplications);
        this.tvNoApplicationsFound = parent.findViewById(R.id.fragBranchApplicationsTvNoApplicationsFound);
        this.rvApplications.setLayoutManager(new WrapperLinearLayoutManager(requireContext()));

        // Hide the "No applications found" textView and show the recyclerView:
        this.rvApplications.setVisibility(View.VISIBLE);
        this.tvNoApplicationsFound.setVisibility(View.GONE);

        // Initialize the adapter:
        this.initAdapter();

        return parent;
    }

    private void initAdapter() {
        // Get a reference to the database:
        final FirebaseFirestore dbRef = FirebaseFirestore.getInstance();

        // Create the query:
        final Query query = dbRef
                .collection("branches")
                .document(this.branch.getBranchId())
                .collection("applications");

        // Create the recyclerView's options:
        FirestoreRecyclerOptions<EmployeeActionNotification> options = new FirestoreRecyclerOptions.Builder<EmployeeActionNotification>()
                .setLifecycleOwner(this)
                .setQuery(query, EmployeeActionNotification.class)
                .build();

        // Create the adapter:
        OnlineApplicationsAdapter adapter = new OnlineApplicationsAdapter(
                requireContext(),
                this.branch,
                () -> {
                    this.tvNoApplicationsFound.setVisibility(View.VISIBLE);
                    this.rvApplications.setVisibility(View.GONE);
                },
                () -> {
                    this.tvNoApplicationsFound.setVisibility(View.GONE);
                    this.rvApplications.setVisibility(View.VISIBLE);
                },
                options
        );

        // Set the adapter for the recycler view:
        this.rvApplications.setAdapter(adapter);
    }
}
