package chatapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {

    private Login login;

    @BeforeEach
    public void setUp() {
        login = new Login();
    }

    // ---------- checkUserName() ----------

    @Test
    public void testUsernameCorrectlyFormatted() {
        assertTrue(login.checkUserName("kyl_1"));
    }

    @Test
    public void testUsernameIncorrectlyFormatted() {
        assertFalse(login.checkUserName("kyle!!!!!!!"));
    }

    // ---------- checkPasswordComplexity() ----------

    @Test
    public void testPasswordMeetsComplexity() {
        assertTrue(login.checkPasswordComplexity("Ch&&sec@ke99!"));
    }

    @Test
    public void testPasswordFailsComplexity() {
        assertFalse(login.checkPasswordComplexity("password"));
    }

    // ---------- checkCellPhoneNumber() ----------

    @Test
    public void testCellPhoneCorrectlyFormatted() {
        assertTrue(login.checkCellPhoneNumber("+27838968976"));
    }

    @Test
    public void testCellPhoneIncorrectlyFormatted() {
        assertFalse(login.checkCellPhoneNumber("08966553"));
    }

    // ---------- registerUser() : assertEquals on exact returned messages ----------

    @Test
    public void testRegisterUser_usernameError() {
        String result = login.registerUser("kyle!!!!!!!", "Ch&&sec@ke99!", "+27838968976",
                "Kyle", "Smith");
        assertEquals("Username is not correctly formatted; please ensure that your "
                + "username contains an underscore and is no more than five "
                + "characters in length.", result);
    }

    @Test
    public void testRegisterUser_passwordError() {
        String result = login.registerUser("kyl_1", "password", "+27838968976",
                "Kyle", "Smith");
        assertEquals("Password is not correctly formatted; please ensure that the "
                + "password contains at least eight characters, a capital "
                + "letter, a number, and a special character.", result);
    }

    @Test
    public void testRegisterUser_cellNumberError() {
        String result = login.registerUser("kyl_1", "Ch&&sec@ke99!", "08966553",
                "Kyle", "Smith");
        assertEquals("Cell phone number incorrectly formatted or does not contain "
                + "international code.", result);
    }

    @Test
    public void testRegisterUser_success() {
        String result = login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976",
                "Kyle", "Smith");
        assertEquals("Username successfully captured. Password successfully captured. "
                + "Cell phone number successfully added.", result);
    }

    // ---------- loginUser() / returnLoginStatus() ----------

    @Test
    public void testLoginSuccessful() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        boolean success = login.loginUser("kyl_1", "Ch&&sec@ke99!");
        assertTrue(success);
        assertEquals("Welcome Kyle, Smith it is great to see you again.",
                login.returnLoginStatus(success));
    }

    @Test
    public void testLoginFailed() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        boolean success = login.loginUser("kyl_1", "WrongPassword1!");
        assertFalse(success);
        assertEquals("Username or password incorrect, please try again.",
                login.returnLoginStatus(success));
    }
}
