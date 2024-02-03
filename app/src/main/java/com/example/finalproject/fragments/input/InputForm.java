package com.example.finalproject.fragments.input;

import android.content.Context;

import com.example.finalproject.util.Result;
import com.google.android.gms.tasks.OnCompleteListener;

import java.util.function.Consumer;

public abstract class InputForm {
    // The title of the form:
    protected final String title;

    // The index of the current page:
    protected int currentPageIdx;

    // An array of the fragments in the form:
    protected final InputFragment[] inputFragments;

    public InputForm(String title, InputFragment ... inputFragments) {
        this.title = title;
        this.inputFragments = inputFragments;
        this.currentPageIdx = 0;
    }

    public boolean isFirstPage() {
        return this.currentPageIdx == 0;
    }

    public boolean isLastPage() {
        return this.currentPageIdx == this.inputFragments.length - 1;
    }

    public InputFragment getCurrentPage() {
        return this.inputFragments[this.currentPageIdx];
    }

    public void nextPage() {
        if (!this.isLastPage())
            this.currentPageIdx++;
    }

    public void prevPage() {
        if (!this.isFirstPage())
            this.currentPageIdx--;
    }

    /**
     * A function that will be called once every page in the form was filled AND VALIDATED (the
     * function is not responsible for validating its fragments).
     * @param context Context of the activity that calls the end form method.
     * @param onCompleteListener A callback that will be run once the method is finished. The
     *                           callback receives the result of the end form, which signals if it
     *                           was successful or failed.
     */
    public abstract void onEndForm(Context context, Consumer<Result<Void, Exception>> onCompleteListener);
}
