package com.example.finalproject.custom_views;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.activities.UsersActivity;
import com.example.finalproject.database.AppDatabase;
import com.example.finalproject.database.entities.User;
import com.example.finalproject.util.Result;
import com.example.finalproject.util.UserFilter;
import com.example.finalproject.util.Util;

import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserVH> implements Filterable {
    // A reference to the database:
    private final AppDatabase db;

    // The list of users displayed in the adapter:
    private final List<User> users;

    // The activity that holds the adapter:
    private final UsersActivity usersActivity;

    public UserAdapter(UsersActivity usersActivity) {
        this.db = AppDatabase.getInstance(usersActivity);
        this.usersActivity = usersActivity;
        this.users = this.db.userDao().getAll();
    }

    @NonNull
    @Override
    public UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the row_users.xml file and create a custom view holder with it:
        final View rowView = LayoutInflater.from(this.usersActivity).inflate(R.layout.row_users, parent, false);
        return new UserVH(rowView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserVH holder, int position) {
        // Check if the currently connected user is the admin:
        User user;
        if (AppDatabase.getConnectedUser().isAdmin()) {
            // Try to get the current user:
            final Result<User, String> userResult = Result.ofNullable(this.users.get(position), "");
            if (userResult.isErr())
                return;

            // Get the user after making sure it is valid:
            user = userResult.getValue();
        }
        else {
            // If it isn't the admin, the only user displayed is the connected one:
            user = AppDatabase.getConnectedUser();
        }

        // Set the details of the user:
        final String fullName = user.getName() + " " + user.getSurname();
        holder.tvName.setText(fullName);

        final int age = (int) ChronoUnit.YEARS.between(user.getBirthdate(), LocalDate.now());
        holder.tvAge.setText(String.format(Locale.getDefault(), "%d years old", age));

        holder.tvGender.setText(user.getGender());

        holder.tvAddress.setText(user.getAddress());

        holder.tvCity.setText(user.getCityName(this.usersActivity));

        holder.tvEmail.setText(user.getEmail());

        final String phone = user.getPhoneNumber();
        holder.tvPhone.setText(String.format("%s-%s-%s",
                phone.substring(0, 3),
                phone.substring(3, 6),
                phone.substring(6)
        ));

        // Set the image of the user:
        final Result<Bitmap, FileNotFoundException> imgResult = Util.getImage(
                this.usersActivity,
                user.getPictureFileName()
        );
        if (imgResult.isOk())
            holder.imgUser.setImageBitmap(imgResult.getValue());
        else
            Log.e("User adapter setting image", imgResult.getError().toString());
    }

    @Override
    public int getItemCount() {
        // Return the true size of the list only if the admin is connected, otherwise return 1:
        return AppDatabase.getConnectedUser().isAdmin() ? this.users.size() : 1;
    }

    private void activateDeleteDialog(final int userIndex) {
        final User currentUser = this.users.get(userIndex);

        // Check that the current user is NOT the connected user:
        if (currentUser.equals(AppDatabase.getConnectedUser())) {
            Toast.makeText(this.usersActivity, "You cannot delete your account from here", Toast.LENGTH_SHORT).show();
            return;
        }

        // Define an OnClickListener for the dialog:
        final DialogInterface.OnClickListener onClickListener =
                (dialogInterface, buttonClicked) -> {
                    if (buttonClicked == DialogInterface.BUTTON_POSITIVE) {
                        currentUser.deleteUser(this.usersActivity);

                        // Clear the query and cause it to set the list of the adapter:
                        this.usersActivity.clearQuery();
                        notifyItemRemoved(userIndex);
                    }
                };

        // Show a dialog if they want to delete it:
        final AlertDialog.Builder builder = new AlertDialog.Builder(this.usersActivity);
        builder.setTitle("Delete User")
                .setMessage(String.format("Are you sure you want to delete %s?", currentUser.getName()))
                .setPositiveButton("Confirm", onClickListener)
                .setNegativeButton("Cancel", onClickListener)
                .setCancelable(false);

        // Show the dialog:
        builder.create().show();
    }

    @Override
    public UserFilter getFilter() {
        return new UserFilter(this.db, this);
    }

    public List<User> getUsers() {
        return this.users;
    }

    public class UserVH extends RecyclerView.ViewHolder {
        // The image of the user:
        private final ImageView imgUser;

        // The layout containing additional info about the user:
        private final ConstraintLayout clMoreInfo;

        // The text views in the row:
        private final TextView tvName, tvAge, tvGender, tvAddress, tvCity, tvEmail, tvPhone;

        public UserVH(@NonNull View itemView) {
            super(itemView);

            // Load all the views:
            this.imgUser = itemView.findViewById(R.id.rowUsersImgUser);
            this.clMoreInfo = itemView.findViewById(R.id.rowUsersClMoreInfo);
            this.tvName = itemView.findViewById(R.id.rowUsersTvUserName);
            this.tvAge = itemView.findViewById(R.id.rowUsersTvUserAge);
            this.tvGender = itemView.findViewById(R.id.rowUsersTvGender);
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
                if (AppDatabase.getConnectedUser().isAdmin())
                    activateDeleteDialog(getAdapterPosition());
                return true;
            });
        }
    }
}
