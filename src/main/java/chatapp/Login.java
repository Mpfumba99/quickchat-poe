package chatapp;

import java.util.regex.Pattern;

/**
 * Handles user registration and login for the QuickChat console application.
 *
 * Cell phone validation regex approach adapted from the general pattern
 * described in the Java Regex tutorial:
 * Oracle. (2024) The Java Tutorials - Regular Expressions.
 * Available at: https://docs.oracle.com/javase/tutorial/essential/regex/
 */
public class Login {

    // Stored account details (single-user store, matches Part 1 scope)
    private String username;
    private String password;
    private String cellPhoneNumber;
    private String firstName;
    private String lastName;
    private boolean registered = false;

    // South African-style international cell number: '+' then country code
    // and subscriber number, with the number portion after the country code
    // no more than ten digits long (matches the brief's cell phone rule and
    // the supplied test data, e.g. +27838968976).
    private static final Pattern CELL_PATTERN =
            Pattern.compile("^\\+[1-9][0-9]{0,2}[0-9]{1,10}$");

    public Login() {
    }

    /**
     * Checks that the username contains an underscore and is no more than
     * five characters long.
     */
    public boolean checkUserName(String username) {
        if (username == null) {
            return false;
        }
        return username.contains("_") && username.length() <= 5;
    }

    /**
     * Checks that the password is at least eight characters long and
     * contains a capital letter, a number, and a special character.
     */
    public boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasCapital = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasCapital = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecial = true;
            }
        }
        return hasCapital && hasDigit && hasSpecial;
    }

    /**
     * Checks that the cell phone number contains an international country
     * code followed by the number, with the number portion no more than
     * ten characters long.
     */
    public boolean checkCellPhoneNumber(String cellNumber) {
        if (cellNumber == null) {
            return false;
        }
        return CELL_PATTERN.matcher(cellNumber.trim()).matches();
    }

    /**
     * Registers a user, validating username, password, and cell phone
     * number in turn, and stores the account details when all checks pass.
     */
    public String registerUser(String username, String password, String cellNumber,
                                String firstName, String lastName) {

        if (!checkUserName(username)) {
            return "Username is not correctly formatted; please ensure that your "
                    + "username contains an underscore and is no more than five "
                    + "characters in length.";
        }

        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted; please ensure that the "
                    + "password contains at least eight characters, a capital "
                    + "letter, a number, and a special character.";
        }

        if (!checkCellPhoneNumber(cellNumber)) {
            return "Cell phone number incorrectly formatted or does not contain "
                    + "international code.";
        }

        this.username = username;
        this.password = password;
        this.cellPhoneNumber = cellNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.registered = true;

        return "Username successfully captured. Password successfully captured. "
                + "Cell phone number successfully added.";
    }

    /**
     * Verifies that the entered username and password match the details
     * captured at registration.
     */
    public boolean loginUser(String username, String password) {
        return registered
                && this.username != null
                && this.username.equals(username)
                && this.password.equals(password);
    }

    /**
     * Returns the appropriate login confirmation or error message.
     */
    public String returnLoginStatus(boolean loginSuccessful) {
        if (loginSuccessful) {
            return "Welcome " + firstName + ", " + lastName + " it is great to see you again.";
        }
        return "Username or password incorrect, please try again.";
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
