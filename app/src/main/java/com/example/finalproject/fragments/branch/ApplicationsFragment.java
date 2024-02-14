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

public class ApplicationsFragment extends Fragment {
    // The applications recyclerView:
    private RecyclerView rvApplications;

    // The text view that appears when there are no applications for the branch:
    private TextView tvNoApplicationsFound;

    // TODO: Initialize adapter

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the XML file:
        final View parent = inflater.inflate(R.layout.fragment_branch_applications, container, false);

        // Load views:
        this.rvApplications = parent.findViewById(R.id.fragBranchApplicationsRvApplications);
        this.tvNoApplicationsFound = parent.findViewById(R.id.fragBranchApplicationsTvNoApplicationsFound);

        // Hide the "No applications found" textView and show the recyclerView:
        this.rvApplications.setVisibility(View.VISIBLE);
        this.tvNoApplicationsFound.setVisibility(View.GONE);

        return parent;
    }
}
