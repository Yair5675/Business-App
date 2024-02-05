package com.example.finalproject.fragments.input.business;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.input.InputForm;
import com.example.finalproject.util.Result;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
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

    // Error message in case a similar branch was found:
    private static final String SIMILAR_BRANCH_FOUND_ERROR = "Similar branch found";

    public BusinessRegistrationForm(Resources res, User connectedUser) {
        super(
                // Set the title:
                res.getString(R.string.act_business_input_title),

                // Set the input fragments:
                new BusinessInputFragment1(),
                new BusinessInputFragment2(connectedUser.getCountry())
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

        // Check that a similar company doesn't already have a branch at that location:
        final OnFailureListener onFailureListener = getOnFailureListener(context, onCompleteListener);
        this.validateSimilarBranch(unused -> {
            // Add the new branch:
            this.addNewBranch(unused1 -> {
                // Add the connected user to the list of employees as a manager:
                this.addUserToListAsManager(unused2 -> {
                    // Add the branch to the list of workplaces of the user:
                    this.addBranchToWorkplacesList(documentReference -> {
                        // Everything was done, activate the onComplete listener:
                        onCompleteListener.accept(Result.success(null));
                    }, onFailureListener);

                }, onFailureListener);
            }, onFailureListener);
        }, onFailureListener);
    }

    private void addBranchToWorkplacesList(
            OnSuccessListener<DocumentReference> onSuccessListener, OnFailureListener onFailureListener
    ) {
        // Load some of the branch's info:
        final HashMap<String, Object> branchInfo = new HashMap<>();
        branchInfo.put("branchId", this.branch.getBranchId());
        branchInfo.put("isManager", true);
        branchInfo.put("companyName", this.branch.getCompanyName());
        branchInfo.put("address", this.branch.getAddress());
        branchInfo.put("city", this.branch.getCity());
        branchInfo.put("country", this.branch.getCountry());

        this.dbRef.collection("users")
                .document(this.connectedUser.getUid())
                .collection("workplaces")
                .add(branchInfo)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(exception -> {
                    // Delete the user info from the employees' list:
                    this.dbRef.collection("branches")
                            .document(this.branch.getBranchId())
                            .collection("employees")
                            .document(this.connectedUser.getUid())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                // Delete the branch from the database:
                                this.dbRef.collection("branches")
                                        .document(this.branch.getBranchId())
                                        .delete();
                            });
                    // Activate the onFailureListener:
                    onFailureListener.onFailure(exception);
                });
    }

    private void addUserToListAsManager(
            OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Load some of the user's info:
        final HashMap<String, Object> userInfo = new HashMap<>();
        userInfo.put("uid", this.connectedUser.getUid());
        userInfo.put("isManager", true);
        userInfo.put("name", this.connectedUser.getName());
        userInfo.put("surname", this.connectedUser.getSurname());
        userInfo.put("imagePath", this.connectedUser.getImagePath());

        this.dbRef.collection("branches")
                .document(this.branch.getBranchId())
                .collection("employees")
                .document(this.connectedUser.getUid())
                .set(userInfo)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(exception -> {
                    // Delete the created branch:
                    this.dbRef.collection("branches").document(this.branch.getBranchId()).delete();

                    // Activate the onFailureListener:
                    onFailureListener.onFailure(exception);
                });
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

    private void addNewBranch(OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        // Load the new branch object:
        this.loadBranchObject();

        // Set the branch in the adapter:
        this.dbRef.collection("branches")
                .add(this.branch)
                .addOnSuccessListener(documentReference -> {
                    // Set the branch's ID:
                    this.branch.setBranchId(documentReference.getId());
                    this.dbRef.collection("branches")
                            .document(this.branch.getBranchId())
                            .update("branchId", this.branch.getBranchId())
                            .addOnSuccessListener(onSuccessListener)
                            .addOnFailureListener(onFailureListener);
                })
                .addOnFailureListener(onFailureListener);

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
