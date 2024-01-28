package com.example.finalproject.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.finalproject.R;
import com.example.finalproject.custom_views.OnlineUsersAdapter;
import com.example.finalproject.database.online.OnlineDatabase;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.util.Util;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class UsersActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    // A reference to the online database:
    private OnlineDatabase db;

    // The search view, allows searching for a specific user:
    private SearchView svUsers;

    // The recycler view of the users:
    private RecyclerView rvUsers;

    // The online adapter populating the recycler view:
    private OnlineUsersAdapter onlineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        // Load the activity's views:
        this.svUsers = findViewById(R.id.actUsersSearchUsers);
        this.svUsers.setOnQueryTextListener(this);
        this.rvUsers = findViewById(R.id.actUsersRvUsers);

        // Initialize layout manager:
        this.rvUsers.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the database:
        this.db = OnlineDatabase.getInstance();

        // Initialize the back press callback:
        this.initOnBackPressedCallback();

        // Initialize the online adapter:
        this.initAdapter();
    }

    private void initOnBackPressedCallback() {
        // Define the callback:
        final OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Go to the main activity:
                final Intent intent = new Intent(UsersActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        // Add the callback:
        getOnBackPressedDispatcher().addCallback(callback);
    }

    private void initAdapter() {
        // Use a simple query to get the first 50 users:
        final Query query = this.db.getFirestoreReference()
                .collection("users")
                .orderBy("birthdate", Query.Direction.DESCENDING)
                .limit(50);
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(query, User.class)
                .build();
        this.onlineAdapter = new OnlineUsersAdapter(this, options);
        this.rvUsers.setAdapter(this.onlineAdapter);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Edit the query to have regular capitalization:
        if (query.isEmpty())
            return false;
        query = Util.fixNamingCapitalization(query);
        // Perform a like query:
        final Query searchQuery = this.db.getFirestoreReference()
                .collection("users")
                .whereGreaterThanOrEqualTo("fullName", query)
                .whereLessThan("fullName", query + "\uf8ff")
                .orderBy("birthdate", Query.Direction.DESCENDING)
                .limit(50);
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(searchQuery, User.class)
                .build();
        this.onlineAdapter.updateOptions(options);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}