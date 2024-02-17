package com.example.finalproject.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.Util;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class DeleteBranchDialog {
    // The dialog that the class represents:
    private final Dialog dialog;

    // The text view warning the user:
    private final TextView tvWarning;

    // The true password of the branch:
    private final String realPassword;

    // A callback that will be run if the user confirms the deletion:
    private final Runnable onConfirmCallback;

    // The layout with the password input fields in it:
    private final LinearLayout passwordLayout;

    // The password input fields:
    private final TextInputLayout tilPassword;
    private final TextInputEditText etPassword;

    // The confirm and cancel buttons:
    private final Button btnConfirm, btnCancel;

    public DeleteBranchDialog(Context context, String realPassword, Runnable onConfirmCallback) {
        // Inflate the dialog's XML file:
        this.dialog = new Dialog(context);
        this.dialog.setContentView(R.layout.dialog_delete_branch);

        // Save the real password:
        this.realPassword = realPassword;

        // Save the confirm callback:
        this.onConfirmCallback = onConfirmCallback;

        // Set the width of the dialog to 90% the screen, and its height to minimal:
        final Resources res = context.getResources();
        final int width = (int) (res.getDisplayMetrics().widthPixels * 0.9);
        final int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        Window window;
        if ((window = this.dialog.getWindow()) != null)
            window.setLayout(width, height);

        // Loading pointers to the views inside the dialog:
        this.tvWarning = this.dialog.findViewById(R.id.dialogDeleteBranchTvWarning);
        this.passwordLayout = this.dialog.findViewById(R.id.dialogDeleteBranchPasswordLayout);
        this.tilPassword = this.dialog.findViewById(R.id.dialogDeleteBranchTilPassword);
        this.etPassword = this.dialog.findViewById(R.id.dialogDeleteBranchEtPassword);
        this.btnConfirm = this.dialog.findViewById(R.id.dialogDeleteBranchBtnConfirm);
        this.btnCancel = this.dialog.findViewById(R.id.dialogDeleteBranchBtnCancel);

        // Set the cancel button's onClickListener to dismiss the dialog:
        this.btnCancel.setOnClickListener(_v -> this.dialog.dismiss());
    }

    private void setFirstLevel() {
        // Show the warning text view and hide the password layout:
        this.tvWarning.setVisibility(View.VISIBLE);
        this.passwordLayout.setVisibility(View.GONE);

        // Change the buttons' text to yes and no:
        this.btnCancel.setText(R.string.dialog_delete_branch_btn_no_txt);
        this.btnConfirm.setText(R.string.dialog_delete_branch_btn_yes_txt);

        // Set the confirm button's onClickListener to go to the next level:
        this.btnConfirm.setOnClickListener(_v -> this.setSecondLevel());
    }

    public void show() {
        // Set the first level before showing:
        this.setFirstLevel();

        // Show the dialog:
        this.dialog.show();
    }

    private void setSecondLevel() {
        // Hide the warning text view and show the password layout:
        this.tvWarning.setVisibility(View.GONE);
        this.passwordLayout.setVisibility(View.VISIBLE);

        // Change the buttons' text to cancel and confirm:
        this.btnCancel.setText(R.string.dialog_delete_branch_btn_cancel_txt);
        this.btnConfirm.setText(R.string.dialog_delete_branch_btn_confirm_txt);

        // Clear the password input field and its error:
        this.etPassword.setText("");
        this.tilPassword.setError(null);

        // Set the confirm button's onClickListener to activate the callback and dismiss the dialog:
        this.btnConfirm.setOnClickListener(_v -> {
            // Check the password:
            final String givenPassword = Util.getTextFromEt(this.etPassword);
            if (givenPassword.isEmpty()) {
                this.tilPassword.setError(Constants.MANDATORY_INPUT_ERROR);
            }
            else if (givenPassword.equals(this.realPassword)) {
                this.dialog.dismiss();
                this.onConfirmCallback.run();
            }
            else {
                this.tilPassword.setError("Wrong password given");
            }
        });
    }
}
