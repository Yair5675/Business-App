package com.example.finalproject.database.online;

import android.graphics.Bitmap;

import com.example.finalproject.database.online.collections.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FirebaseDatabase {
    // A reference to the actual database:
    private final FirebaseFirestore db;

    // A reference to firebase authentication:
    private final FirebaseAuth auth;

    // A reference to firebase storage:
    private final StorageReference storageRef;

    // The only instance of the class:
    private static FirebaseDatabase instance;

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
        // Authenticate the user:
        this.auth
                .createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(
                        this.getAuthCallback(user, userImg, successListener, failureListener)
                );
    }

    /**
     * Returns the callback that will be executed once authenticating the user was performed. If
     * the authentication was successful, the callback will set the user's ID and try to upload the
     * user's image to the storage. If the authentication failed, the callback will call the given
     * failureListener callback.
     * @param user The user object containing the user's information.
     * @param userImg The user's image, will be uploaded to firebase storage.
     * @param successListener The callback that will be executed once the entire creation process
     *                        was finished successfully.
     * @param failureListener The callback that will be executed if the creation of the user failed.
     * @return The callback that will be executed once authenticating the user was performed.
     */
    private OnCompleteListener<AuthResult> getAuthCallback(
            User user, Bitmap userImg,
            OnSuccessListener<Void> successListener, OnFailureListener failureListener
    ) {
        return task -> {
            // If the authentication was a success:
            if (task.isSuccessful()) {
                if (task.getResult().getUser() != null) {
                    // Get the firebase user:
                    FirebaseUser firebaseUser = task.getResult().getUser();

                    // Get the user ID:
                    user.setUid(firebaseUser.getUid());

                    // Save the user's image in the storage:
                    this.storageRef.child(getImagePath(user.getUid()))
                            .putBytes(toByteArray(userImg))
                            .addOnCompleteListener(
                                    this.getImgUploadCallback(
                                            user, firebaseUser, successListener, failureListener
                                    )
                            );
                }
            }
            // If not, call the failure callback:
            else if (task.getException() != null)
                failureListener.onFailure(task.getException());
        };
    }

    private OnCompleteListener<UploadTask.TaskSnapshot> getImgUploadCallback(
            User user, FirebaseUser firebaseUser,
            OnSuccessListener<Void> successListener, OnFailureListener failureListener
    ) {
        return task -> {
            // If the upload to storage was successful:
            if (task.isSuccessful()) {
                // Save the user in Firestore:
                this.db.collection("users")
                        .document(user.getUid())
                        .set(user)
                        .addOnSuccessListener(successListener)
                        .addOnFailureListener(failureListener);
            }
            // If not, delete the user from the authentication and activate the onFailureListener:
            else if (task.getException() != null) {
                firebaseUser.delete();
                failureListener.onFailure(task.getException());
            }
        };
    }

    private static String getCurrentTime() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");
        return formatter.format(LocalDate.now());
    }

    private static String getImagePath(String uid) {
        return String.format("images/%s/%s", uid, getCurrentTime());
    }

    private static byte[] toByteArray(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }
}
