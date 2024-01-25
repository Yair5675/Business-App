package com.example.finalproject.database.online;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseDatabase {
    // A reference to the actual database:
    private FirebaseFirestore db;

    // The only instance of the class:
    private static FirebaseDatabase instance;

    private FirebaseDatabase() {
        this.db = FirebaseFirestore.getInstance();
    }

    public static FirebaseDatabase getInstance() {
        if (instance == null)
            instance = new FirebaseDatabase();
        return instance;
    }
}
