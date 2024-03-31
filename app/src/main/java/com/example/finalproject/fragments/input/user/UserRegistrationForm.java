package com.example.finalproject.fragments.input.user;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.activities.MainActivity;
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

public class UserRegistrationForm extends InputForm {
    // A reference to the online database:
    private final OnlineDatabase db;

    // The user's image:
    private Bitmap userImg;

    public UserRegistrationForm(Resources res) {
        super(
                // Set the title:
                res.getString(R.string.act_input_title_register),

                // Set the title in the toolbar:
                res.getString(R.string.user_registration_form_toolbar_title),

                // Set the fragments:
                new UserInputFragment1(null),
                new UserInputFragment2(null),
                new UserInputFragment3(null)
        );

        // Initialize the online database reference:
        this.db = OnlineDatabase.getInstance();
    }

    @Override
    public void onEndForm(Context context, Consumer<Result<Void, Exception>> onCompleteListener) {
        // Create a new user:
        final User user = this.createNewUser();

        // Initialize callbacks:
        OnSuccessListener<Void> onSuccessListener = getOnSuccessListener(context, onCompleteListener);
        OnFailureListener failureListener = getOnFailureListener(context, onCompleteListener);

        // Save them in the database:
        this.db.addNewUser(user, this.userImg, onSuccessListener, failureListener);
    }

    private OnSuccessListener<Void> getOnSuccessListener(Context context, Consumer<Result<Void, Exception>> callback) {
        return unused -> {
            // Signal the user they registered successfully:
            Toast.makeText(context, "Registered Successfully!", Toast.LENGTH_SHORT).show();

            // Send a verification email:
            this.db.sendVerificationEmail();

            // Go to the main activity:
            final Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);

            // Activate the callback:
            callback.accept(Result.success(null));
        };
    }

    private static OnFailureListener getOnFailureListener(Context context, Consumer<Result<Void, Exception>> callback) {
        return exception -> {
            // Log the error and signal the user something went wrong:
            Log.e("UserRegistrationForm", "Failed to register user", exception);

            // Check if the email already exists:
            if (exception instanceof FirebaseAuthUserCollisionException)
                Toast.makeText(context, "Email is already registered", Toast.LENGTH_SHORT).show();
                // Check if the phone already exists:
            else if (exception.getMessage() != null && exception.getMessage().equals("Existing phone number"))
                Toast.makeText(context, "Phone is already registered", Toast.LENGTH_SHORT).show();
                // If it's another error:
            else {
                Log.e("UserRegistrationForm", "Failed to register user", exception);
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }

            // Activate the callback:
            callback.accept(Result.failure(exception));
        };
    }

    private User createNewUser() {
        final User user = new User();

        // Set inputs from the first two fragment:
        this.loadInfoFromFirstFragment(user);
        this.loadInfoFromSecondFragment(user);

        // Set the user's image:
        this.loadUserImg();

        // Return the new user:
        return user;
    }

    private void loadInfoFromFirstFragment(User user) {
        final Bundle bundle1 = this.inputFragments[0].getInputs();
        user
                .setName(bundle1.getString(UserInputFragment1.NAME_KEY, user.getName()))
                .setSurname(bundle1.getString(UserInputFragment1.SURNAME_KEY, user.getSurname()))
                .setEmail(bundle1.getString(UserInputFragment1.EMAIL_KEY, user.getEmail()))
                .setPassword(bundle1.getString(UserInputFragment1.PASSWORD_KEY, user.getPassword()))
        ;
        Serializable birthdate = bundle1.getSerializable(UserInputFragment1.BIRTHDATE_KEY);
        if (birthdate instanceof Date)
            user.setBirthdate((Date) birthdate);
    }

    private void loadInfoFromSecondFragment(User user) {
        final Bundle bundle2 = this.inputFragments[1].getInputs();
        user
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
