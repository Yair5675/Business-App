package com.example.finalproject.custom_views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.database.online.OnlineDatabase;
import com.example.finalproject.database.online.collections.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

public class OnlineUsersAdapter extends FirestoreRecyclerAdapter<User, OnlineUsersAdapter.UserVH> {
    // A reference to the database:
    private final OnlineDatabase db;

    // The context of the recyclerView:
    private final Context context;

    public OnlineUsersAdapter(Context context, @NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
        this.context = context;
        this.db = OnlineDatabase.getInstance();
    }

    @Override
    protected void onBindViewHolder(@NonNull UserVH holder, int position, @NonNull User user) {
        // Set the details of the user:
        holder.tvName.setText(user.getFullName());

        // Convert the birthdate to local date:
        final LocalDate localBirthdate = convertDateToLocalDate(user.getBirthdate().toDate());
        final int monthsDiff = (int) ChronoUnit.MONTHS.between(localBirthdate, LocalDate.now());
        final int yearsOld = monthsDiff / 12;
        final int monthsOld = monthsDiff % 12;
        if (monthsOld != 0)
            holder.tvAge.setText(String.format(Locale.getDefault(),
                    "%d years and %d months old", yearsOld, monthsOld));
        else
            holder.tvAge.setText(String.format(Locale.getDefault(), "%d years old", yearsOld));

        holder.tvCountry.setText(user.getCountry());
        holder.tvCity.setText(user.getCity());
        holder.tvAddress.setText(user.getAddress());

        holder.tvEmail.setText(user.getEmail());
        holder.tvPhone.setText(user.getPhoneNumber());

        // Load the user's image:
        this.db.loadUserImgFromStorage(this.context, user, holder.imgUser, R.drawable.guest);
    }

    private static LocalDate convertDateToLocalDate(@NonNull Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @NonNull
    @Override
    public UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the row_users.xml file and create a custom view holder with it:
        final View rowView = LayoutInflater.from(this.context).inflate(R.layout.row_users, parent, false);
        return new OnlineUsersAdapter.UserVH(rowView);
    }

    public static class UserVH extends RecyclerView.ViewHolder {
        // The image of the user:
        private final ImageView imgUser;

        // The layout containing additional info about the user:
        private final LinearLayout clMoreInfo;

        // The text views in the row:
        private final TextView tvName, tvAge, tvCountry, tvAddress, tvCity, tvEmail, tvPhone;

        public UserVH(@NonNull View itemView) {
            super(itemView);

            // Load all the views:
            this.imgUser = itemView.findViewById(R.id.rowUsersImgUser);
            this.clMoreInfo = itemView.findViewById(R.id.rowUsersClMoreInfo);
            this.tvName = itemView.findViewById(R.id.rowUsersTvUserName);
            this.tvAge = itemView.findViewById(R.id.rowUsersTvUserAge);
            this.tvCountry = itemView.findViewById(R.id.rowUsersTvCountry);
            this.tvAddress = itemView.findViewById(R.id.rowUsersTvAddress);
            this.tvCity = itemView.findViewById(R.id.rowUsersTvCity);
            this.tvEmail = itemView.findViewById(R.id.rowUsersTvEmail);
            this.tvPhone = itemView.findViewById(R.id.rowUsersTvPhone);

            // Make the user info appear and disappear when the user is clicked:
            itemView.setOnClickListener(view -> {
                final int currentVisibility = this.clMoreInfo.getVisibility();
                this.clMoreInfo.setVisibility(currentVisibility == View.GONE ? View.VISIBLE : View.GONE);
            });

            // If the row is clicked for a long time, raise a dialog to delete the user:
            itemView.setOnLongClickListener(view -> {
                // TODO: Delete user
                return true;
            });
        }
    }
}
