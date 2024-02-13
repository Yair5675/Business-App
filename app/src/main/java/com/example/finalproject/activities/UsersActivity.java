package com.example.finalproject.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.custom_views.OnlineUsersAdapter;
import com.example.finalproject.database.online.OnlineDatabase;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.util.Util;
import com.example.finalproject.util.WrapperLinearLayoutManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import java.io.Serializable;

public class UsersActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    // A reference to the online database:
    private OnlineDatabase db;

    // The recycler view of the users:
    private RecyclerView rvUsers;

    // The text view that will appear if the users recyclerView is empty:
    private TextView tvNoUsersFound;

    // The online adapter populating the recycler view:
    private OnlineUsersAdapter onlineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        // Load the activity's views:
        SearchView svUsers = findViewById(R.id.actUsersSearchUsers);
        svUsers.setOnQueryTextListener(this);
        this.rvUsers = findViewById(R.id.actUsersRvUsers);

        // Initialize layout manager:
        this.rvUsers.setLayoutManager(new WrapperLinearLayoutManager(this));

        // Initialize the database:
        this.db = OnlineDatabase.getInstance();

        // Initialize the back press callback:
        this.initOnBackPressedCallback();

        // Get the current user:
        final User currentUser = this.loadUserFromIntent();

        // If the user is null, go back to the main activity:
        if (currentUser == null) {
            final Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // If he isn't an admin, hide the search view:
        svUsers.setVisibility(currentUser.isAdmin() ? View.VISIBLE : View.GONE);

        // Initialize the "No users found" textView and make it disappear:
        this.tvNoUsersFound = findViewById(R.id.actUsersTvUserNotFound);
        this.tvNoUsersFound.setVisibility(View.GONE);

        // Initialize the online adapter:
        this.initAdapter(currentUser);
    }

    private @Nullable User loadUserFromIntent() {
        // Get the intent:
        final Intent intent = this.getIntent();

        // Check if a user was given:
        if (intent.hasExtra("user")) {
            // Perform type checking (just in case):
            Serializable user = intent.getSerializableExtra("user");
            if (user instanceof User)
                return (User) user;
        }
        return null;
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

    private void initAdapter(User currentUser) {
        // If the user is an admin:
        FirestoreRecyclerOptions.Builder<User> builder = new FirestoreRecyclerOptions.Builder<User>()
                .setLifecycleOwner(this);
        Query query;
        if (currentUser.isAdmin())
            // Use a simple query to get the first 50 users:
            query = this.db.getFirestoreReference()
                    .collection("users")
                    .orderBy("birthdate", Query.Direction.DESCENDING)
                    .limit(50);
        else
            // Only get the current user:
            query = this.db.getFirestoreReference()
                    .collection("users")
                    .whereEqualTo("uid", currentUser.getUid())
                    .limit(1);

        FirestoreRecyclerOptions<User> options = builder.setQuery(query, User.class).build();
        this.onlineAdapter = new OnlineUsersAdapter(
                this,
                () -> {
                    // Make the "No users found" textView appear and the recyclerView disappear:
                    this.tvNoUsersFound.setVisibility(View.VISIBLE);
                    this.rvUsers.setVisibility(View.GONE);
                },
                () -> {
                    // Make the recyclerView appear and the "No users found" textView disappear:
                    this.tvNoUsersFound.setVisibility(View.GONE);
                    this.rvUsers.setVisibility(View.VISIBLE);
                },
                options
        );
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