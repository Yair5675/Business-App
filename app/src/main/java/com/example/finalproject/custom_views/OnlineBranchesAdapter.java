package com.example.finalproject.custom_views;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.activities.BranchActivity;
import com.example.finalproject.database.online.collections.Branch;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.Locale;

public class OnlineBranchesAdapter extends FirestoreRecyclerAdapter<Branch, OnlineBranchesAdapter.BranchVH> {
    // The context of the recycler view:
    private final Context context;

    public OnlineBranchesAdapter(Context context, @NonNull FirestoreRecyclerOptions<Branch> options) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull BranchVH holder, int position, @NonNull Branch branch) {
        // Set the name of the branch:
        holder.tvName.setText(branch.getCompanyName());

        // Set the opening and closing time:
        final int openingMinutes = branch.getOpeningTime(), closingMinutes = branch.getClosingTime();
        final String openingTxt = String.format(
                Locale.getDefault(), "Opening: %d:%02d",
                openingMinutes / 60, openingMinutes % 60
        );
        final String closingTxt = String.format(
                Locale.getDefault(), "Closing: %d:%02d",
                closingMinutes / 60, closingMinutes % 60
        );
        holder.tvOpeningTime.setText(openingTxt);
        holder.tvClosingTime.setText(closingTxt);

        // Set the branch's address:
        final String formattedAddress = String.format(
                Locale.getDefault(), "%s, %s, %s",
                branch.getCountry(), branch.getCity(), branch.getAddress()
        );
        holder.tvAddress.setText(formattedAddress);

    }

    @NonNull
    @Override
    public BranchVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the row_workplace.xml file and create a custom view holder with it:
        final View rowView = LayoutInflater.from(this.context).inflate(R.layout.row_branch, parent, false);
        return new BranchVH(rowView);
    }

    public class BranchVH extends RecyclerView.ViewHolder {
        // The text views in the row:
        private final TextView tvName, tvOpeningTime, tvClosingTime, tvAddress;

        // The apply to business button:
        private final Button btnApplyToBusiness;

        public BranchVH(@NonNull View itemView) {
            super(itemView);

            // Load the views:
            this.tvName = itemView.findViewById(R.id.rowBranchTvName);
            this.tvOpeningTime = itemView.findViewById(R.id.rowBranchTvOpeningTime);
            this.tvClosingTime = itemView.findViewById(R.id.rowBranchTvClosingTime);
            this.tvAddress = itemView.findViewById(R.id.rowBranchTvAddress);
            this.btnApplyToBusiness = itemView.findViewById(R.id.rowBranchBtnApplyToBusiness);

            // Go to the branch activity if the user clicks on the see more button:
            itemView.findViewById(R.id.rowBranchBtnSeeMore).setOnClickListener(_v -> {
                // Load the current branch:
                final int index = getAbsoluteAdapterPosition();
                if (index >= 0 && index < getItemCount()) {
                    final Branch branch = getItem(getAbsoluteAdapterPosition());

                    // Put the branch in the intent:
                    final Intent intent = new Intent(context, BranchActivity.class);
                    intent.putExtra("branch", branch);

                    // Go to the activity:
                    context.startActivity(intent);
                }
            });
        }
    }
}