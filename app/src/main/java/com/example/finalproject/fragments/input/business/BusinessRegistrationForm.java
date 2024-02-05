package com.example.finalproject.fragments.input.business;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import com.example.finalproject.R;
import com.example.finalproject.fragments.input.InputForm;
import com.example.finalproject.util.Result;
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

        // TODO: Check that a similar company doesn't already have a branch at that location:

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
