package com.example.finalproject.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.broadcast_receivers.OnInternetConnectivityChanged;
import com.example.finalproject.fragments.input.InputForm;
import com.example.finalproject.util.Util;

public class InputActivity extends AppCompatActivity implements OnInternetConnectivityChanged {
    // The buttons that go forwards or backwards in the pages:
    private Button btnNext, btnPrev;

    // The progress bar that will be shown while the user's details are saved in the database:
    private ProgressBar progressBar;

    // The dialog that appears when there is no wifi:
    private Dialog noInternetDialog;

    // The current input form:
    private static InputForm currentForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        // Set the title according to the form:
        final TextView title = findViewById(R.id.actInputTitle);
        title.setText(currentForm.getTitle());

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

        // Initialize the toolbar:
        final Toolbar toolbar = findViewById(R.id.actInputToolbar);
        toolbar.setTitle(currentForm.getToolbarTitle());
        this.setSupportActionBar(toolbar);

        // Show back button on the toolbar:
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Check for the toolbar's back button:
        if (item.getItemId() == android.R.id.home) {
            this.closeInputForm();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeInputForm() {
        // Create an alert dialog asking if the want to exit:
        final AlertDialog exitDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.act_input_exit_dialog_title)
                .setMessage(R.string.act_input_exit_dialog_message)
                .setPositiveButton("Yes", (dialogInterface, i) -> finish())
                .setNegativeButton("No", ((dialogInterface, i) -> {}))
                .create();

        // Show the exit dialog:
        exitDialog.show();
    }

    private void loadCurrentPage(@AnimRes int enter, @AnimRes int exit) {
        // Get a fragment transaction object:
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Show animation:
        if (enter != -1 && exit != -1)
            transaction.setCustomAnimations(enter, exit);

        // Replace the current fragment with the updated fragment:
        transaction.replace(R.id.actInputFragmentContainer, currentForm.getCurrentPage());

        // Save change:
        transaction.commit();
        transaction.runOnCommit(this::updateNavigationButtons);
    }

    private void updateNavigationButtons() {
        // If it's the first page, show the cancel symbol:
        if (currentForm.isFirstPage()) {
            this.btnPrev.setText(R.string.act_input_cancel_btn_text);
            this.btnPrev.setCompoundDrawablesWithIntrinsicBounds(R.drawable.cancel_symbol, 0, 0, 0);
        }
        else {
            this.btnPrev.setText(R.string.act_input_back_btn_text);
            this.btnPrev.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_back, 0, 0, 0);
        }
        this.btnPrev.setCompoundDrawablePadding(8);

        // If it's the last page, show the confirm button:
        if (currentForm.isLastPage()) {
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
        if (currentForm.isFirstPage()) {
            this.closeInputForm();
        }
        else {
            // Go to the previous page and load it:
            currentForm.prevPage();
            this.loadCurrentPage(R.anim.slide_in_to_right, R.anim.slide_out_to_right);
        }
    }

    private void moveForward() {
        // Validate the current page:
        if (currentForm.getCurrentPage().validateAndSetError()) {
            // If it's the last activity:
            if (currentForm.isLastPage())
                endForm();
            else {
                currentForm.nextPage();
                this.loadCurrentPage(R.anim.slide_in_to_left, R.anim.slide_out_to_left);
            }
        }
    }

    private void endForm() {
        // Show the progress bar and hide the buttons:
        this.progressBar.setVisibility(View.VISIBLE);
        this.btnNext.setVisibility(View.GONE);
        this.btnPrev.setVisibility(View.GONE);

        // End the form:
        currentForm.onEndForm(this, result -> {
            // If the result is a success, clear the current input and finish the activity:
            if (result.isOk()) {
                currentForm = null;
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

    public static void setCurrentInputForm(InputForm inputForm) {
        InputActivity.currentForm = inputForm;
    }

    @Override
    public void onInternetAvailable() {
        // Create the dialog if it wasn't created already:
        if (this.noInternetDialog == null)
            this.noInternetDialog = Util.getNoInternetDialog(this);
        this.noInternetDialog.dismiss();
    }

    @Override
    public void onInternetUnavailable() {
        // Create the dialog if it wasn't created already:
        if (this.noInternetDialog == null)
            this.noInternetDialog = Util.getNoInternetDialog(this);
        this.noInternetDialog.show();
    }
}