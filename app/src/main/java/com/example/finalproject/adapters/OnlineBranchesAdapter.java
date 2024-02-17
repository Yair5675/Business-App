package com.example.finalproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.activities.BranchActivity;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.Locale;

public class OnlineBranchesAdapter extends OnlineAdapter<Branch, OnlineBranchesAdapter.BranchVH> {
    // The connected user:
    private User user;

    // The context of the recycler view:
    private final Context context;

    public OnlineBranchesAdapter(
            User user, Context context, Runnable onEmptyCallback, Runnable onNotEmptyCallback,
            @NonNull FirestoreRecyclerOptions<Branch> options
    ) {
        super(context, onEmptyCallback, onNotEmptyCallback, options);
        this.user = user;
        this.context = context;
    }

    public void setUser(User user) {
        this.user = user;
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

        public BranchVH(@NonNull View itemView) {
            super(itemView);

            // Load the views:
            this.tvName = itemView.findViewById(R.id.rowBranchTvName);
            this.tvOpeningTime = itemView.findViewById(R.id.rowBranchTvOpeningTime);
            this.tvClosingTime = itemView.findViewById(R.id.rowBranchTvClosingTime);
            this.tvAddress = itemView.findViewById(R.id.rowBranchTvAddress);

            // Go to the branch activity if the user clicks on the see more button:
            itemView.findViewById(R.id.rowBranchBtnSeeMore).setOnClickListener(_v -> {
                // Load the current branch:
                final int index = getAbsoluteAdapterPosition();
                if (index >= 0 && index < getItemCount()) {
                    final Branch branch = getItem(index);

                    // Put the branch and user in the intent:
                    final Intent intent = new Intent(context, BranchActivity.class);
                    intent.putExtra("user", user);
                    intent.putExtra("branch", branch);

                    // Go to the activity:
                    context.startActivity(intent);
                }
            });
        }
    }
}
