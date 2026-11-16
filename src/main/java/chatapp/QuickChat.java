package chatapp;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * PROG5121 POE - QuickChat menu (Parts 2 and 3).
 * Runs only after the user has successfully logged in (Part 1).
 */
public class QuickChat {

    private static final Path STORAGE = Paths.get(Message.STORAGE_FILE);

    public static void run(Scanner input) {
        run(input, "Unknown user");
    }

    public static void run(Scanner input, String sender) {
        MessageStore store = new MessageStore(sender);
        int loaded = store.loadStoredMessages(STORAGE);

        System.out.println();
        System.out.println("Welcome to QuickChat.");
        if (loaded > 0) {
            System.out.println(loaded + " stored message(s) loaded from " + Message.STORAGE_FILE + ".");
        }

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("Please choose an option:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Stored Messages and Reports");
            System.out.println("4) Quit");
            System.out.print("Option: ");

            int option = readInt(input);
            switch (option) {
                case 1:
                    sendMessages(input, store);
                    break;
                case 2:
                    System.out.println(store.displaySentMessages());
                    break;
                case 3:
                    reportsMenu(input, store);
                    break;
                case 4:
                    System.out.println("Total number of messages sent: " + Message.returnTotalMessages());
                    System.out.println("Goodbye.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option, please enter 1, 2, 3 or 4.");
            }
        }
    }

    private static void reportsMenu(Scanner input, MessageStore store) {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- Stored Messages and Reports ---");
            System.out.println("1) Display sender and recipient of all sent and stored messages");
            System.out.println("2) Display the longest message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search messages by recipient");
            System.out.println("5) Delete a message using its message hash");
            System.out.println("6) Display full report");
            System.out.println("7) Back to main menu");
            System.out.print("Option: ");

            switch (readInt(input)) {
                case 1:
                    System.out.println(store.displaySenderAndRecipient());
                    break;
                case 2:
                    System.out.println("Longest message: " + store.getLongestMessage());
                    break;
                case 3:
                    System.out.print("Enter the message ID: ");
                    System.out.println(store.searchByMessageID(input.nextLine().trim()));
                    break;
                case 4:
                    System.out.print("Enter the recipient's cell number: ");
                    System.out.println(store.searchByRecipient(input.nextLine().trim()));
                    break;
                case 5:
                    System.out.print("Enter the message hash: ");
                    String hash = input.nextLine().trim().toUpperCase();
                    boolean wasStored = store.isStored(hash);
                    System.out.println(store.deleteByMessageHash(hash));
                    if (wasStored) {
                        store.saveStoredMessages(STORAGE); // keep the JSON file in step
                    }
                    break;
                case 6:
                    System.out.println(store.displayReport());
                    break;
                case 7:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option, please enter 1 to 7.");
            }
        }
    }

    private static void sendMessages(Scanner input, MessageStore store) {
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
            int choice;
            do {
                System.out.println("Choose: 1) Send Message  2) Disregard Message  3) Store Message to send later");
                System.out.print("Option: ");
                choice = readInt(input);
                System.out.println(message.sentMessage(choice));
            } while (choice < 1 || choice > 3);

            if (choice == 1) {
                store.addMessage(message, MessageStore.SENT);
                System.out.println();
                System.out.println(message.getDetails());
            } else if (choice == 2) {
                System.out.print("Enter 0 to delete: ");
                readInt(input);
                store.addMessage(message, MessageStore.DISREGARD);
                System.out.println("Message deleted.");
            } else {
                store.addMessage(message, MessageStore.STORED);
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
