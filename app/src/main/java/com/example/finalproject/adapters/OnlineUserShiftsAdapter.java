package com.example.finalproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.database.online.collections.UserShift;
import com.example.finalproject.util.Constants;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.Locale;

public class OnlineUserShiftsAdapter extends OnlineAdapter<UserShift, OnlineUserShiftsAdapter.UserShiftVH> {


    public OnlineUserShiftsAdapter(Context context, Runnable onEmptyCallback, Runnable onNotEmptyCallback, @NonNull FirestoreRecyclerOptions<UserShift> options) {
        super(context, onEmptyCallback, onNotEmptyCallback, options);
    }

    @NonNull
    @Override
    public OnlineUserShiftsAdapter.UserShiftVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the xml file and create a custom view holder with it:
        final View rowView = LayoutInflater.from(this.context).inflate(R.layout.row_user_shift, parent, false);
        return new UserShiftVH(rowView);
    }

    @Override
    protected void onBindViewHolder(@NonNull UserShiftVH holder, int position, @NonNull UserShift shift) {
        // Set the company name and role:
        holder.tvCompanyName.setText(String.format(
                Locale.getDefault(), "%s - %s", shift.getCompanyName(), shift.getRoleName()
        ));

        // Set the date:
        holder.tvDate.setText(String.format(
                Locale.getDefault(),
                "At: %s", Constants.DATE_FORMAT.format(shift.getDate())
        ));

        // Set starting time:
        holder.tvStartTime.setText(String.format(
                Locale.getDefault(), "From: %d:%d",
                shift.getStartingTime() / 60, shift.getStartingTime() % 60
        ));

        // Set ending time:
        holder.tvEndTime.setText(String.format(
                Locale.getDefault(), "To: %d:%d",
                shift.getEndingTime() / 60, shift.getEndingTime() % 60
        ));
    }

    public static class UserShiftVH extends RecyclerView.ViewHolder {
        // The text views of the row:
        private final TextView tvCompanyName, tvDate, tvStartTime, tvEndTime;

        public UserShiftVH(@NonNull View itemView) {
            super(itemView);

            // Load the views:
            this.tvCompanyName = itemView.findViewById(R.id.rowUserShiftTvCompanyName);
            this.tvDate = itemView.findViewById(R.id.rowUserShiftTvDate);
            this.tvStartTime = itemView.findViewById(R.id.rowUserShiftTvStart);
            this.tvEndTime = itemView.findViewById(R.id.rowUserShiftTvEnd);
        }
    }
}
