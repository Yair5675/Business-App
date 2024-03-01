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
    // TODO: For the love of god dismantle this class and cast it to the shadow realm
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

                    // Verify the phone number:
                    verifyPhoneNumber(db, firebaseUser, user.getPhoneNumber(), unused -> {
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
                    }, failureListener);
                }
            }
            // If not, call the failure callback:
            else if (task.getException() != null)
                failureListener.onFailure(task.getException());
        };
    }

    private static void verifyPhoneNumber(
            FirebaseFirestore db,
            FirebaseUser connectedUser,
            String newPhone,
            OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        db.collection("users")
                .whereEqualTo("phoneNumber", newPhone)
                .get()
                .addOnCompleteListener(task -> {
                    // If the task was a success:
                    if (task.isSuccessful()) {
                        // Check if a similar phone number was found:
                        if (task.getResult().isEmpty()) {
                            onSuccessListener.onSuccess(null);
                        }
                        else {
                            // Delete the saved firebase user:
                            connectedUser.delete();
                            onFailureListener.onFailure(new Exception("Existing phone number"));
                        }
                    }
                    else {
                        // Delete the saved firebase user:
                        connectedUser.delete();
                        if (task.getException() != null) {
                            onFailureListener.onFailure(task.getException());
                        }
                    }
                });
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
        // Download the file (limit to 15 megabytes):
        final long MAX_DOWNLOAD_SIZE = (long) (15 * 1024 * 1024);

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
     * calling this function. If not, the update process won't be realized. Pay attention that the
     * email is not updated even if it is changed in the user object. To do that use method
     * "updateEmail".
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
    public static void updateUserInfo(
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

        // Prepare the update callback:
        OnSuccessListener<Void> updateCallback = unused -> {
            // Update the user's image:
            updateUserImage(storage, user, image, unused1 -> {
                // If the image was updated successfully, save the user in the database:
                updateUserOnFirestore(db, user, onSuccessListener, onFailureListener);
                // If the image update failed:
            }, onFailureListener);
        };

        // Update the password if it was changed:
        if (!oldPassword.equals(user.getPassword())) {
            updatePassword(
                    connectedUser, oldEmail, oldPassword, user.getPassword(), updateCallback,
                    onFailureListener
            );
        }
        // If not, just update the other details:
        else
            updateCallback.onSuccess(null);
    }

    public static void updatePassword(
            FirebaseUser user,
            String oldEmail,
            String oldPassword,
            String newPassword,
            OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Re-authenticate user:
        reauthenticateUser(user, oldEmail, oldPassword, unused -> {
            // If the re-authentication was a success, change the password:
            Log.d(TAG, "Updating password");
            user.updatePassword(newPassword)
                    .addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(onFailureListener);
        }, onFailureListener);
    }

    public static void updateEmail(
            FirebaseUser user,
            String oldEmail,
            String newEmail,
            String password,
            OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Re-authenticate the user:
        reauthenticateUser(
                user, oldEmail, password,
                unused -> {
                    // Send a verification email and update the user's email:
                    user.verifyBeforeUpdateEmail(newEmail)
                            .addOnSuccessListener(onSuccessListener)
                            .addOnFailureListener(onFailureListener);
                },
                onFailureListener
        );
    }

    public static void reauthenticateUser(
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
