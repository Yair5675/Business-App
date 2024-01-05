package com.example.finalproject.util;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * A class designed to be an extension of TextWatcher, that also saves the input field whose text
 * was changed.
 */
public interface ImprovedTextWatcher extends TextWatcher {

    @Override
    default void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    default void afterTextChanged(Editable editable) {}
}
