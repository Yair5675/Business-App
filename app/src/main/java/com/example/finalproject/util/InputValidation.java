package com.example.finalproject.util;

/**
 * A class responsible for the validation of any information given by the user.
 */
public class InputValidation {
    // The minimum length of a user's name:
    private static final byte MIN_NAME_LENGTH = 2;
    // The maximum length of a user's name:
    private static final byte MAX_NAME_LENGTH = 10;

    // The minimum length of a user's surname:
    private static final byte MIN_SURNAME_LENGTH = 2;
    // The maximum length of a user's surname:
    private static final byte MAX_SURNAME_LENGTH = 10;

    // The minimum length of a user's password:
    private static final byte MIN_PASSWORD_LENGTH = 8;
    // The maximum length of a user's password:
    private static final byte MAX_PASSWORD_LENGTH = 22;

    /**
     * Checks if a given name is valid for a user according to various rules.
     * @param name A potential name of a user, will be checked to see if it's a suitable name.
     * @return If the name is ok, the function returns an OK result with nothing in it. If the name
     *         is not ok, the function returns an ERR result with the reason the name is not ok
     *         inside the Result object;
     */
    public static Result<Void, String> validateFirstName(String name) {
        // Checking if the name is empty:
        if (name.isEmpty())
            return Result.failure(Constants.MANDATORY_INPUT_ERROR);
        // Checking the length of the name:
        if (name.length() < MIN_NAME_LENGTH)
            return Result.failure("Name too short");
        else if (name.length() > MAX_NAME_LENGTH)
            return Result.failure("Name too long");

        // Checking that every character is in English:
        else if (isInEnglish(name))
            return Result.success(null);
        else
            return Result.failure("Must be in English");
    }

    /**
     * Checks if a given surname is valid for a user according to various rules.
     * @param lastName A potential surname of a user, will be checked to see if it's a suitable
     *                surname.
     * @return If the surname is ok, the function returns an OK result with nothing in it. If the
     *         surname is not ok, the function returns an ERR result with the reason the surname is
     *         not ok inside the Result object.
     */
    public static Result<Void, String> validateLastName(String lastName) {
        // Checking if the surname is empty:
        if (lastName.isEmpty())
            return Result.failure(Constants.MANDATORY_INPUT_ERROR);
        // Checking the length of the surname:
        else if (lastName.length() < MIN_SURNAME_LENGTH)
            return Result.failure("Surname too short");
        else if (lastName.length() > MAX_SURNAME_LENGTH)
            return Result.failure("Surname too long");

        // Checking that every character is in English:
        if (isInEnglish(lastName))
            return Result.success(null);
        else
            return Result.failure("Must be in English");
    }

    public static Result<Void, String> validateEmail(String email) {
        // Checking if the email is empty:
        if (email.isEmpty())
            return Result.failure(Constants.MANDATORY_INPUT_ERROR);

        // Checking that the email ends with '@gmail.com':
        else if(!email.endsWith("@gmail.com"))
            return Result.failure("Must end with '@gmail.com'");

        // Checking there are only English characters and numbers in the email:
        final String uniquePart = email.substring(0, email.lastIndexOf('@'));

        // Checking the part before the '@' is not empty:
        if (uniquePart.isEmpty())
            return Result.failure("Invalid email");

        for (int i = 0; i < uniquePart.length(); i++) {
            final char current = uniquePart.charAt(i);

            // If it's not an english char nor number:
            if (('A' > current || current > 'Z') && ('a' > current || current > 'z') && ('0' > current || current > '9'))
                return Result.failure("Must contain only English characters and numbers");
        }

        return Result.success(null);
    }

