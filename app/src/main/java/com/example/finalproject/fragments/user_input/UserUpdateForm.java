package com.example.finalproject.fragments.user_input;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.finalproject.R;
import com.example.finalproject.database.online.OnlineDatabase;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.input.InputForm;
import com.example.finalproject.util.Result;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.io.Serializable;
import java.util.Date;
import java.util.function.Consumer;

public class UserUpdateForm extends InputForm {
    // A reference to the online database:
    private final OnlineDatabase db;

    // The currently connected user:
    private final User user;

    // The user's image:
    private Bitmap userImg;

    public UserUpdateForm(@NonNull User connectedUser, Resources res) {
        super(
                // Set the title:
                res.getString(R.string.act_input_title_update),

                // Load the rest of the fragments:
                new UserInputFragment1(connectedUser),
                new UserInputFragment2(connectedUser),
                new UserInputFragment3(connectedUser)
        );

        // Save the user:
        this.user = connectedUser;

        // Initialize the online database reference:
        this.db = OnlineDatabase.getInstance();
    }

    @Override
    public void onEndForm(Context context, Consumer<Result<Void, Exception>> onCompleteListener) {
        // Save the old email and password:
        final String oldEmail = this.user.getEmail(), oldPassword = this.user.getPassword();

        // Set the new information in the user's object (except for the new email):
        this.loadInfoFromFragments();

        // Initialize callbacks:
        OnSuccessListener<Void> successListener = getOnSuccessListener(context, onCompleteListener);
        OnFailureListener failureListener = getOnFailureListener(context, onCompleteListener);

        // Update the email if it is different:
        if (!user.getEmail().equals(oldEmail))
            this.db.updateUserEmail(
                    oldEmail, user.getEmail(), oldPassword,
                    unused -> Log.d("InputActivity", "Sent verification email"),
                    e -> Log.e("InputActivity", "Failed to change email", e)
            );

        // Update the user in the database
        this.db.updateUser(user, user.getEmail(), oldPassword, this.userImg, successListener, failureListener);
    }

    private static OnSuccessListener<Void> getOnSuccessListener(
            Context context, Consumer<Result<Void, Exception>> onCompleteListener
    ) {
        return unused -> {
            // Signal the user their info was updated successfully:
            Toast.makeText(context, "Updated Successfully!", Toast.LENGTH_SHORT).show();

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
        this.loadUserImg();
    }

    private void loadInfoFromFirstFragment() {
        final Bundle bundle1 = this.inputFragments[0].getInputs();
        this.user
                .setName(bundle1.getString(UserInputFragment1.NAME_KEY, user.getName()))
                .setSurname(bundle1.getString(UserInputFragment1.SURNAME_KEY, user.getSurname()))
                .setEmail(bundle1.getString(UserInputFragment1.EMAIL_KEY, user.getEmail()))
                .setPassword(bundle1.getString(UserInputFragment1.PASSWORD_KEY, user.getPassword()))
                ;
        Serializable birthdate = bundle1.getSerializable(UserInputFragment1.BIRTHDATE_KEY);
        if (birthdate instanceof Date)
            this.user.setBirthdate((Date) birthdate);
    }

    private void loadInfoFromSecondFragment() {
        final Bundle bundle2 = this.inputFragments[1].getInputs();
        this.user
                .setPhoneNumber(bundle2.getString(UserInputFragment2.PHONE_KEY, user.getPhoneNumber()))
                .setCountry(bundle2.getString(UserInputFragment2.COUNTRY_KEY, user.getCountry()))
                .setCity(bundle2.getString(UserInputFragment2.CITY_KEY, user.getCity()))
                .setAddress(bundle2.getString(UserInputFragment2.ADDRESS_KEY, user.getAddress()))
        ;
    }

    private void loadUserImg() {
        final Bundle bundle2 = this.inputFragments[2].getInputs();
        final byte[] data = bundle2.getByteArray(UserInputFragment3.PHOTO_KEY);
        if (data != null)
            this.userImg = BitmapFactory.decodeByteArray(data, 0, data.length);
    }
}
