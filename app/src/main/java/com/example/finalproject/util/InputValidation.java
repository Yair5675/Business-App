package com.example.finalproject.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // The minimum length of a role name:
    private static final byte MIN_ROLE_LENGTH = 2;
    // The maximum length of a role name:
    private static final byte MAX_ROLE_LENGTH = 15;

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

        // Split to words:
        final String[] words = lastName.split(" ");

        // Check the length:
        if (words.length > 2)
            return Result.failure("Only 2 words are allowed");

        // Validate each word:
        for (int i = 0; i < words.length; i++) {
            final String word = words[i];
            // Check the length of the word:
            if (word.length() < MIN_SURNAME_LENGTH) {
                if (word.length() == 1 && Character.isAlphabetic(word.charAt(0)))
                    return Result.failure("An initial ends with a dot");
                return Result.failure(String.format("%s word is too short", i == 0 ? "First" : "Second"));
            }

            // Check that the word is in english:
            if (!isInEnglish(word)) {
                // Check if that's an initial:
                if (Character.isAlphabetic(word.charAt(0))) {
                    if (i != 0 || words.length == 1)
                        return Result.failure("Initial must be before a surname");
                }
                else
                    return Result.failure("Must be in English");
            }
        }

        return Result.success(null);
    }

    public static Result<Void, String> validateEmail(String email) {
        if (email == null || email.isEmpty())
            return Result.failure(Constants.MANDATORY_INPUT_ERROR);

        // Try with custom email regex:
        final String EMAIL_REGEX = "^[a-zA-Z0-9+&-]+(?:\\.[a-zA-Z0-9+&-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);

        // If the pattern matches it's an email:
        return matcher.matches() ? Result.success(null) : Result.failure("Invalid email");
    }

    public static Result<Void, String> validateRoleName(String roleName) {
        // Check if the role name is empty:
        if (roleName.isEmpty())
            return Result.failure(Constants.MANDATORY_INPUT_ERROR);

        // Check the length:
        if (roleName.length() < MIN_ROLE_LENGTH)
            return Result.failure("Role name too short");
        else if (roleName.length() > MAX_ROLE_LENGTH)
            return Result.failure("Role name too long");

        // Only allow two words max:
        final String[] words = roleName.split(" ");
        if (words.length > 2)
            return Result.failure("Only two words are allowed");

        // Check that all words are in english:
        for (String word : words) {
            if (word.isEmpty())
                return Result.failure("Consecutive spaces are not allowed");
            else if (!isInEnglish(word))
                return Result.failure("Must be in english");
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

    private static boolean isInEnglish(String word) {
        return word.matches("^[a-zA-Z]+$");
    }

    public static Result<Void, String> validateCompanyName(String companyName) {
        // https://learn.microsoft.com/en-us/partner-center/validate-names-addresses
        if (companyName.isEmpty())
            return Result.failure(Constants.MANDATORY_INPUT_ERROR);
        else if (companyName.length() < 2)
            return Result.failure("Company name too short");
        else if (companyName.length() > 30)
            return Result.failure("Company name too long");
        else if(companyName.contains("  "))
            return Result.failure("Double spaces are not allowed");
        else if (companyName.charAt(0) == ' ' || companyName.charAt(companyName.length() - 1) == ' ')
            return Result.failure("Can't end or start with a space");

        final String[] words = companyName.split(" ");
        for (String word : words) {
            final Result<Void, String> wordResult = validateCompanyWord(word);
            if (wordResult.isErr())
                return wordResult;
        }

        return Result.success(null);
    }

    private static Result<Void, String> validateCompanyWord(String word) {
        // Don't allow a single char:
        if (word.length() == 1)
            return Result.failure("Each word must have more than one character");

        // Check allowed characters:
        final String allowedChars = "~-=#.%+-:^[$&]@*()/|`<!:(>?){،{}'⟨;⟩'\"+";
        for (char c : word.toCharArray()) {
            if (!allowedChars.contains(Character.toString(c)) && !isInEnglish(String.valueOf(c)) && !Character.isDigit(c))
                return Result.failure(String.format("Character %c is not allowed", c));
        }

        return Result.success(null);
    }

}
