package com.example.finalproject.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.AnimRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.database.online.OnlineDatabase;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.user_input.UserInputFragment1;
import com.example.finalproject.fragments.user_input.UserInputFragment2;
import com.example.finalproject.fragments.user_input.UserInputFragment3;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.io.Serializable;

public class InputActivity extends AppCompatActivity implements View.OnClickListener {
    // A pointer to the database:
    private OnlineDatabase db;

    // The user whose info is being edited (if a new user is registered this field will be null):
    private User user;

    // Whether the input activity is registering a new user or updating the details of an existing
    // user:
    private boolean isRegisterActivity;

    // The current page displayed:
    private int currentPage;

    // The three pages of the input form:
    private UserInputFragment1 firstPage;
    private UserInputFragment2 secondPage;
    private UserInputFragment3 thirdPage;

    // The inputs from the three pages:
    private UserInputFragment1.PackagedInfo firstPageInfo;
    private UserInputFragment2.PackagedInfo secondPageInfo;
    private Bitmap userImg;

    // The buttons that go forwards or backwards in the pages:
    private Button btnNext, btnPrev;

    // The progress bar that will be shown while the user's details are saved in the database:
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        // Initialize the database:
        this.db = OnlineDatabase.getInstance();

        // Try to load the user from the intent and check if this is a register activity:
        this.loadUserFromIntent();
        this.isRegisterActivity = this.user == null;

        // Set the title accordingly:
        final TextView title = findViewById(R.id.actInputTitle);
        title.setText(isRegisterActivity ? R.string.act_input_title_register : R.string.act_input_title_update);

        // Initializing the pages and giving the user to them (it's ok if we are giving null):
        this.firstPage = new UserInputFragment1(this.user);
        this.secondPage = new UserInputFragment2(this.user);
        this.thirdPage = new UserInputFragment3(this.user);

        // Initialize the next and previous buttons:
        this.btnNext = findViewById(R.id.actInputBtnNextOrRegister);
        this.btnPrev = findViewById(R.id.actInputBtnBackOrCancel);
        this.btnNext.setOnClickListener(this);
        this.btnPrev.setOnClickListener(this);

        // Initialize the progress bar and make it disappear:
        this.progressBar = findViewById(R.id.actInputProgressBar);
        this.progressBar.setVisibility(View.GONE);

        // Loading the first page:
        this.currentPage = 1;
        this.loadFirstPage(-1, -1);

