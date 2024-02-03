package com.example.finalproject.fragments.input;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public abstract class InputFragment extends Fragment {
    /**
     * Checks the given input and sets errors to all invalid input fields.
     * @return True if every input is valid, false otherwise.
     */
    public abstract boolean validateAndSetError();

    /**
     * Collects every input from the fragment and puts it in a bundle.
     * @return A bundle with all the input from the fragment.
     */
    public abstract Bundle getInputs();
}
