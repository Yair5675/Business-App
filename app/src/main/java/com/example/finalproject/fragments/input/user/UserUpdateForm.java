package com.example.finalproject.fragments.input.user;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.finalproject.R;
import com.example.finalproject.activities.MainActivity;
import com.example.finalproject.database.online.StorageUtil;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.input.InputForm;
import com.example.finalproject.util.Result;
import com.example.finalproject.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.Date;
import java.util.function.Consumer;

public class UserUpdateForm extends InputForm {
    // The user before their details were changed:
    private final User oldUser;

    // The user after their details were changed:
    private final User newUser;

    // Whether or not the user's image was changed:
    private boolean isImageChanged;

    // The user's image:
    private Bitmap userImg;
    
    // Tag for debugging purposes:
    private static final String TAG = "UserUpdateForm";

    public UserUpdateForm(@NonNull User connectedUser, Resources res) {
        super(
                // Set the title:
                res.getString(R.string.act_input_title_update),

                // Load the rest of the fragments:
                new UserInputFragment1(connectedUser),
                new UserInputFragment2(connectedUser),
                new UserInputFragment3(connectedUser)
        );

        // Save the user (copy for the new to prevent aliasing):
        this.oldUser = connectedUser;
        this.newUser = new User(this.oldUser);

    }

    @Override
    public void onEndForm(Context context, Consumer<Result<Void, Exception>> onCompleteListener) {
        // Initialize callbacks:
        OnSuccessListener<Void> successListener = getOnSuccessListener(context, onCompleteListener);
        OnFailureListener failureListener = getOnFailureListener(context, onCompleteListener);
        // Re-authenticate the user:
        reauthenticateUser(user -> {

            // Set the new information in the user's object (except for the new email):
            this.loadInfoFromFragments();

            // Validate user connectivity:
            if (this.isUserConnectivityValid(user)) {

                // Update the user's authentication details first:
                this.updateAuthDetails(user, unused1 -> {

                    // Update the user's image second:
                    this.updateUserImage(unused2 -> {

                        // Update the user in the database:
                        this.updateDatabaseDetails(successListener, failureListener);
                    }, failureListener);
                }, failureListener);
            }
        }, e -> {
            Log.e(TAG, "Unable to re-authenticate user", e);
            Toast.makeText(context, "Something went wrong. Log out and try again", Toast.LENGTH_SHORT).show();
            failureListener.onFailure(e);
        });
    }

    private void updateAuthDetails(
            FirebaseUser user, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener
    ) {
        // Check if the user changed their email:
        if (!this.newUser.getEmail().equals(this.oldUser.getEmail()))
            this.updateUserEmail(user, unused -> {
                // Check if the user changed their password:
                if (!this.newUser.getPassword().equals(this.oldUser.getPassword()))
                    updateUserPassword(user, onSuccessListener, onFailureListener);
            }, onFailureListener);
        // Check if the user changed their password:
        else if (!this.newUser.getPassword().equals(this.oldUser.getPassword()))
            this.updateUserPassword(user, onSuccessListener, onFailureListener);
        // If nothing was changed, simply activate the on success listener:
        else
            onSuccessListener.onSuccess(null);
    }

    private void updateUserEmail(FirebaseUser user, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        // Update the email and send a verification email and update the user's email:
        if (user != null)
            user.verifyBeforeUpdateEmail(this.newUser.getEmail())
                    .addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(onFailureListener);
        else
            onFailureListener.onFailure(new Exception("User is null"));
    }

    private void updateUserPassword(FirebaseUser user, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        // Update the password:
        if (user != null)
            user.updatePassword(this.newUser.getPassword())
                    .addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(onFailureListener);
        else
            onFailureListener.onFailure(new Exception("User is null"));
    }

