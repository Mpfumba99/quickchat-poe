package chatapp;

import java.util.Scanner;

/**
 * Console entry point for the QuickChat application.
 *
 * Part 1: registers a new user and logs them in (Login class).
 * Parts 2 and 3: after a successful login, opens the QuickChat menu,
 * passing the user's full name so it can be shown as the message sender.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Login login = new Login();

        System.out.println("=== QuickChat Registration ===");

        String username;
        String password;
        String cellNumber;
        String result;

        do {
            System.out.print("Enter a username (must contain '_' and be <= 5 characters): ");
            username = scanner.nextLine();

            System.out.print("Enter a password (8+ chars, capital, number, special char): ");
            password = scanner.nextLine();

            System.out.print("Enter your SA cell phone number (e.g. +27838968976): ");
            cellNumber = scanner.nextLine();

            System.out.print("Enter your first name: ");
            String firstName = scanner.nextLine();

            System.out.print("Enter your last name: ");
            String lastName = scanner.nextLine();

            result = login.registerUser(username, password, cellNumber, firstName, lastName);
            System.out.println(result);

        } while (!result.startsWith("Username successfully captured"));

        System.out.println("\n=== QuickChat Login ===");
        System.out.print("Enter your username: ");
        String loginUsername = scanner.nextLine();
        System.out.print("Enter your password: ");
        String loginPassword = scanner.nextLine();

        boolean success = login.loginUser(loginUsername, loginPassword);
        System.out.println(login.returnLoginStatus(success));
        if (success) {
            QuickChat.run(scanner, login.getFirstName() + " " + login.getLastName());
        }

        scanner.close();
    }
}
