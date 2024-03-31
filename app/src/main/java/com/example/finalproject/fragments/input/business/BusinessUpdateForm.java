package com.example.finalproject.fragments.input.business;

import static com.example.finalproject.util.Constants.SIMILAR_BRANCH_FOUND_ERROR;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.finalproject.R;
import com.example.finalproject.activities.BranchActivity;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.input.InputForm;
import com.example.finalproject.util.Result;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.function.Consumer;

public class BusinessUpdateForm extends InputForm {
    // The currently connected user (needed when activation thee branch activity):
    private final User user;

    // The branch which is being updated:
    private final Branch oldBranch;

    // A reference to the online database:
    private final FirebaseFirestore dbRef;

    // Tag for debugging purposes:
    private static final String TAG = "BusinessUpdateForm";

    public BusinessUpdateForm(@NonNull Branch branch, @NonNull User user, Resources res) {
        super(
                // Set the title:
                res.getString(R.string.act_business_input_title),

                // Set the toolbar's title:
                res.getString(R.string.business_update_form_toolbar_title),

                // Set the fragments with the branch:
                new BusinessInputFragment1(branch),
                new BusinessInputFragment2(branch.getCountry(), branch)
        );

        // Save the branch and the user:
        this.user = user;
        this.oldBranch = branch;

        // Create a reference to the database:
        this.dbRef = FirebaseFirestore.getInstance();
    }

    @Override
    public void onEndForm(Context context, Consumer<Result<Void, Exception>> onCompleteListener) {
        // Get the updated branch:
        final Branch branch = this.getBranchFromFragments();

        // Get the on failure listener:
        final OnFailureListener onFailureListener = this.getOnFailureListener(context, onCompleteListener);

        // Check that there are no similar branches:
        this.validateSimilarBranch(
                branch,
                unused -> {
                    // Update the branch's document in the database:
                    this.dbRef
                            .collection("branches")
                            .document(branch.getBranchId())
                            .set(branch, SetOptions.merge())
                            .addOnSuccessListener(unused1 -> {
                                // Alert the user:
                                Toast.makeText(context, "Business updated successfully!", Toast.LENGTH_SHORT).show();

                                // Go to the branch activity with the updated branch and the user:
                                final Intent intent = new Intent(context, BranchActivity.class);
                                intent.putExtra("user", this.user);
                                intent.putExtra("branch", branch);
                                context.startActivity(intent);

                                // Activate the callback:
                                onCompleteListener.accept(Result.success(null));
                            })
                            .addOnFailureListener(onFailureListener);
                }, onFailureListener
        );
    }

    private OnFailureListener getOnFailureListener(Context context, Consumer<Result<Void, Exception>> callback) {
        return exception -> {
            // Log the error and toast it:
            Log.e(TAG, "Ending form failed", exception);

            // Check for similar branch error:
            if (exception.getMessage() != null && exception.getMessage().equals(SIMILAR_BRANCH_FOUND_ERROR))
                Toast.makeText(context, SIMILAR_BRANCH_FOUND_ERROR, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();

            // Activate the callback:
            callback.accept(Result.failure(exception));
        };
    }


    private void validateSimilarBranch(
            Branch updatedBranch,
            OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Check for similar branches:
        this.dbRef.collection("branches")
                .whereNotEqualTo("branchId", updatedBranch.getBranchId())
                .whereEqualTo("companyName", updatedBranch.getCompanyName())
                .whereEqualTo("fullAddress", updatedBranch.getFullAddress())
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Check that there is no similar branch:
                    if (queryDocumentSnapshots.isEmpty())
                        onSuccessListener.onSuccess(null);
                    else
                        onFailureListener.onFailure(new Exception(SIMILAR_BRANCH_FOUND_ERROR));
                })
                .addOnFailureListener(onFailureListener);
    }

    private Branch getBranchFromFragments() {
        // Create an empty branch and set the ID to be the same as the old branch:
        final Branch branch = new Branch();
        branch.setBranchId(this.oldBranch.getBranchId());

        // Load the inputs from the first fragment:
        this.loadFirstFragmentInputs(branch);

        // Load the inputs from the second fragment:
        this.loadSecondFragmentInputs(branch);

        // Return the branch:
        return branch;
    }

    private void loadFirstFragmentInputs(Branch branch) {
        // Get the inputs:
        final Bundle bundle = this.inputFragments[0].getInputs();

        // Load them into the branch:
        branch.setCompanyName(bundle.getString(BusinessInputFragment1.COMPANY_NAME_KEY));
        branch.setPassword(bundle.getString(BusinessInputFragment1.BRANCH_PASSWORD_KEY));
        branch.setOpeningTime(bundle.getInt(BusinessInputFragment1.OPENING_TIME_MINUTES_KEY));
        branch.setClosingTime(bundle.getInt(BusinessInputFragment1.CLOSING_TIME_MINUTES_KEY));
        branch.setDailyShiftsNum(bundle.getIntegerArrayList(BusinessInputFragment1.WEEKLY_SHIFTS_NUM_KEY));
    }

    private void loadSecondFragmentInputs(Branch branch) {
        // Get the inputs:
        final Bundle bundle = this.inputFragments[1].getInputs();

        // Load them into the branch:
        branch.setCountry(bundle.getString(BusinessInputFragment2.SELECTED_COUNTRY_KEY));
        branch.setCity(bundle.getString(BusinessInputFragment2.SELECTED_CITY_KEY));
        branch.setAddress(bundle.getString(BusinessInputFragment2.SELECTED_ADDRESS_KEY));
        branch.setFullAddress(String.format("%s %s %s", branch.getCountry(), branch.getCity(), branch.getAddress()));
    }
}
