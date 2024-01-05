package com.example.finalproject.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.finalproject.R;
import com.example.finalproject.custom_views.UserAdapter;
import com.example.finalproject.database.AppDatabase;
import com.example.finalproject.util.UserFilter;

public class UsersActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    // The spinner that enables sorting options:
    private Spinner spinner;

    // The search view that allows the admin to search for specific users:
    private SearchView svUsers;

    // The adapter for the recycler view:
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        // Load the search view and set up its queries functionality:
        this.svUsers = findViewById(R.id.actUsersSvUserSearch);
        this.svUsers.setOnQueryTextListener(this);

        // Initialize the adapter:
        this.adapter = new UserAdapter(this);

        // Initialize the spinner:
        this.initSpinner();

        // Set up the recycler view:
        this.setRecyclerView();

        // Set up back button functionality:
        this.setBackBtn();

        // If the connected user isn't an admin he shouldn't be able to search people:
        final int searchVisibility = AppDatabase.getConnectedUser().isAdmin() ? View.VISIBLE : View.GONE;
        this.svUsers.setVisibility(searchVisibility);
        this.spinner.setVisibility(searchVisibility);

        // Don't forget the text view:
        findViewById(R.id.actUsersTvSortBy).setVisibility(searchVisibility);
    }

    private void initSpinner() {
        // Load the spinner:
        this.spinner = findViewById(R.id.actUsersSpinSorting);

        // Load the sorting options:
        final String[] options = getResources().getStringArray(R.array.act_users_spinner_sorting_options);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        this.spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> _a, View _v, int index, long _l) {
                        // If something was selected, set the sorting option in the filter:
                        final UserFilter filter = UsersActivity.this.adapter.getFilter();
                        filter.setSortingOption(options[index]);

                        // Re-filter based on this sorting:
                        final String query = UsersActivity.this.svUsers.getQuery().toString();
                        filter.filter(query);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> _a) {
                        // If nothing was selected, set the sorting option to 'Nothing':
                        UsersActivity.this.adapter.getFilter().setSortingOption("Nothing");
                    }
                }
        );

        // Load the adapter:
        this.spinner.setAdapter(adapter);
    }

    private void setRecyclerView() {
        final RecyclerView usersList = findViewById(R.id.actUsersRvUsers);
        usersList.setAdapter(this.adapter);
        usersList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setBackBtn() {
        // If the user presses the back button, go back to the main activity:
        final OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                final Intent intent = new Intent(UsersActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(callback);
    }

    public void clearQuery() {
        this.svUsers.setQuery("", true);
        this.adapter.getFilter().filter(this.svUsers.getQuery());
    }

    @Override
    public boolean onQueryTextSubmit(String nameSearched) {
        return onQueryTextChange(nameSearched);
    }

    @Override
    public boolean onQueryTextChange(String nameSearched) {
        this.adapter.getFilter().filter(nameSearched);
        return false;
    }
}