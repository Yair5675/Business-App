package com.example.finalproject.fragments.input.business;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.fragments.input.InputForm;
import com.example.finalproject.util.Result;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.function.Consumer;

public class BusinessRegistrationForm extends InputForm {
    // A reference to firestore:
    private final FirebaseFirestore dbRef;

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

        // Initialize the database:
        this.dbRef = FirebaseFirestore.getInstance();
    }

    @Override
    public void onEndForm(Context context, Consumer<Result<Void, Exception>> onCompleteListener) {
        // Load info from the fragments:
        this.loadFragmentsInfo();

        // Check that a similar company doesn't already have a branch at that location:
        final OnFailureListener onFailureListener = getOnFailureListener(context);
        this.validateSimilarBranch(unused -> {
            // Add the new branch:
            this.addNewBranch(unused1 -> {
                // TODO: Add the connected user to the list of employees as a manager
            }, onFailureListener);
        }, onFailureListener);
    }

    private OnFailureListener getOnFailureListener(Context context) {
        return exception -> {
            // Log the error and toast it:
            Log.e(TAG, "Ending form failed", exception);

            // Check for similar branch error:
            if (exception.getMessage() != null && exception.getMessage().equals(SIMILAR_BRANCH_FOUND_ERROR))
                Toast.makeText(context, SIMILAR_BRANCH_FOUND_ERROR, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
        };
    }

    private void addNewBranch(OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        // Create the new branch object:
        final Branch branch = this.getBranchObject();

        // Set the branch in the adapter:
        this.dbRef.collection("branches")
                .add(branch)
                .addOnSuccessListener(documentReference -> {
                    // Set the branch's ID:
                    branch.setBranchId(documentReference.getId());
                    this.dbRef.collection("branches")
                            .document(branch.getBranchId())
                            .update("branchId", branch.getBranchId())
                            .addOnSuccessListener(onSuccessListener)
                            .addOnFailureListener(onFailureListener);
                })
                .addOnFailureListener(onFailureListener);

    }

    private Branch getBranchObject() {
        final Branch branch = new Branch();
        branch.setCompanyName(this.companyName);
        branch.setPassword(this.branchPassword);
        branch.setCountry(this.country);
        branch.setCity(this.city);
        branch.setAddress(this.address);
        branch.setFullAddress(String.format("%s %s %s", this.country, this.city, this.address));
        branch.setOpeningTime(this.openingTimeMinutes);
        branch.setClosingTime(this.closingTimeMinutes);
        branch.setDailyShiftsNum(this.weeklyShiftsNum);

        return branch;
    }

    private void validateSimilarBranch(OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        // Check for similar branches:
        final String fullAddress = String.format("%s %s %s", this.country, this.city, this.address);
        this.dbRef.collection("branches")
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
