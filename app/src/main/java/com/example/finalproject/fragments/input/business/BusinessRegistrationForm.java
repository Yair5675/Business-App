package com.example.finalproject.fragments.input.business;

import static com.example.finalproject.util.Constants.SIMILAR_BRANCH_FOUND_ERROR;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.Employee;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.database.online.collections.Workplace;
import com.example.finalproject.fragments.input.InputForm;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.Result;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.function.Consumer;

public class BusinessRegistrationForm extends InputForm {
    // A reference to firestore:
    private final FirebaseFirestore dbRef;

    // The created branch:
    private Branch branch;

    // The connected user:
    private final User connectedUser;

    // The branch's company name:
    private String companyName;

    // The branch's password:
    private String branchPassword;

    // The opening and closing time of the branch:
    private int openingTimeMinutes, closingTimeMinutes;

    // The number of shifts every day for the branch:
    private List<Integer> weeklyShiftsNum;

    // The location of the branch:
    private String country, city, address;

    // Tag for debugging purposes:
    private static final String TAG = "BusinessRegistrationForm";


    public BusinessRegistrationForm(Resources res, User connectedUser) {
        super(
                // Set the title:
                res.getString(R.string.act_business_input_title),

                // Set the input fragments:
                new BusinessInputFragment1(null),
                new BusinessInputFragment2(connectedUser.getCountry(), null)
        );

        // Save the connected user:
        this.connectedUser = connectedUser;

        // Initialize the database:
        this.dbRef = FirebaseFirestore.getInstance();
    }

    @Override
    public void onEndForm(Context context, Consumer<Result<Void, Exception>> onCompleteListener) {
        // Load info from the fragments:
        this.loadFragmentsInfo();

        // Create the branch object:
        this.loadBranchObject();

        // Check that a similar company doesn't already have a branch at that location:
        final OnFailureListener onFailureListener = getOnFailureListener(context, onCompleteListener);
        this.validateSimilarBranch(unused -> {
            // Create a new batch write:
            FirebaseFirestore dbRef = FirebaseFirestore.getInstance();
            WriteBatch batch = dbRef.batch();

            // Add the new branch and set the ID to the object:
            final DocumentReference branchRef = dbRef.collection("branches").document();
            this.branch.setBranchId(branchRef.getId());
            batch.set(branchRef, this.branch, SetOptions.merge());

            // Add the connected user to the list of employees as a manager:
            final Employee employee = Employee.fromUser(this.connectedUser, true);
            final DocumentReference employeeRef = branchRef.collection("employees")
                    .document(employee.getUid());
            batch.set(employeeRef, employee, SetOptions.merge());

            // Add the new branch to the list of workplaces in the connected user's document:
            final Workplace workplace = Workplace.fromBranch(this.branch, true);
            final DocumentReference workplaceRef = dbRef.collection("users")
                    .document(this.connectedUser.getUid())
                    .collection("workplaces")
                    .document(workplace.getBranchId());
            batch.set(workplaceRef, workplace, SetOptions.merge());

            // Commit the batch:
            batch.commit().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Business added successfully!", Toast.LENGTH_SHORT).show();
                    onCompleteListener.accept(Result.success(null));
                }
                else if (task.getException() != null)
                    onCompleteListener.accept(Result.failure(task.getException()));
                else
                    onCompleteListener.accept(Result.failure(new Exception("Something went wrong")));
            });
        }, onFailureListener);
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

    private void loadBranchObject() {
        this.branch = new Branch();
        branch.setCompanyName(this.companyName);
        branch.setPassword(this.branchPassword);
        branch.setCountry(this.country);
        branch.setCity(this.city);
        branch.setAddress(this.address);
        branch.setFullAddress(String.format("%s %s %s", this.country, this.city, this.address));
        branch.setOpeningTime(this.openingTimeMinutes);
        branch.setClosingTime(this.closingTimeMinutes);
        branch.setDailyShiftsNum(this.weeklyShiftsNum);
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
        this.weeklyShiftsNum = bundle1.getIntegerArrayList(BusinessInputFragment1.WEEKLY_SHIFTS_NUM_KEY);

        // Load every field from the second fragment:
        final Bundle bundle2 = this.inputFragments[1].getInputs();
        this.country = bundle2.getString(BusinessInputFragment2.SELECTED_COUNTRY_KEY);
        this.city = bundle2.getString(BusinessInputFragment2.SELECTED_CITY_KEY);
        this.address = bundle2.getString(BusinessInputFragment2.SELECTED_ADDRESS_KEY);
    }
}
