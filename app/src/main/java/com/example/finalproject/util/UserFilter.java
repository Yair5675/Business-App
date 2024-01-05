package com.example.finalproject.util;

import android.annotation.SuppressLint;
import android.widget.Filter;

import com.example.finalproject.custom_views.UserAdapter;
import com.example.finalproject.database.AppDatabase;
import com.example.finalproject.database.entities.User;

import java.util.ArrayList;
import java.util.List;

public class UserFilter extends Filter {
    // A reference to the database:
    private final AppDatabase db;

    // The adapter which will be updated:
    private final UserAdapter adapter;

    // The sorting options when searching:
    private SortingOption sortingOption;
    private enum SortingOption {
        NOTHING,
        NAME,
        SURNAME,
        AGE,
    }

    public UserFilter(AppDatabase db, UserAdapter adapter) {
        this.db = db;
        this.adapter = adapter;
        this.sortingOption = SortingOption.NOTHING;
    }

    public void setSortingOption(String optionName) {
        // Save the previous option in case the option name given is invalid:
        final SortingOption prevOption = this.sortingOption;
        this.sortingOption = null;

        // Search for the option name:
        final SortingOption[] options = SortingOption.values();
        for (int i = 0; i < options.length && this.sortingOption == null; i++)
            if (options[i].name().equals(optionName.toUpperCase()))
                this.sortingOption = options[i];

        // If nothing was found, return the previous option:
        if (this.sortingOption == null)
            this.sortingOption = prevOption;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        // Create a list to hold the filtered values:
        ArrayList<User> filteredUsers;

        // Search for all matching users:
        final String userName = charSequence.toString().toLowerCase().trim();
        filteredUsers = new ArrayList<>(
                this.db.userDao().getUsersSortedAndFiltered(userName, this.sortingOption.name())
        );

        // Return the filtered list and its size:
        final FilterResults results = new FilterResults();
        results.values = filteredUsers;
        results.count = filteredUsers.size();
        return results;
    }

    @Override
    @SuppressLint("NotifyDataSetChanged")
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        // Clear the values and add the filtered ones:
        this.adapter.getUsers().clear();
        this.adapter.getUsers().addAll((List<User>) filterResults.values);
        this.adapter.notifyDataSetChanged();
    }
}
