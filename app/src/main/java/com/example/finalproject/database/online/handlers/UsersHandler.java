package com.example.finalproject.database.online.handlers;

import static com.example.finalproject.util.Util.toByteArray;

import android.graphics.Bitmap;

import com.example.finalproject.database.online.Util;
import com.example.finalproject.database.online.collections.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UsersHandler {
    /**
     * Adds a new user to the database.
     * @param auth A reference to firebase authentication.
     * @param storageRef A reference to firebase storage.
     * @param db A reference to the database.
     * @param user A user object containing every field except the ID and image reference. The two
     *             fields will be set by this function.
     * @param userImg The bitmap of the user image, will be uploaded to the firebase storage. The
     *                path to the image in the storage will be saved in the user object.
     * @param successListener A callback that will be run when the user is fully saved in the
     *                        database.
     * @param failureListener A callback that will be run if a task failed at any point of the
     *                        creation of the user.
     */
    public static void addNewUser(
            FirebaseAuth auth,
            StorageReference storageRef,
            FirebaseFirestore db,
            User user, Bitmap userImg,
            OnSuccessListener<Void> successListener, OnFailureListener failureListener
    ) {
        // Authenticate the user:
        auth
                .createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(
                        getAuthCallback(
                                storageRef, db, user, userImg, successListener, failureListener
                        )
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
    private static OnCompleteListener<AuthResult> getAuthCallback(
            StorageReference storageRef,
            FirebaseFirestore db,
            User user, Bitmap userImg,
            OnSuccessListener<Void> successListener, OnFailureListener failureListener
    ) {
        return task -> {
            // If the authentication was a success:
            if (task.isSuccessful()) {
                if (task.getResult().getUser() != null) {
                    // Get the firebase user:
                    FirebaseUser firebaseUser = task.getResult().getUser();

                    // Set the user ID and image path:
                    user.setUid(firebaseUser.getUid());
                    user.setImagePath(Util.getStorageImagePath(user.getUid()));

                    // Save the user's image in the storage:
                    storageRef.child(user.getImagePath())
                            .putBytes(toByteArray(userImg))
                            .addOnCompleteListener(
                                    getNewUserImgUploadCallback(
                                            db, user, firebaseUser, successListener, failureListener
                                    )
                            );
                }
            }
            // If not, call the failure callback:
            else if (task.getException() != null)
                failureListener.onFailure(task.getException());
        };
    }

    private static OnCompleteListener<UploadTask.TaskSnapshot> getNewUserImgUploadCallback(
            FirebaseFirestore db,
            User user, FirebaseUser firebaseUser,
            OnSuccessListener<Void> successListener, OnFailureListener failureListener
    ) {
        return task -> {
            // If the upload to storage was successful:
            if (task.isSuccessful()) {
                // Save the user in Firestore:
                db.collection("users")
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
}