        // Implement custom back navigation when the user presses the back button:
        this.loadBackButtonCallback();
    }

    private void loadUserFromIntent() {
        // Get the intent:
        final Intent intent = getIntent();

        // Check if a user was given:
        if (intent.hasExtra("user")) {
            // Perform type checking (just in case):
            Serializable user = intent.getSerializableExtra("user");
            if (user instanceof User)
                this.user = (User) user;
        }
    }

    private void loadBackButtonCallback() {
        // Define the callback:
        final OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveBackwards();
            }
        };
        // Add the callback:
        getOnBackPressedDispatcher().addCallback(callback);
    }

    private void moveBackwards() {
        // Check the current page:
        if (currentPage == 1) {
            // Return to the home activity:
            final Intent intent = new Intent(InputActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            // Lower the current page by one and go to that page:
            this.currentPage--;
            if (currentPage == 1)
                this.loadFirstPage(R.anim.slide_in_to_right, R.anim.slide_out_to_right);
            else
                this.loadSecondPage(R.anim.slide_in_to_right, R.anim.slide_out_to_right);
        }
    }

    private void moveForward() {
        switch (this.currentPage) {
            case 1: {
                // Validate the inputs:
                if (this.firstPage.areInputsValid()) {
                    // Get the info from the page:
                    this.firstPageInfo = this.firstPage.getPackagedInfo();

                    // Load the next page:
                    this.currentPage++;
                    this.loadSecondPage(R.anim.slide_in_to_left, R.anim.slide_out_to_left);
                }
                break;
            }
            case 2: {
                // Validate the inputs:
                if (this.secondPage.areInputsValid()) {
                    // Get the info from the page:
                    this.secondPageInfo = this.secondPage.getPackagedInfo();

                    // Load the next page:
                    this.currentPage++;
                    this.loadThirdPage();
                }
                break;
            }
            case 3: {
                // Validate the input:
                if (this.thirdPage.areInputsValid(this)) {
                    // Get the info:
                    this.userImg = this.thirdPage.getBitmapPhoto();

                    // Confirm the details and create a new user/update an existing one:
                    this.finishInputs();
                }
                break;
            }
        }
    }

    private void loadFirstPage(@AnimRes int enter, @AnimRes int exit) {
        // Get a fragment transaction object:
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Show animation:
        if (enter != -1 && exit != -1)
            transaction.setCustomAnimations(enter, exit);

        // Replace the current fragment with the fragment of the first page:
        transaction.replace(R.id.actInputFragmentContainer, this.firstPage);

        // Save change:
        transaction.commit();

        // Set the prev button to display 'cancel' and an X drawable:
        this.btnPrev.setText(R.string.act_input_cancel_btn_text);
        this.btnPrev.setCompoundDrawablesWithIntrinsicBounds(R.drawable.cancel_symbol, 0, 0, 0);

        // Set the next button to display 'next' and an arrow drawable:
        this.btnNext.setText(R.string.act_input_next_btn_text);
        this.btnNext.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_forward, 0);
    }

    private void loadSecondPage(@AnimRes int enter, @AnimRes int exit) {
        // Get a fragment transaction object:
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Show animation:
        if (enter != -1 && exit != -1)
            transaction.setCustomAnimations(enter, exit);

        // Replace the current fragment with the fragment of the second page:
        transaction.replace(R.id.actInputFragmentContainer, this.secondPage);

        // Save change:
        transaction.commit();

        // Set the prev button to display 'previous' and a backwards arrow drawable:
        this.btnPrev.setText(R.string.act_input_back_btn_text);
        this.btnPrev.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_back, 0, 0, 0);

        // Set the next button to display 'next' and an arrow drawable:
        this.btnNext.setText(R.string.act_input_next_btn_text);
        this.btnNext.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_forward, 0);
    }

    private void loadThirdPage() {
        // Get a fragment transaction object:
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Show animation (will always be forward because it's the last page):
        transaction.setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left);

        // Replace the current fragment with the fragment of the third page:
        transaction.replace(R.id.actInputFragmentContainer, this.thirdPage);

        // Save change:
        transaction.commit();

        // Set the prev button to display 'previous' and a backwards arrow drawable:
        this.btnPrev.setText(R.string.act_input_back_btn_text);
        this.btnPrev.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_back, 0, 0, 0);

        // Set the next button to display 'confirm' and a checkmark drawable:
        this.btnNext.setText(R.string.act_input_confirm_btn_text);
        this.btnNext.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark_symbol, 0);
    }

    @Override
    public void onClick(View view) {
        // Get the ID of the view:
        final int ID = view.getId();

        // If the user wants to go forward in the pages:
        if (ID == R.id.actInputBtnNextOrRegister)
            this.moveForward();

        // If the user wants to go backwards in the pages:
        else if (ID == R.id.actInputBtnBackOrCancel)
            this.moveBackwards();
    }

    private void finishInputs() {
        // Register or update the user:
        if (this.isRegisterActivity)
            this.registerNewUser();
        else
            this.updateUser();
    }

    private void updateUser() {
        // Save the old password:
        final String oldPassword = this.user.getPassword();

        // Set the new information in the user's object (except for the new email):
        this.user
                .setName(firstPageInfo.NAME)
                .setSurname(firstPageInfo.SURNAME)
                .setBirthdate(firstPageInfo.BIRTHDATE)
                .setPassword(firstPageInfo.PASSWORD)
                .setPhoneNumber(secondPageInfo.PHONE)
                .setCountry(secondPageInfo.COUNTRY)
                .setCity(secondPageInfo.CITY)
                .setAddress(secondPageInfo.ADDRESS)
        ;

        // Initialize callbacks:
        OnSuccessListener<Void> successListener = unused -> {
            // Signal the user their info was updated successfully:
            Toast.makeText(this, "Updated Successfully!", Toast.LENGTH_SHORT).show();

            // Make the progress bar disappear and the buttons re-appear:
            this.progressBar.setVisibility(View.GONE);
            this.btnNext.setVisibility(View.VISIBLE);
            this.btnPrev.setVisibility(View.VISIBLE);

            // Go to the main activity and send the new email to them:
            final Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("new email", firstPageInfo.EMAIL);
            startActivity(intent);
            finish();
        };
        OnFailureListener failureListener = exception -> {
            // Log the error and signal the user something went wrong:
            Log.e("InputActivity", "Failed to update user", exception);

            // Check if the email is used by another user:
            if (exception instanceof FirebaseAuthUserCollisionException)
                Toast.makeText(
                        this,
                        "The email you entered is already used by another user",
                        Toast.LENGTH_SHORT
                ).show();
            else if (exception.getMessage() != null && exception.getMessage().equals("Existing phone number"))
                Toast.makeText(
                        this,
                        "The phone number you entered is already used by another user",
                        Toast.LENGTH_SHORT
                ).show();
            else {
                Log.e("InputActivity", "User update failed", exception);
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }

            // Make the progress bar disappear and the buttons re-appear:
            this.progressBar.setVisibility(View.GONE);
            this.btnNext.setVisibility(View.VISIBLE);
            this.btnPrev.setVisibility(View.VISIBLE);
        };

        // Show the progress bar and hide the buttons:
        this.progressBar.setVisibility(View.VISIBLE);
        this.btnNext.setVisibility(View.GONE);
        this.btnPrev.setVisibility(View.GONE);

        // Update the email if it is different:
        if (!user.getEmail().equals(firstPageInfo.EMAIL))
            this.db.updateUserEmail(
                    user.getEmail(), firstPageInfo.EMAIL, oldPassword,
                    unused -> Log.d("InputActivity", "Sent verification email"),
                    e -> Log.e("InputActivity", "Failed to change email", e)
            );

        // Update the user in the database
        this.db.updateUser(user, user.getEmail(), oldPassword, this.userImg, successListener, failureListener);
    }

    private void registerNewUser() {
        // Create the user object:
        final User user = new User();
        user
                .setName(firstPageInfo.NAME)
                .setSurname(firstPageInfo.SURNAME)
                .setEmail(firstPageInfo.EMAIL)
                .setBirthdate(firstPageInfo.BIRTHDATE)
                .setPassword(firstPageInfo.PASSWORD)
                .setCountry(secondPageInfo.COUNTRY)
                .setCity(secondPageInfo.CITY)
                .setAddress(secondPageInfo.ADDRESS)
                .setPhoneNumber(secondPageInfo.PHONE)
        ;

        // Initialize callbacks:
        OnSuccessListener<Void> successListener = unused -> {
            // Signal the user they registered successfully:
            Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_SHORT).show();

            // Make the progress bar disappear and the buttons re-appear:
            this.progressBar.setVisibility(View.GONE);
            this.btnNext.setVisibility(View.VISIBLE);
            this.btnPrev.setVisibility(View.VISIBLE);

            // Send a verification email:
            this.db.sendVerificationEmail();

            // Go to the main activity:
            final Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        };
        OnFailureListener failureListener = exception -> {
            // Log the error and signal the user something went wrong:
            Log.e("InputActivity", "Failed to register user", exception);

            // Check if the email already exists:
            if (exception instanceof FirebaseAuthUserCollisionException)
                Toast.makeText(this, "Email is already registered", Toast.LENGTH_SHORT).show();
            else if (exception.getMessage() != null && exception.getMessage().equals("Existing phone number"))
                Toast.makeText(this, "Phone is already registered", Toast.LENGTH_SHORT).show();
            else {
                Log.e("InputActivity", "Failed to register user", exception);
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }

            // Make the progress bar disappear and the buttons re-appear:
            this.progressBar.setVisibility(View.GONE);
            this.btnNext.setVisibility(View.VISIBLE);
            this.btnPrev.setVisibility(View.VISIBLE);
        };

        // Show the progress bar and hide the buttons:
        this.progressBar.setVisibility(View.VISIBLE);
        this.btnNext.setVisibility(View.GONE);
        this.btnPrev.setVisibility(View.GONE);

        // Save them in the database:
        this.db.addNewUser(user, this.userImg, successListener, failureListener);
    }
}