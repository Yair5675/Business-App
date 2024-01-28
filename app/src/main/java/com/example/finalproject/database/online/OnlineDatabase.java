package com.example.finalproject.database.online;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.database.online.handlers.UsersHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class OnlineDatabase {
    // A reference to the actual database:
    private final FirebaseFirestore db;

    // A reference to firebase authentication:
    private final FirebaseAuth auth;

    // A reference to firebase storage:
    private final StorageReference storageRef;

    // The only instance of the class:
    private static OnlineDatabase instance;

    // Tag used for logging purposes:
    public static final String TAG = "Online database";

    private OnlineDatabase() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.storageRef = FirebaseStorage.getInstance().getReference();
    }

    public static OnlineDatabase getInstance() {
        if (instance == null)
            instance = new OnlineDatabase();
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

    public boolean isConnectedUserEmailVerified() {
        final FirebaseUser connectedUser = this.auth.getCurrentUser();
        if (connectedUser != null)
            return connectedUser.isEmailVerified();
        return true;
    }

    public void sendVerificationEmail() {
        final FirebaseUser connectedUser = this.auth.getCurrentUser();
        if (connectedUser != null)
            connectedUser.sendEmailVerification()
                    .addOnCompleteListener(task ->
                            Log.d(TAG, "Verification email sent: " + task.isSuccessful())
                    );
    }

    public void sendVerificationEmail(OnCompleteListener<Void> onCompleteListener) {
        final FirebaseUser connectedUser = this.auth.getCurrentUser();
        if (connectedUser != null)
            connectedUser.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        Log.d(TAG, "Verification email sent: " + task.isSuccessful());
                        onCompleteListener.onComplete(task);
                    });
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
                            String error = "User is authenticated but is missing from the database";
                            Log.e(TAG, error);
                            onFailureListener.onFailure(new Exception(error));
                        }
                    },
                    onFailureListener
            );
        } else {
            onFailureListener.onFailure(new Exception("No user is connected"));
        }
    }

    /**
     * Loads the image of the user into an image view.
     * @param context The context calling the load, necessary for Glide.
     * @param user The user whose image will be loaded into the image view.
     * @param imageView The image view that the user's image will be loaded into.
     * @param errorImg The ID of a drawable that will be shown if an error occurred while retrieving
     *                 the user's image.
     */
    public void loadUserImgFromStorage(
            Context context,
            User user,
            ImageView imageView,
            @DrawableRes int errorImg
    ) {
        Glide.with(context)
                .load(this.storageRef.child(user.getImagePath()))
                .apply(
                        new RequestOptions()
                        .error(errorImg)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                )
                .into(imageView);
    }

    /**
     * Disconnects the currently connected user.
     */
    public void disconnectUser() {
        // Sign out. If no user is connected this function has no effect
        this.auth.signOut();
    }

    public void logUserIn(
            String email, String password,
            OnSuccessListener<User> onSuccessListener, OnFailureListener onFailureListener
    ) {
        // Log in the user:
        this.auth
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // If the user successfully logged in, get them from the database and use the
                    // given callback:
                    getCurrentUser(onSuccessListener, onFailureListener);
                })
                .addOnFailureListener(onFailureListener);
    }

    public void refreshUser(
            String password,
            OnSuccessListener<User> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        Log.d(TAG, "Given password: " + password);
        FirebaseUser user;
        if ((user = this.auth.getCurrentUser()) != null) {
            user.reload()
                    .addOnCompleteListener(task -> {
                        Log.d(TAG, "User email after trying to reload: " + user.getEmail());
                        // If the operation logged the user out, get them back in:
                        if (task.isSuccessful()) {
                            if (!isUserSignedIn())
                                logUserIn(user.getEmail(), password, onSuccessListener, onFailureListener);
                            else
                                getCurrentUser(onSuccessListener, onFailureListener);
                        }
                        else if (task.getException() != null) {
                            Log.e(TAG, "Refreshing user failed", task.getException());
                            onFailureListener.onFailure(task.getException());
                        }
                    });
        }
    }

    public void getUserImage(
            User user,
            OnSuccessListener<Bitmap> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        UsersHandler.getUserImage(user, this.storageRef, onSuccessListener, onFailureListener);
    }

    /**
     * Updates the user's email.
     * @param oldEmail The old email that will be change.
     * @param newEmail The new email.
     * @param password The password of the user.
     * @param onSuccessListener A callback that will be activated when the verification email is
     *                          sent (not necessarily when the email is changed).
     * @param onFailureListener A callback that will be activated if the email update failed at some
     *                          point.
     */
    public void updateUserEmail(
            String oldEmail,
            String newEmail,
            String password,
            OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        UsersHandler.updateEmail(
                this.auth.getCurrentUser(),
                oldEmail,
                newEmail,
                password,
                onSuccessListener,
                onFailureListener
        );
    }

    /**
     * Sometimes when the user changes their email, it is not immediately changed due to the email
     * verification step. Therefor, this function checks if the connected user has different email
     * than the given user, changes the user object, and saves it in the database.
     * @param user The not necessarily updated user who might have out-dated email.
     */
    public void fixUserEmail(User user) {
        FirebaseUser connectedUser;
        if ((connectedUser = this.auth.getCurrentUser()) != null && connectedUser.getEmail() != null) {
            Log.d(TAG, "Updated email: " + connectedUser.getEmail());
            Log.d(TAG, "Saved email: " + user.getEmail());
            if (!connectedUser.getEmail().equals(user.getEmail())) {
                Log.d(TAG, "fixed user email");
                user.setEmail(connectedUser.getEmail());
                this.db
                        .collection("users")
                        .document(user.getUid())
                        .update("email", connectedUser.getEmail());
            }
        }
    }

    /**
     * Updates a user in the database.
     * @param user A user object with the updated info. The ID in this user will be used to find the
     *             document in the database to update.
     * @param image The updated image of the user.
     * @param onSuccessListener A callback that will be activated once the user is fully updated.
     * @param onFailureListener A callback that will be activated if any error occurred during the
     *                          updating process.
     */
    public void updateUser(
            User user,
            String oldEmail,
            String oldPassword,
            Bitmap image,
            OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        UsersHandler.updateUserInfo(
                this.auth,
                this.db,
                this.storageRef,
                oldEmail,
                oldPassword,
                user,
                image,
                onSuccessListener,
                onFailureListener
        );
    }
}
