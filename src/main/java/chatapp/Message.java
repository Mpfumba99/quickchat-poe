package chatapp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * PROG5121 POE Part 2 - Sending Messages.
 * Represents a single QuickChat message and keeps track of all messages sent.
 *
 * JSON storage uses the org.json library.
 * Reference: Leary, S. 2024. JSON in Java (org.json). [Online]
 * Available at: https://github.com/stleary/JSON-java [Accessed 18 October 2026].
 */
public class Message {

    public static final int MAX_MESSAGE_LENGTH = 250;
    public static final String STORAGE_FILE = "stored_messages.json";

    // Messages that were actually sent during this session
    private static final List<Message> sentMessages = new ArrayList<>();
    private static int totalMessagesSent = 0;

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    /** Creates a message with an auto-generated random 10-digit ID. */
    public Message(int messageNumber, String recipient, String messageText) {
        this(generateMessageID(), messageNumber, recipient, messageText);
    }

    /** Creates a message with a known ID (used by the unit tests). */
    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = createMessageHash();
    }

    /** Generates a random 10-digit number as a String (may start with 0). */
    public static String generateMessageID() {
        Random random = new Random();
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            id.append(random.nextInt(10));
        }
        return id.toString();
    }

    /** Ensures the message ID is not more than ten characters. */
    public boolean checkMessageID() {
        return messageID != null && !messageID.isEmpty() && messageID.length() <= 10;
    }

    /**
     * Ensures the recipient cell number starts with the international code (+27)
     * followed by 9 digits - same rule as Part 1.
     */
    public String checkRecipientCell() {
        if (recipient != null && recipient.matches("^\\+27\\d{9}$")) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.";
    }

    /** Ensures the message is no more than 250 characters. */
    public String checkMessageLength() {
        int length = messageText == null ? 0 : messageText.length();
        if (length <= MAX_MESSAGE_LENGTH) {
            return "Message ready to send.";
        }
        int excess = length - MAX_MESSAGE_LENGTH;
        return "Message exceeds 250 characters by " + excess + ", please reduce size.";
    }

    /**
     * Message hash = first two digits of the message ID : message number :
     * first word + last word of the message, all in capitals.
     * Example: 00:0:HITONIGHT
     */
    public String createMessageHash() {
        String firstTwo = messageID.substring(0, Math.min(2, messageID.length()));
        String[] words = messageText.trim().split("\\s+");
        String firstWord = words[0].replaceAll("[^A-Za-z0-9]", "");
        String lastWord = words[words.length - 1].replaceAll("[^A-Za-z0-9]", "");
        return (firstTwo + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    /**
     * Handles the user's choice for this message.
     * 1 = Send, 2 = Disregard, 3 = Store to send later.
     */
    public String sentMessage(int option) {
        switch (option) {
            case 1:
                sentMessages.add(this);
                totalMessagesSent++;
                return "Message successfully sent.";
            case 2:
                return "Press 0 to delete the message.";
            case 3:
                storeMessage();
                return "Message successfully stored.";
            default:
                return "Invalid option, please choose 1, 2 or 3.";
        }
    }

    /** Returns the details of every message sent during this session. */
    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages sent yet.";
        }
        StringBuilder sb = new StringBuilder();
        for (Message m : sentMessages) {
            sb.append(m.getDetails()).append(System.lineSeparator());
        }
        return sb.toString().trim();
    }

    /** Returns the total number of messages sent. */
    public static int returnTotalMessages() {
        return totalMessagesSent;
    }

    /** Stores this message in a JSON file (appends to the existing array). */
    public boolean storeMessage() {
        return storeMessage(Paths.get(STORAGE_FILE));
    }

    public boolean storeMessage(Path file) {
        try {
            JSONArray stored = new JSONArray();
            if (Files.exists(file)) {
                String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8).trim();
                if (!content.isEmpty()) {
                    stored = new JSONArray(content);
                }
            }
            stored.put(toJSON());
            Files.write(file, stored.toString(4).getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            System.out.println("Could not store message: " + e.getMessage());
            return false;
        }
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("messageID", messageID);
        obj.put("messageNumber", messageNumber);
        obj.put("messageHash", messageHash);
        obj.put("recipient", recipient);
        obj.put("message", messageText);
        return obj;
    }

    /** Full details displayed after a message is sent. */
    public String getDetails() {
        return "Message ID: " + messageID + System.lineSeparator()
                + "Message Hash: " + messageHash + System.lineSeparator()
                + "Recipient: " + recipient + System.lineSeparator()
                + "Message: " + messageText;
    }

    /** Clears session data (used by unit tests). */
    static void resetMessages() {
        sentMessages.clear();
        totalMessagesSent = 0;
    }

    public String getMessageID() { return messageID; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipient() { return recipient; }
    public String getMessageText() { return messageText; }
    public String getMessageHash() { return messageHash; }
}
