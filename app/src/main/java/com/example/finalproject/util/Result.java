package com.example.finalproject.util;


import androidx.annotation.Nullable;

/**
 * The `Result` class represents the outcome of an operation that could fail. It provides a way to
 * handle success and failure scenarios explicitly.
 */
public class Result<T, E> {
    // The value of the variable if the operation was successful:
    private final T value;

    // The error in case the operation failed:
    private final E error;

    // Whether or not the result is a success:
    private final boolean isOk;

    private Result(T value, E error, boolean isOk) {
        // Setting the isOk attribute:
        this.isOk = isOk;

        // Setting the value or the error:
        if (isOk) {
            this.value = value;
            this.error = null;
        }
        else {
            this.value = null;
            this.error = error;
        }
    }

    /**
     * Creates a Result object of a successful operation.
     * @param value The successful value that will be wrapped with the Result class.
     * @return A successful Result object containing the given value.
     * @param <T> The type of the successful value.
     * @param <E> The type of the error in case of failure.
     */
    public static <T, E> Result<T, E> success(T value) {
        return new Result<>(value, null, true);
    }

    /**
     * Creates a Result object of a failed operation.
     * @param error A description of the error that occurred.
     * @return A successful Result object containing the given value.
     * @param <T> The type of the successful value.
     * @param <E> The type of the error in case of failure.
     */
    public static <T, E> Result<T, E> failure(E error) {
        return new Result<>(null, error, false);
    }

    /**
     * Creates a Result object based on a nullable value. If the value is not null, the function
     * will return a success result. If it is null, the function will return a failure result with
     * the given error value.
     * @param value A nullable value. If not null, it will be wrapped inside a success result.
     * @param error In case parameter 'value' is null, this parameter will be wrapped in a failure
     *              result.
     * @return A success result with parameter 'value' in it if value is not null, a failure result
     *         with parameter 'error' in it if it is.
     */
    public static <T, E> Result<T, E> ofNullable(@Nullable T value, E error) {
        // If it's null, return an error:
        if (value == null)
            return new Result<>(null, error, false);
        // If not, return a success:
        else
            return new Result<>(value, null, true);
    }

    public T getValue() {
        return value;
    }

    public E getError() {
        return error;
    }

    /**
     * Checks if the current Result object contains some value (and not an error).
     * @return True if the Result object contains some value, False if the Result object has an
     *         error description.
     */
    public boolean isOk() {
        return this.isOk;
    }

    /**
     * Checks if the current Result object contains an error.
     * @return True if the current Result object contains an error and not a value, False otherwise.
     */
    public boolean isErr() {
        return !this.isOk;
    }

}
