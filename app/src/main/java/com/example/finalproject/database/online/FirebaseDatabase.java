package com.example.finalproject.database.online;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.database.online.handlers.UsersHandler;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseDatabase {
    // A reference to the actual database:
    private final FirebaseFirestore db;

    // A reference to firebase authentication:
    private final FirebaseAuth auth;

    // A reference to firebase storage:
    private final StorageReference storageRef;

    // The only instance of the class:
    private static FirebaseDatabase instance;

    // Tag used for logging purposes:
    public static final String TAG = "Online database";

    private FirebaseDatabase() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.storageRef = FirebaseStorage.getInstance().getReference();
    }

    public static FirebaseDatabase getInstance() {
        if (instance == null)
            instance = new FirebaseDatabase();
        return instance;
    }

    /**
     * Adds a new user to the database.
     * @param user A user object containing every field except the ID and image reference. The two
     *             fields will be set by this function.
     * @param userImg The bitmap of the user image, will be uploaded to the firebase storage. The
     *                path to the image in the storage will be saved in the user object.
     * @param successListener A callback that will be run when the user is fully saved in the
     *                        database.
     * @param failureListener A callback that will be run if a task failed at any point of the
     *                        creation of the user.
     */
    public void addNewUser(
            User user, Bitmap userImg,
            OnSuccessListener<Void> successListener, OnFailureListener failureListener
    ) {
        UsersHandler.addNewUser(
                this.auth, this.storageRef, this.db, user, userImg, successListener, failureListener
        );
    }

    public boolean isUserSignedIn() {
        return this.auth.getCurrentUser() != null;
    }

    public void getCurrentUser(
            OnSuccessListener<User> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Check if the user is signed in:
        FirebaseUser connectedUser;
        if ((connectedUser = this.auth.getCurrentUser()) != null) {
            // Get the user from the database:
            UsersHandler.getUserById(
                    this.db,
                    connectedUser.getUid(),
                    documentSnapshot -> {
                        // Check that the data exists:
                        if (documentSnapshot.exists() && documentSnapshot.getData() != null) {
                            // Convert the document into a User object:
                            User user = documentSnapshot.toObject(User.class);

                            // Activate the given onSuccessListener with the user object:
                            onSuccessListener.onSuccess(user);
                        } else {
                            Log.e(TAG, "User is authenticated but is missing from the database");
                        }
                    },
                    onFailureListener
            );
        } else {
            onFailureListener.onFailure(new Exception("No user is connected"));
        }
    }
}
