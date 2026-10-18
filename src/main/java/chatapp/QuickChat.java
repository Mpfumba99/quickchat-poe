package chatapp;

import java.util.Scanner;

/**
 * PROG5121 POE Part 2 - QuickChat menu.
 * Runs only after the user has successfully logged in (Part 1).
 */
public class QuickChat {

    public static void run(Scanner input) {
        System.out.println();
        System.out.println("Welcome to QuickChat.");

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("Please choose an option:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.print("Option: ");

            int option = readInt(input);
            switch (option) {
                case 1:
                    sendMessages(input);
                    break;
                case 2:
                    System.out.println("Coming Soon.");
                    break;
                case 3:
                    System.out.println("Total number of messages sent: " + Message.returnTotalMessages());
                    System.out.println("Goodbye.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option, please enter 1, 2 or 3.");
            }
        }
    }

    private static void sendMessages(Scanner input) {
        System.out.print("How many messages would you like to send? ");
        int count = readInt(input);
        while (count < 1) {
            System.out.print("Please enter a number greater than 0: ");
            count = readInt(input);
        }

        for (int i = 0; i < count; i++) {
            System.out.println();
            System.out.println("--- Message " + (i + 1) + " of " + count + " ---");

            // Recipient
            String recipient;
            while (true) {
                System.out.print("Enter recipient cell number (e.g. +27718693002): ");
                recipient = input.nextLine().trim();
                String result = new Message("0000000000", i, recipient, "x").checkRecipientCell();
                System.out.println(result);
                if (result.startsWith("Cell phone number successfully")) {
                    break;
                }
            }

            // Message text
            String text;
            while (true) {
                System.out.print("Enter your message (max 250 characters): ");
                text = input.nextLine();
                if (text.trim().isEmpty()) {
                    System.out.println("Message cannot be empty.");
                    continue;
                }
                String result = new Message("0000000000", i, recipient, text).checkMessageLength();
                System.out.println(result);
                if (result.equals("Message ready to send.")) {
                    break;
                }
            }

            Message message = new Message(i, recipient, text);
            if (!message.checkMessageID()) {
                System.out.println("Message ID is invalid.");
                continue;
            }
            System.out.println("Message ID generated: " + message.getMessageID());
            System.out.println("Message Hash: " + message.getMessageHash());

            // Send / Disregard / Store
            String outcome;
            int choice;
            do {
                System.out.println("Choose: 1) Send Message  2) Disregard Message  3) Store Message to send later");
                System.out.print("Option: ");
                choice = readInt(input);
                outcome = message.sentMessage(choice);
                System.out.println(outcome);
            } while (choice < 1 || choice > 3);

            if (choice == 2) {
                System.out.print("Enter 0 to delete: ");
                readInt(input);
                System.out.println("Message deleted.");
            }

            if (choice == 1) {
                System.out.println();
                System.out.println(message.getDetails());
            }
        }

        System.out.println();
        System.out.println("Total number of messages sent: " + Message.returnTotalMessages());
    }

    private static int readInt(Scanner input) {
        while (true) {
            String line = input.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a number: ");
            }
        }
    }
}
