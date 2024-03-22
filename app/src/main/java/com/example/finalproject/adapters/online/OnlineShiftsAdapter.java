package com.example.finalproject.adapters.online;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.database.online.collections.Shift;
import com.example.finalproject.util.Constants;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.time.LocalTime;
import java.util.Locale;

public class OnlineShiftsAdapter extends OnlineAdapter<Shift, OnlineShiftsAdapter.ShiftVH> {
    // A flag indicating if the branch's name should be visible:
    private final boolean showCompanyName;

    // A flag indicating if only shifts that haven't happened yet should be visible:
    private final boolean showFutureShiftsOnly;

    public OnlineShiftsAdapter(boolean showCompanyName, boolean showFutureShiftsOnly, Context context, Runnable onEmptyCallback, Runnable onNotEmptyCallback, @NonNull FirestoreRecyclerOptions<Shift> options) {
        super(context, onEmptyCallback, onNotEmptyCallback, options);
        this.showCompanyName = showCompanyName;
        this.showFutureShiftsOnly = showFutureShiftsOnly;
    }

    @Override
    protected void onBindViewHolder(@NonNull ShiftVH holder, int position, @NonNull Shift shift) {
        // If the future flag is on:
        if (this.showFutureShiftsOnly) {
            // Compare now to the opening time of the shifts:
            final LocalTime now = LocalTime.now();
            final int currentMinutes = now.getHour() * 60 + now.getMinute();

            // Don't show the shift if it has passed:
            if (shift.startHour() * 60 + shift.startMinutes() < currentMinutes) {
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                return;
            }
            else {
                holder.itemView.setVisibility(View.VISIBLE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
        // Set company name if the "showCompanyName" flag is true:
        if (this.showCompanyName) {
            holder.tvCompanyName.setVisibility(View.VISIBLE);
            holder.tvCompanyName.setText(shift.getCompanyName());
        }
        else
            holder.tvCompanyName.setVisibility(View.GONE);

        // Set role:
        holder.etRole.setText(shift.getRoleName());

        // Set date:
        holder.etDate.setText(Constants.DATE_FORMAT.format(shift.getStartingTime()));

        // Set the edit text:
        final String timeTxt = String.format(
                Locale.getDefault(),
                "%d:%02d to %d:%02d",
                shift.startHour(), shift.startMinutes(), shift.endHour(), shift.endMinute()
        );
        holder.etTime.setText(timeTxt);
    }

    @NonNull
    @Override
    public OnlineShiftsAdapter.ShiftVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the XML file:
        final View itemView = LayoutInflater.from(this.context).inflate(R.layout.row_shift, parent, false);
        return new ShiftVH(itemView);
    }

    public static class ShiftVH extends RecyclerView.ViewHolder {
        // The views in the shift row:
        private final TextView tvCompanyName;
        private final EditText etRole, etDate, etTime;

        public ShiftVH(@NonNull View itemView) {
            super(itemView);

            // Load the views:
            this.tvCompanyName = itemView.findViewById(R.id.rowShiftTvCompanyName);
            this.etRole = itemView.findViewById(R.id.rowShiftEtRole);
            this.etDate = itemView.findViewById(R.id.rowShiftEtDate);
            this.etTime = itemView.findViewById(R.id.rowShiftEtTime);
        }
    }
}
