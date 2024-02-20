package com.example.finalproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.database.online.collections.Shift;
import com.example.finalproject.util.Constants;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.Locale;

public class OnlineUserShiftsAdapter extends OnlineAdapter<Shift, OnlineUserShiftsAdapter.ShiftVH> {

    public OnlineUserShiftsAdapter(Context context, Runnable onEmptyCallback, Runnable onNotEmptyCallback, @NonNull FirestoreRecyclerOptions<Shift> options) {
        super(context, onEmptyCallback, onNotEmptyCallback, options);
    }

    @Override
    protected void onBindViewHolder(@NonNull OnlineUserShiftsAdapter.ShiftVH holder, int position, @NonNull Shift shift) {
        // Load the details into the text views:
        holder.tvCompanyName.setText(shift.getCompanyName());

        holder.tvDate.setText(String.format(
                Locale.getDefault(), "At: %s", Constants.DATE_FORMAT.format(shift.getDate())
        ));

        holder.tvStartTime.setText(String.format(
                Locale.getDefault(), "From: %d:%d", shift.getStartingTime() / 60, shift.getStartingTime() % 60
        ));

        holder.tvEndTime.setText(String.format(
                Locale.getDefault(), "To: %d:%d", shift.getEndingTime() / 60, shift.getEndingTime() % 60
        ));
    }

    @NonNull
    @Override
    public OnlineUserShiftsAdapter.ShiftVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the xml file and create a custom view holder with it:
        final View rowView = LayoutInflater.from(this.context).inflate(R.layout.row_user_shift, parent, false);
        return new ShiftVH(rowView);
    }

    public static class ShiftVH extends RecyclerView.ViewHolder {
        // The text views of the row:
        private final TextView tvCompanyName, tvDate, tvStartTime, tvEndTime;

        public ShiftVH(@NonNull View itemView) {
            super(itemView);

            // Load the views:
            this.tvCompanyName = itemView.findViewById(R.id.rowUserShiftTvCompanyName);
            this.tvDate = itemView.findViewById(R.id.rowUserShiftTvDate);
            this.tvStartTime = itemView.findViewById(R.id.rowUserShiftTvStart);
            this.tvEndTime = itemView.findViewById(R.id.rowUserShiftTvEnd);
        }
    }
}
