package com.example.finalproject.fragments.input;

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
     */
    public abstract void onEndForm();
}