    private void reauthenticateUser(
            OnSuccessListener<FirebaseUser> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Get the current user:
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(
                    this.oldUser.getEmail(), this.oldUser.getPassword()
            );
            user.reauthenticate(credential)
                    .addOnSuccessListener(unused -> onSuccessListener.onSuccess(user))
                    .addOnFailureListener(onFailureListener);
        }
        else
            onFailureListener.onFailure(new Exception("User is null"));
    }

    private void updateUserImage(OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        // Check if the image has been changed:
        if (this.isImageChanged) {
            final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

            // Add the new image:
            final String newPath = StorageUtil.getStorageImagePath(this.newUser.getUid());
            storageRef.child(newPath)
                    .putBytes(Util.toByteArray(this.userImg))
                    .addOnSuccessListener(taskSnapshot -> {
                        // Delete the old image:
                        storageRef.child(this.oldUser.getImagePath())
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    // Set the new path for the user:
                                    this.newUser.setImagePath(newPath);

                                    // Activate the callback:
                                    onSuccessListener.onSuccess(null);
                                })
                                .addOnFailureListener(e -> {
                                    // Delete the old photo:
                                    storageRef.child(newPath).delete();

                                    // Activate the failure callback:
                                    onFailureListener.onFailure(e);
                                });
                    })
                    .addOnFailureListener(onFailureListener);
        }
        // If not, just activate the success callback:
        else
            onSuccessListener.onSuccess(null);
    }

    private void updateDatabaseDetails(OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        // Get a reference to the database:
        final FirebaseFirestore dbRef = FirebaseFirestore.getInstance();

        // Update the user document:
        dbRef.collection("users")
                .document(this.newUser.getUid())
                .set(this.newUser, SetOptions.merge())
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    private boolean isUserConnectivityValid(FirebaseUser user) {
        return user != null && user.getUid().equals(this.oldUser.getUid());
    }

    private static OnSuccessListener<Void> getOnSuccessListener(
            Context context, Consumer<Result<Void, Exception>> onCompleteListener
    ) {
        return unused -> {
            // Signal the user their info was updated successfully:
            Toast.makeText(context, "Updated Successfully!", Toast.LENGTH_SHORT).show();

            // Go to the main activity:
            final Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);

            // Activate the callback:
            onCompleteListener.accept(Result.success(null));
        };
    }

    private static OnFailureListener getOnFailureListener(
            Context context, Consumer<Result<Void, Exception>> callback
    ) {
        return exception -> {
            // Log the error and signal the user something went wrong:
            Log.e("UserUpdateForm", "Failed to update user", exception);

            // Check if the email is used by another user:
            if (exception instanceof FirebaseAuthUserCollisionException)
                Toast.makeText(
                        context,
                        "The email you entered is already used by another user",
                        Toast.LENGTH_SHORT
                ).show();
            else if (exception.getMessage() != null && exception.getMessage().equals("Existing phone number"))
                Toast.makeText(
                        context,
                        "The phone number you entered is already used by another user",
                        Toast.LENGTH_SHORT
                ).show();
            else {
                Log.e("UserUpdateForm", "User update failed", exception);
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }

            // Activate the callback:
            callback.accept(Result.failure(exception));
        };
    }

    private void loadInfoFromFragments() {
        // Load the info from the first input fragment:
        this.loadInfoFromFirstFragment();

        // Load the info from the second input fragment:
        this.loadInfoFromSecondFragment();

        // Load the image from the third input fragment:
        this.loadInfoFromThirdFragment();
    }

    private void loadInfoFromFirstFragment() {
        final Bundle bundle1 = this.inputFragments[0].getInputs();
        this.newUser
                .setName(bundle1.getString(UserInputFragment1.NAME_KEY, oldUser.getName()))
                .setSurname(bundle1.getString(UserInputFragment1.SURNAME_KEY, oldUser.getSurname()))
                .setEmail(bundle1.getString(UserInputFragment1.EMAIL_KEY, oldUser.getEmail()))
                .setPassword(bundle1.getString(UserInputFragment1.PASSWORD_KEY, oldUser.getPassword()))
                ;
        Serializable birthdate = bundle1.getSerializable(UserInputFragment1.BIRTHDATE_KEY);
        if (birthdate instanceof Date)
            this.newUser.setBirthdate((Date) birthdate);
    }

    private void loadInfoFromSecondFragment() {
        final Bundle bundle2 = this.inputFragments[1].getInputs();
        this.newUser
                .setPhoneNumber(bundle2.getString(UserInputFragment2.PHONE_KEY, oldUser.getPhoneNumber()))
                .setCountry(bundle2.getString(UserInputFragment2.COUNTRY_KEY, oldUser.getCountry()))
                .setCity(bundle2.getString(UserInputFragment2.CITY_KEY, oldUser.getCity()))
                .setAddress(bundle2.getString(UserInputFragment2.ADDRESS_KEY, oldUser.getAddress()))
        ;
    }

    private void loadInfoFromThirdFragment() {
        // Get the bundle:
        final Bundle bundle2 = this.inputFragments[2].getInputs();

        // Get the image:
        final byte[] data = bundle2.getByteArray(UserInputFragment3.PHOTO_KEY);
        if (data != null)
            this.userImg = BitmapFactory.decodeByteArray(data, 0, data.length);

        // Get whether or not the image has changed (default to true just in case):
        this.isImageChanged = bundle2.getBoolean(UserInputFragment3.IS_IMAGE_CHANGED_KEY, true);
    }
}
