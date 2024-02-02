package com.example.finalproject.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;

public class BranchesFragment extends Fragment implements View.OnClickListener {
    // The recycler view that holds all the branches:
    private RecyclerView rvBranches;

    // The search view that allows the user to search for a specific business:
    private SearchView svBranches;

    // The checkbox that allows the user to search for businesses in their city only:
    private CheckedTextView checkboxMyCity;

    // The add business image:
    private ImageView imgAddBusiness;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View parent = inflater.inflate(R.layout.fragment_branches, container, false);

        // Load the views:
        this.rvBranches = parent.findViewById(R.id.fragBranchesRvBranches);
        this.svBranches = parent.findViewById(R.id.fragBranchesSearchBranch);
        this.checkboxMyCity = parent.findViewById(R.id.fragBranchesMyCityCheckBox);
        this.imgAddBusiness = parent.findViewById(R.id.fragBranchesImgAddBusiness);

        // Add the class as an onclick listener:
        this.imgAddBusiness.setOnClickListener(this);

        // TODO: Implement the recycler view and search view once the businesses are added
        return parent;
    }

    @Override
    public void onClick(View view) {
        final int ID = view.getId();

        if (ID == R.id.fragBranchesImgAddBusiness) {
            // TODO: Go to the add businesses activity
        }
    }
}