    public static Result<Void, String> validatePassword(String password) {
        // Checking if the password is empty:
        if (password.isEmpty())
            return Result.failure(Constants.MANDATORY_INPUT_ERROR);

        // Checking the length of the password:
        else if (password.length() < MIN_PASSWORD_LENGTH)
            return Result.failure("Password too short");
        else if (password.length() > MAX_PASSWORD_LENGTH)
            return Result.failure("Password too long");

        // Checking that there is at least one capital letter, one lowercase letter, one number and
        // one special character:
        boolean containsCapital = false, containsLowerCase = false, containsNumber = false,
                containsSymbol = false;
        final String allowedSymbols = "!?@#$%^&*()|\\~`/.,'\";-_";

        for (int i = 0; i < password.length(); i++) {
            final char current = password.charAt(i);

            // Checking the current letter:
            final boolean isCapital = 'Z' >= current && current >= 'A';
            final boolean isLowerCase = 'z' >= current && current >= 'a';
            final boolean isDigit = '9' >= current && current >= '0';
            final boolean isSymbol = allowedSymbols.contains(Character.toString(current));

            // If none of those are true, it's an illegal character:
            if (!(isCapital || isLowerCase || isDigit || isSymbol))
                return Result.failure(String.format("\"%c\" is not a valid character", current));

            // Update the flags:
            containsCapital |= isCapital;
            containsLowerCase |= isLowerCase;
            containsNumber |= isDigit;
            containsSymbol |= isSymbol;
        }

        if (!containsCapital)
            return Result.failure("Must contain at least one capital letter");
        else if (!containsLowerCase)
            return Result.failure("Must contain at least one lowercase letter");
        else if (!containsNumber)
            return Result.failure("Must contain at least one number");
        else if (!containsSymbol)
            return Result.failure(
                    String.format(
                            "Must contain at least one of the following characters: %s",
                            allowedSymbols
                    )
            );
        else
            return Result.success(null);
    }

    public static Result<Void, String> validatePhone(String phoneNumber) {
        // Checking if the phone number is empty:
        if (phoneNumber.isEmpty())
            return Result.failure(Constants.MANDATORY_INPUT_ERROR);

        // Phone number must be 10 characters long and start with "05". Additionally the phone
        // number cannot start with "056" or "057":
        else if (!phoneNumber.startsWith("05"))
            return Result.failure("Phone must start with \"05\"");
        else if (phoneNumber.startsWith("056") || phoneNumber.startsWith("057"))
            return Result.failure(String.format("Invalid area code: 05%c", phoneNumber.charAt(2)));
        // Check that the phone number is all digits:
        else if (!isANumber(phoneNumber))
            return Result.failure("Invalid phone number");
        else if (phoneNumber.length() < 10)
            return Result.failure("Phone number too short");
        else if (phoneNumber.length() > 10)
            return Result.failure("Phone number too long");
        else
            return Result.success(null);
    }

    public static Result<Void, String> validateAddress(String address) {
        // Check that it isn't empty:
        if (address.isEmpty())
            return Result.failure(Constants.MANDATORY_INPUT_ERROR);

        // Save a uniform error:
        final String INVALID_STREET_NAME_MSG = "Invalid street name";

        // Split the address into words:
        final String[] words = address.split(" ");

        // Make sure the first word is at least 2 characters long:
        if (words[0].length() < 2)
            return Result.failure(INVALID_STREET_NAME_MSG);

        // Check that every word but the last is in english:
        for (int i = 0; i < words.length - 1; i++) {
            if (!isInEnglish(words[i]))
                return Result.failure(INVALID_STREET_NAME_MSG);
        }

        // Check that the last word is a number:
        if (isANumber(words[words.length - 1]))
            return Result.success(null);

        // If it isn't, check if it's a mix of letters and digits (in that case it's an invalid
        // street name):
        else if (words[words.length - 1].matches(".*\\d.*"))
            return Result.failure(INVALID_STREET_NAME_MSG);

        // If it doesn't contain any digits but does have letters, we are missing a house number:
        else if (isInEnglish(words[words.length - 1]))
            return Result.failure("Missing house number");

        // If it's gibberish, it's once again invalid name:
        else
            return Result.failure(INVALID_STREET_NAME_MSG);
    }

    private static boolean isANumber(String input) {
        return input.matches("^[0-9]+$");
    }

    private static boolean isInEnglish(String word) {
        return word.matches("^[a-zA-Z]+$");
    }
}
