package com.example.finalproject.fragments.shifts;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.finalproject.R;
import com.example.finalproject.database.online.collections.Branch;

import java.io.Serializable;
import java.time.LocalDate;

public class ShiftsFragment extends Fragment {
    // The date of all shifts in this fragment:
    private LocalDate date;

    // The branch that the shifts belong to:
    private Branch branch;

    // The maximum amount of shifts in the day:
    private int maxShifts;

    // The button that adds another shift:
    private Button btnAddShift;

    // The layout that holds all shift views:
    private LinearLayout shiftsLayout;

    // The fragment initialization parameter keys:
    private static final String DATE_KEY = "date";
    private static final String BRANCH_KEY = "branch";
    private static final String MAX_SHIFTS_KEY = "maxShifts";

    public ShiftsFragment() {
        // Required empty public constructor
    }

    public static ShiftsFragment newInstance(LocalDate date, Branch branch, int maxShifts) {
        // Create the new fragment:
        ShiftsFragment fragment = new ShiftsFragment();

        // Save the parameters in a bundle and set it in the fragment:
        Bundle args = new Bundle();
        args.putSerializable(DATE_KEY, date);
        args.putSerializable(BRANCH_KEY, branch);
        args.putInt(MAX_SHIFTS_KEY, maxShifts);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load parameters from the arguments:
        final Bundle arguments = getArguments();
        if (arguments != null) {
            final Serializable dateSer = arguments.getSerializable(DATE_KEY), branchSer = arguments.getSerializable(BRANCH_KEY);
            if (dateSer instanceof LocalDate)
                this.date = (LocalDate) dateSer;
            if (branchSer instanceof Branch)
                this.branch = (Branch) branchSer;
            this.maxShifts = arguments.getInt(MAX_SHIFTS_KEY, 3);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment:
        final View parent = inflater.inflate(R.layout.fragment_shifts_shifts, container, false);

        // Load the views of the fragment:
        this.shiftsLayout = parent.findViewById(R.id.fragDayShiftsShiftsLayout);
        this.btnAddShift = parent.findViewById(R.id.fragShiftsShiftsBtnAddShift);

        return parent;
    }
}