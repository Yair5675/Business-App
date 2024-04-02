package com.example.finalproject.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.finalproject.R;

public class HelpHomeScreenDialog extends DialogFragment implements View.OnClickListener {
    // The titles for each screen:
    private TextView tvPersonal, tvBusinesses, tvWorkplaces, tvShifts;

    // The layouts of each screen:
    private LinearLayout personalLayout, businessesLayout, workplacesLayout, shiftsLayout;
    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Get the context:
        final Context context = getContext();

        // Inflate the layout:
        final View parent = LayoutInflater.from(context).inflate(R.layout.dialog_help_home_screen, null);

        // Load titles and layouts:
        this.tvPersonal = parent.findViewById(R.id.dialogHelpHomeScreenTvPersonalScreenTitle);
        this.tvBusinesses = parent.findViewById(R.id.dialogHelpHomeScreenTvBusinessesScreenTitle);
        this.tvWorkplaces = parent.findViewById(R.id.dialogHelpHomeScreenTvWorkplacesScreenTitle);
        this.tvShifts = parent.findViewById(R.id.dialogHelpHomeScreenTvShiftsScreenTitle);

        this.personalLayout = parent.findViewById(R.id.dialogHelpHomeScreenPersonalScreenLayout);
        this.businessesLayout = parent.findViewById(R.id.dialogHelpHomeScreenBusinessesScreenLayout);
        this.workplacesLayout = parent.findViewById(R.id.dialogHelpHomeScreenWorkplacesScreenLayout);
        this.shiftsLayout = parent.findViewById(R.id.dialogHelpHomeScreenShiftsScreenLayout);

        // Set on click listeners for the titles:
        this.tvPersonal.setOnClickListener(this);
        this.tvBusinesses.setOnClickListener(this);
        this.tvWorkplaces.setOnClickListener(this);
        this.tvShifts.setOnClickListener(this);

        // Return the dialog:
        return new AlertDialog.Builder(context).setView(parent).create();
    }

    @Override
    public void onClick(View view) {
        // Get the ID:
        final int ID = view.getId();

        // Close / Open each layout:
        if (ID == this.tvPersonal.getId())
            showOrHideSection(this.tvPersonal, this.personalLayout);
        else if (ID == this.tvBusinesses.getId())
            showOrHideSection(this.tvBusinesses, this.businessesLayout);
        else if (ID == this.tvWorkplaces.getId())
            showOrHideSection(this.tvWorkplaces, this.workplacesLayout);
        else if (ID == this.tvShifts.getId())
            showOrHideSection(this.tvShifts, this.shiftsLayout);
    }

    private static void showOrHideSection(TextView tvTitle, LinearLayout layout) {
        // Check if the section is shown or hidden:
        final boolean isExpanded = layout.getVisibility() == View.VISIBLE;

        // Change the icon in the title to the opposite:
        final @DrawableRes int icon = isExpanded ? R.drawable.expand_icon : R.drawable.unexpand_icon;
        tvTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0);

        // Change the layout itself:
        layout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
    }
}
