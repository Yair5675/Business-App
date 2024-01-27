package com.example.finalproject.database.online.handlers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.finalproject.database.online.StorageUtil;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UsersHandler {

    public static final String TAG = "UsersHandler";

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
                    user.setImagePath(StorageUtil.getStorageImagePath(user.getUid()));

                    // Save the user's image in the storage:
                    storageRef.child(user.getImagePath())
                            .putBytes(Util.toByteArray(userImg))
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

    public static void getUserById(
            FirebaseFirestore db,
            String uid,
            OnSuccessListener<DocumentSnapshot> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        db
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    public static void getUserImage(
            User user,
            StorageReference storage,
            OnSuccessListener<Bitmap> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Download the file (limit to 1.5 megabytes):
        final long MAX_DOWNLOAD_SIZE = (long) (1.5 * 1024 * 1024);

        // Download the bytes:
        storage.child(user.getImagePath()).getBytes(MAX_DOWNLOAD_SIZE)
                .addOnSuccessListener(bytes -> {
                    // Convert the bytes to a bitmap and call the success callback:
                    Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    onSuccessListener.onSuccess(image);
                })
                .addOnFailureListener(onFailureListener);
    }

    /**
     * Updates a user in the database and their image. The updated user MUST BE CONNECTED when
     * calling this function. If not, the update process won't be realized.
     * @param auth A reference to firebase authentication system.
     * @param db A reference to the cloud database.
     * @param storage A reference to the storage.
     * @param oldPassword The old password, necessary for re-authenticating the user.
     * @param user The user object containing the new info. The function will use the ID saved in
     *             this object.
     * @param image The new image of the user, will be saved in the storage under a different name.
     *              The function will delete the previous image, and change the image path inside
     *              the user object and the database.
     * @param onSuccessListener A callback that will be activated once the user is fully updated.
     * @param onFailureListener A callback that will be activated if any error occurred during the
     *                          updating process.
     */
    public static void updateUser(
            FirebaseAuth auth,
            FirebaseFirestore db,
            StorageReference storage,
            String oldEmail,
            String oldPassword,
            User user,
            Bitmap image,
            OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Make sure the updated user is the connected user:
        FirebaseUser connectedUser = auth.getCurrentUser();
        if (connectedUser == null) {
            onFailureListener.onFailure(new Exception("No user is connected"));
            return;
        }
        else if (!connectedUser.getUid().equals(user.getUid())) {
            onFailureListener.onFailure(new Exception("Updated user is not the connected user"));
        }

        // Update the user's authentication details:
        updateAuthDetails(
            connectedUser, oldEmail, oldPassword, user.getEmail(),
            user.getPassword(), unused -> {
                // If the authentication update was successful, update the user's image:
                updateUserImage(storage, user, image, unused1 -> {
                    // If the image was updated successfully, save the user in the database:
                    updateUserOnFirestore(db, user, onSuccessListener, onFailureListener);
                    // If the image update failed:
                }, onFailureListener);
                // If the authentication update failed:
            }, onFailureListener);

    }

    private static void reauthenticateUser(
            FirebaseUser user,
            String email,
            String password,
            OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        Log.d(TAG, "Given email: " + email);
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        user.reauthenticate(credential)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    private static void updateAuthDetails(
            FirebaseUser user,
            String oldEmail,
            String oldPassword,
            String newEmail,
            String newPassword,
            OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Check if the authentication details are similar:
        final boolean similarEmails = oldEmail.equals(newEmail),
                similarPasswords = oldPassword.equals(newPassword);

        // If the new email is similar to the old email, skip the email and update the password:
        if (similarEmails && !similarPasswords) {
            Log.d(TAG, "emails are the same");
            user.updatePassword(newPassword)
                    .addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(onFailureListener);
        }
        else if (!similarEmails)
            // Re-authenticate the user:
            reauthenticateUser(user, oldEmail, oldPassword, _v1 -> {
                // Update the email:
                Log.d(TAG, "sending email verification");
                user.verifyBeforeUpdateEmail(newEmail)
                        .addOnSuccessListener(_v2 -> {
                            // Update the password too:
                            if (!similarPasswords) {
                                Log.d(TAG, "Updating password");
                                user.updatePassword(newPassword)
                                        .addOnSuccessListener(onSuccessListener)
                                        .addOnFailureListener(onFailureListener);
                            }
                            // If the password is similar, activate the on success listener:
                            else
                                onSuccessListener.onSuccess(null);
                        })
                        .addOnFailureListener(onFailureListener);
            }, onFailureListener);

        // If both as similar, activate the on success listener:
        else {
            Log.d(TAG, "Old email: " + oldEmail);
            Log.d(TAG, "New email: " + newEmail);
            onSuccessListener.onSuccess(null);
        }
    }

    private static void updateUserImage(
            StorageReference storage,
            User user,
            Bitmap newImage,
            OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Delete the previous image:
        storage.child(user.getImagePath())
                .delete()
                .addOnSuccessListener(unused -> {
                    // The image was removed successfully, upload the new one:
                    final String newPath = StorageUtil.getStorageImagePath(user.getUid());
                    storage.child(newPath)
                            .putBytes(Util.toByteArray(newImage))
                            .addOnSuccessListener(taskSnapshot -> {
                                // Only if the new image upload was a success, set the new image
                                // path:
                                user.setImagePath(newPath);

                                // Activate the callback:
                                onSuccessListener.onSuccess(null);
                            })
                            // If an error occurred while uploading the new image:
                            .addOnFailureListener(onFailureListener);
                })
                // If an error occurred while deleting the old image:
                .addOnFailureListener(onFailureListener);
    }

    private static void updateUserOnFirestore(
            FirebaseFirestore db,
            User user,
            OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        db
                .collection("users")
                .document(user.getUid())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }
}
