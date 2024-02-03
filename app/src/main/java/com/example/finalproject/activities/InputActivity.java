package com.example.finalproject.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.AnimRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.fragments.input.InputForm;

public class InputActivity extends AppCompatActivity {
    // The current input form:
    private InputForm currentForm;

    // The buttons that go forwards or backwards in the pages:
    private Button btnNext, btnPrev;

    // The progress bar that will be shown while the user's details are saved in the database:
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        // Get the current input form:
        this.currentForm = CurrentInput.getCurrentInputForm();

        // Set the title according to the form:
        final TextView title = findViewById(R.id.actInputTitle);
        title.setText(this.currentForm.getTitle());

        // Initialize the next and previous buttons:
        this.btnNext = findViewById(R.id.actInputBtnNextOrRegister);
        this.btnPrev = findViewById(R.id.actInputBtnBackOrCancel);
        this.btnNext.setVisibility(View.VISIBLE);
        this.btnPrev.setVisibility(View.VISIBLE);
        this.btnNext.setOnClickListener(_v -> this.moveForward());
        this.btnPrev.setOnClickListener(_v -> this.moveBackwards());

        // Initialize the progress bar and make it disappear:
        this.progressBar = findViewById(R.id.actInputProgressBar);
        this.progressBar.setVisibility(View.GONE);

        // Load the page:
        this.loadCurrentPage(-1, -1);

        // Implement custom back navigation when the user presses the back button:
        this.loadBackButtonCallback();

    }

    private void loadCurrentPage(@AnimRes int enter, @AnimRes int exit) {
        // Get a fragment transaction object:
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Show animation:
        if (enter != -1 && exit != -1)
            transaction.setCustomAnimations(enter, exit);

        // Replace the current fragment with the updated fragment:
        transaction.replace(R.id.actInputFragmentContainer, this.currentForm.getCurrentPage());

        // Save change:
        transaction.commit();
        transaction.runOnCommit(this::updateNavigationButtons);
    }

    private void updateNavigationButtons() {
        // If it's the first page, show the cancel symbol:
        if (this.currentForm.isFirstPage()) {
            this.btnPrev.setText(R.string.act_input_cancel_btn_text);
            this.btnPrev.setCompoundDrawablesWithIntrinsicBounds(R.drawable.cancel_symbol, 0, 0, 0);
        }
        else {
            this.btnPrev.setText(R.string.act_input_back_btn_text);
            this.btnPrev.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_back, 0, 0, 0);
        }
        this.btnPrev.setCompoundDrawablePadding(8);

        // If it's the last page, show the confirm button:
        if (this.currentForm.isLastPage()) {
            this.btnNext.setText(R.string.act_input_confirm_btn_text);
            this.btnNext.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark_symbol, 0);
        }
        else {
            this.btnNext.setText(R.string.act_input_next_btn_text);
            this.btnNext.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_forward, 0);
        }
        this.btnNext.setCompoundDrawablePadding(8);
    }

    private void loadBackButtonCallback() {
        // Define the callback:
        final OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Go back only if the activity isn't loading:
                if (progressBar.getVisibility() == View.GONE)
                    moveBackwards();
            }
        };
        // Add the callback:
        getOnBackPressedDispatcher().addCallback(callback);
    }

    private void moveBackwards() {
        // If it's the first page, go back to the main activity:
        if (this.currentForm.isFirstPage()) {
            final Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            // Go to the previous page and load it:
            this.currentForm.prevPage();
            this.loadCurrentPage(R.anim.slide_in_to_right, R.anim.slide_out_to_right);
        }
    }

    private void moveForward() {
        // If it's the last activity:
        if (this.currentForm.isLastPage()) {
            // Validate the current page:
            final boolean isValid = this.currentForm.getCurrentPage().validateAndSetError();
            if (isValid)
                endForm();
        }
        // If not, just go to the next page:
        else {
            this.currentForm.nextPage();
            this.loadCurrentPage(R.anim.slide_in_to_left, R.anim.slide_out_to_left);
        }
    }

    private void endForm() {
        // Show the progress bar and hide the buttons:
        this.progressBar.setVisibility(View.VISIBLE);
        this.btnNext.setVisibility(View.GONE);
        this.btnPrev.setVisibility(View.GONE);

        // End the form:
        this.currentForm.onEndForm(this, result -> {
            // If the result is a success, go to the main activity:
            if (result.isOk()) {
                final Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            // If not, just show the buttons again:
            else {
                this.progressBar.setVisibility(View.GONE);
                this.btnPrev.setVisibility(View.VISIBLE);
                this.btnNext.setVisibility(View.VISIBLE);
            }
        });
    }

    public static class CurrentInput {
        // The current input form:
        private static InputForm currentInputForm;

        public static InputForm getCurrentInputForm() {
            return currentInputForm;
        }

        public static void setCurrentInputForm(InputForm currentInputForm) {
            CurrentInput.currentInputForm = currentInputForm;
        }
    }
}