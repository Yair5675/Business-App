package com.example.finalproject.fragments.input;

import android.content.Context;

import com.example.finalproject.util.Result;

import java.util.function.Consumer;

public abstract class InputForm {
    // The title of the form:
    protected final String title;

    // The title that appears on the toolbar:
    protected final String toolbarTitle;

    // The index of the current page:
    protected int currentPageIdx;

    // An array of the fragments in the form:
    protected final InputFragment[] inputFragments;

    public InputForm(String title, String toolbarTitle, InputFragment ... inputFragments) {
        this.title = title;
        this.toolbarTitle = toolbarTitle;
        this.inputFragments = inputFragments;
        this.currentPageIdx = 0;
    }

    public String getTitle() {
        return title;
    }

    public String getToolbarTitle() {
        return toolbarTitle;
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
     * function is not responsible for validating its fragments). The function is responsible to
     * start another activity and to exit the current activity. The caller of the function is
     * responsible for finishing the activity.
     * @param context Context of the activity that calls the end form method.
     * @param onCompleteListener A callback that will be run once the method is finished. The
     *                           callback receives the result of the end form, which signals if it
     *                           was successful or failed.
     */
    public abstract void onEndForm(Context context, Consumer<Result<Void, Exception>> onCompleteListener);
}
