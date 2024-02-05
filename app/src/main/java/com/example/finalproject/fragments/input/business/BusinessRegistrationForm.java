package com.example.finalproject.fragments.input.business;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.fragments.input.InputForm;
import com.example.finalproject.util.Result;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.function.Consumer;

public class BusinessRegistrationForm extends InputForm {
    // The branch's company name:
    private String companyName;

    // The branch's password:
    private String branchPassword;

    // The opening and closing time of the branch:
    private int openingTimeMinutes, closingTimeMinutes;

    // The number of shifts every day for the branch:
    private int[] weeklyShiftsNum;

    // The location of the branch:
    private String country, city, address;

    // Tag for debugging purposes:
    private static final String TAG = "BusinessRegistrationForm";

    // Error message in case a similar branch was found:
    private static final String SIMILAR_BRANCH_FOUND_ERROR = "Similar branch found";

    public BusinessRegistrationForm(Resources res, String userCountry) {
        super(
                // Set the title:
                res.getString(R.string.act_business_input_title),

                // Set the input fragments:
                new BusinessInputFragment1(),
                new BusinessInputFragment2(userCountry)
        );
    }

    @Override
    public void onEndForm(Context context, Consumer<Result<Void, Exception>> onCompleteListener) {
        // Load info from the fragments:
        this.loadFragmentsInfo();

        // Check that a similar company doesn't already have a branch at that location:
        this.validateSimilarBranch(unused -> {
            // TODO: Add the new branch:
        }, exception-> {
            // Log the error and toast it:
            Log.e(TAG, "Failed to validate branch", exception);

            if (exception.getMessage() != null && exception.getMessage().equals(SIMILAR_BRANCH_FOUND_ERROR))
                Toast.makeText(context, SIMILAR_BRANCH_FOUND_ERROR, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
        });
    }

    private void validateSimilarBranch(OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        // Get a reference to firestore:
        final FirebaseFirestore dbRef = FirebaseFirestore.getInstance();

        // Check for similar branches:
        final String fullAddress = String.format("%s %s %s", this.country, this.city, this.address);
        dbRef.collection("branches")
                .whereEqualTo("companyName", this.companyName)
                .whereEqualTo("fullAddress", fullAddress)
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

    private void loadFragmentsInfo() {
        // Load every field from the first fragment:
        final Bundle bundle1 = this.inputFragments[0].getInputs();
        this.companyName = bundle1.getString(BusinessInputFragment1.COMPANY_NAME_KEY);
        this.branchPassword = bundle1.getString(BusinessInputFragment1.BRANCH_PASSWORD_KEY);
        this.openingTimeMinutes = bundle1.getInt(BusinessInputFragment1.OPENING_TIME_MINUTES_KEY);
        this.closingTimeMinutes = bundle1.getInt(BusinessInputFragment1.CLOSING_TIME_MINUTES_KEY);
        this.weeklyShiftsNum = bundle1.getIntArray(BusinessInputFragment1.WEEKLY_SHIFTS_NUM_KEY);

        // Load every field from the second fragment:
        final Bundle bundle2 = this.inputFragments[1].getInputs();
        this.country = bundle2.getString(BusinessInputFragment2.SELECTED_COUNTRY_KEY);
        this.city = bundle2.getString(BusinessInputFragment2.SELECTED_CITY_KEY);
        this.address = bundle2.getString(BusinessInputFragment2.SELECTED_ADDRESS_KEY);
    }
}
