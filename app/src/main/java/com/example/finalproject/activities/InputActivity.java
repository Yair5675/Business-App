package com.example.finalproject.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.AnimRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.fragments.input.InputFragment1;
import com.example.finalproject.fragments.input.InputFragment2;
import com.example.finalproject.fragments.input.InputFragment3;
import com.example.finalproject.database.AppDatabase;
import com.example.finalproject.database.entities.User;

public class InputActivity extends AppCompatActivity implements View.OnClickListener {
    // A pointer to the database:
    private AppDatabase db;

    // Whether the input activity is registering a new user or updating the details of an existing
    // user:
    private boolean isRegisterActivity;

    // The current page displayed:
    private int currentPage;

    // The user created in the activity:
    private User user;

    // The three pages of the input form:
    private InputFragment1 firstPage;
    private InputFragment2 secondPage;
    private InputFragment3 thirdPage;

    // The inputs from the three pages:
    private InputFragment1.PackagedInfo firstPageInfo;
    private InputFragment2.PackagedInfo secondPageInfo;
    private Bitmap userImg;

    // The buttons that go forwards or backwards in the pages:
    private Button btnNext, btnPrev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        // Initialize the database:
        this.db = AppDatabase.getInstance(this);

        // If a user was already connected, the activity will update their details. If not, it will
        // register a new user:
        this.isRegisterActivity = !AppDatabase.isUserLoggedIn();
        final TextView title = findViewById(R.id.actInputTitle);

        this.user = isRegisterActivity ? new User() : AppDatabase.getConnectedUser();
        title.setText(isRegisterActivity ? R.string.act_input_title_register : R.string.act_input_title_update);

        // Initializing the pages (third one will be initialized later):
        this.firstPage = new InputFragment1();

        // Initialize the next and previous buttons:
        this.btnNext = findViewById(R.id.actInputBtnNextOrRegister);
        this.btnPrev = findViewById(R.id.actInputBtnBackOrCancel);
        this.btnNext.setOnClickListener(this);
        this.btnPrev.setOnClickListener(this);

        // Loading the first page:
        this.currentPage = 1;
        this.loadFirstPage(-1, -1);

        // Implement custom back navigation when the user presses the back button:
        this.loadBackButtonCallback();
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
        // Creating the second page if it hadn't been created already:
        if (this.secondPage == null)
            this.secondPage = new InputFragment2();

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
        // Creating the third page if it hadn't been created already:
        if (this.thirdPage == null)
            this.thirdPage = new InputFragment3();

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
        // Enter the inputs to the user:
        // TODO: Change details after all fragments are updated
        this.user.setName(this.firstPageInfo.NAME);
        this.user.setSurname(this.firstPageInfo.SURNAME);
        this.user.setBirthdate(this.firstPageInfo.BIRTHDATE);
        this.user.setPhoneNumber(this.secondPageInfo.PHONE);
        this.user.setEmail(this.firstPageInfo.EMAIL);
        this.user.setPassword(this.firstPageInfo.PASSWORD);

        this.user.setPictureFileName("TEMPORARY");

        // If the user is new, add them to the database. If not, update them:
        if (this.isRegisterActivity)
            this.db.userDao().insert(this.user);
        else
            this.db.userDao().update(this.user);

        // Signal the user they registered successfully:
        Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_SHORT).show();

        // Return to the main activity:
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}