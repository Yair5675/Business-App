package com.example.finalproject.fragments.branch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.adapters.OnlineRolesAdapter;
import com.example.finalproject.dialogs.AddRoleDialog;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class RolesFragment extends Fragment {
    // Whether the user is a manager or not:
    private boolean isManager;

    // The ID of the current branch:
    private final String branchId;

    // The recycler view displaying the roles:
    private RecyclerView rvRoles;

    // The adapter of the recycler view:
    private OnlineRolesAdapter adapter;

    // The add role dialog:
    private AddRoleDialog addRoleDialog;

    // The text view that appears if no role is found in the branch:
    private TextView tvRolesNotFound;

    // The add role button:
    private Button btnAddRole;

    public RolesFragment(String branchId, boolean isManager) {
        this.branchId = branchId;
        this.isManager = isManager;
    }

    public void setManager(boolean manager) {
        this.isManager = manager;
        this.adapter.setManager(manager);

        // Show the add role button only if the user is a manager:
        this.btnAddRole.setVisibility(manager ? View.VISIBLE : View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View parent = inflater.inflate(R.layout.fragment_branch_roles, container, false);

        // Load the views in the fragment:
        this.rvRoles = parent.findViewById(R.id.fragBranchRolesRvRoles);
        this.tvRolesNotFound = parent.findViewById(R.id.fragBranchRolesTvNoRolesFound);
        this.btnAddRole = parent.findViewById(R.id.fragBranchRolesBtnAddRole);

        // Initialize the recyclerView adapter:
        this.initAdapter();

        // Initialize the add role dialog:
        this.addRoleDialog = new AddRoleDialog(requireContext(), this.branchId);

        // Show the add role button only if the user is a manager:
        this.btnAddRole.setVisibility(this.isManager ? View.VISIBLE : View.GONE);

        // Initialize onClickListener for the add role button:
        this.btnAddRole.setOnClickListener(_v -> {
            // Show the add role dialog:
            this.addRoleDialog.show();
        });

        return parent;
    }

    private void initAdapter() {
        final FirebaseFirestore dbRef = FirebaseFirestore.getInstance();
        final Query query = dbRef
                .collection("branches")
                .document(this.branchId)
                .collection("roles")
                .orderBy("roleName");
        final FirestoreRecyclerOptions<String> options = new FirestoreRecyclerOptions.Builder<String>()
                .setLifecycleOwner(this)
                // Get the role name from the snapshot:
                .setQuery(query, snapshot -> {
                    final String roleName = snapshot.getString("roleName");
                    return roleName == null ? "" : roleName;
                }).build();

        this.adapter = new OnlineRolesAdapter(
                this.isManager, this.branchId, requireContext(),
                () -> {
                    this.rvRoles.setVisibility(View.GONE);
                    this.tvRolesNotFound.setVisibility(View.VISIBLE);
                },
                () -> {
                    this.rvRoles.setVisibility(View.VISIBLE);
                    this.tvRolesNotFound.setVisibility(View.GONE);
                },
                options
        );
        this.rvRoles.setAdapter(this.adapter);
    }
}
