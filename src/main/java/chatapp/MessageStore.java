package chatapp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * PROG5121 POE Part 3 - stores messages in arrays and produces reports.
 *
 * Arrays used:
 *   sentMessages, disregardedMessages, storedMessages (Message objects)
 *   messageHashes, messageIDs (one entry for every message captured)
 *
 * Stored messages are read from the JSON file into the storedMessages array
 * using the org.json library.
 * Reference: Leary, S. 2024. JSON in Java (org.json). [Online]
 * Available at: https://github.com/stleary/JSON-java [Accessed 16 November 2026].
 */
public class MessageStore {

    public static final String SENT = "Sent";
    public static final String STORED = "Stored";
    public static final String DISREGARD = "Disregard";

    private static final int START_SIZE = 10;

    private final String sender;

    private Message[] sentMessages = new Message[START_SIZE];
    private Message[] disregardedMessages = new Message[START_SIZE];
    private Message[] storedMessages = new Message[START_SIZE];
    private String[] messageHashes = new String[START_SIZE];
    private String[] messageIDs = new String[START_SIZE];

    private int sentCount = 0;
    private int disregardedCount = 0;
    private int storedCount = 0;
    private int totalCount = 0;

    public MessageStore(String sender) {
        this.sender = sender;
    }

    // ------------------------------------------------------------------
    // Populating the arrays
    // ------------------------------------------------------------------

    /** Adds a message to the correct array based on its flag (Sent, Stored or Disregard). */
    public void addMessage(Message message, String flag) {
        if (SENT.equalsIgnoreCase(flag)) {
            sentMessages = ensureSpace(sentMessages, sentCount);
            sentMessages[sentCount++] = message;
        } else if (STORED.equalsIgnoreCase(flag)) {
            storedMessages = ensureSpace(storedMessages, storedCount);
            storedMessages[storedCount++] = message;
        } else if (DISREGARD.equalsIgnoreCase(flag)) {
            disregardedMessages = ensureSpace(disregardedMessages, disregardedCount);
            disregardedMessages[disregardedCount++] = message;
        } else {
            throw new IllegalArgumentException("Unknown flag: " + flag);
        }

        if (totalCount == messageHashes.length) {
            messageHashes = Arrays.copyOf(messageHashes, messageHashes.length * 2);
            messageIDs = Arrays.copyOf(messageIDs, messageIDs.length * 2);
        }
        messageHashes[totalCount] = message.getMessageHash();
        messageIDs[totalCount] = message.getMessageID();
        totalCount++;
    }

    /** Reads the stored messages JSON file into the storedMessages array. Returns how many were loaded. */
    public int loadStoredMessages(Path file) {
        if (!Files.exists(file)) {
            return 0;
        }
        try {
            String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                return 0;
            }
            JSONArray array = new JSONArray(content);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Message m = new Message(obj.getString("messageID"), obj.getInt("messageNumber"),
                        obj.getString("recipient"), obj.getString("message"));
                addMessage(m, STORED);
            }
            return array.length();
        } catch (IOException e) {
            System.out.println("Could not read stored messages: " + e.getMessage());
            return 0;
        }
    }

    /** Writes the storedMessages array back to the JSON file (used after a delete). */
    public void saveStoredMessages(Path file) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < storedCount; i++) {
            array.put(storedMessages[i].toJSON());
        }
        try {
            Files.write(file, array.toString(4).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Could not save stored messages: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Part 3 features
    // ------------------------------------------------------------------

    /** a. Displays the sender and recipient of all sent and stored messages. */
    public String displaySenderAndRecipient() {
        if (sentCount + storedCount == 0) {
            return "No sent or stored messages.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentCount; i++) {
            sb.append("Sent   | Sender: ").append(sender)
              .append(" | Recipient: ").append(sentMessages[i].getRecipient()).append(System.lineSeparator());
        }
        for (int i = 0; i < storedCount; i++) {
            sb.append("Stored | Sender: ").append(sender)
              .append(" | Recipient: ").append(storedMessages[i].getRecipient()).append(System.lineSeparator());
        }
        return sb.toString().trim();
    }

    /** b. Returns the longest message across sent, stored and disregarded messages. */
    public String getLongestMessage() {
        String longest = "";
        for (Message m : allMessages()) {
            if (m.getMessageText().length() > longest.length()) {
                longest = m.getMessageText();
            }
        }
        return longest.isEmpty() ? "No messages captured." : longest;
    }

    /** c. Searches for a message ID and returns its recipient and message. */
    public String searchByMessageID(String messageID) {
        for (Message m : allMessages()) {
            if (m.getMessageID().equals(messageID)) {
                return "Recipient: " + m.getRecipient() + System.lineSeparator()
                        + "Message: " + m.getMessageText();
            }
        }
        return "Message ID not found.";
    }

    /** d. Returns every sent or stored message for a particular recipient. */
    public String searchByRecipient(String recipient) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentCount; i++) {
            if (sentMessages[i].getRecipient().equals(recipient)) {
                sb.append(sentMessages[i].getMessageText()).append(System.lineSeparator());
            }
        }
        for (int i = 0; i < storedCount; i++) {
            if (storedMessages[i].getRecipient().equals(recipient)) {
                sb.append(storedMessages[i].getMessageText()).append(System.lineSeparator());
            }
        }
        return sb.length() == 0 ? "No messages found for " + recipient + "." : sb.toString().trim();
    }

    /** e. Deletes a message using its message hash. */
    public String deleteByMessageHash(String hash) {
        Message removed = removeFrom(sentMessages, sentCount, hash);
        if (removed != null) {
            sentCount--;
        } else if ((removed = removeFrom(storedMessages, storedCount, hash)) != null) {
            storedCount--;
        } else if ((removed = removeFrom(disregardedMessages, disregardedCount, hash)) != null) {
            disregardedCount--;
        } else {
            return "Message hash not found.";
        }

        // Remove the matching entries from the hash and ID arrays
        for (int i = 0; i < totalCount; i++) {
            if (messageHashes[i].equals(hash)) {
                for (int j = i; j < totalCount - 1; j++) {
                    messageHashes[j] = messageHashes[j + 1];
                    messageIDs[j] = messageIDs[j + 1];
                }
                totalCount--;
                messageHashes[totalCount] = null;
                messageIDs[totalCount] = null;
                break;
            }
        }
        return "Message \"" + removed.getMessageText() + "\" successfully deleted.";
    }

    /** f. Displays a report of the full details of all sent and stored messages. */
    public String displayReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== QUICKCHAT MESSAGE REPORT ==========").append(System.lineSeparator());
        appendSection(sb, "SENT MESSAGES", sentMessages, sentCount);
        appendSection(sb, "STORED MESSAGES", storedMessages, storedCount);
        sb.append("Total sent: ").append(sentCount)
          .append(" | Total stored: ").append(storedCount)
          .append(" | Total disregarded: ").append(disregardedCount);
        return sb.toString();
    }

    /** Lists the details of all sent messages (menu option "Show recently sent messages"). */
    public String displaySentMessages() {
        if (sentCount == 0) {
            return "No messages sent yet.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentCount; i++) {
            sb.append(sentMessages[i].getDetails()).append(System.lineSeparator())
              .append("----------------------------------------").append(System.lineSeparator());
        }
        return sb.toString().trim();
    }

    // ------------------------------------------------------------------
    // Getters (return copies trimmed to the number of items)
    // ------------------------------------------------------------------

    public Message[] getSentMessages() { return Arrays.copyOf(sentMessages, sentCount); }
    public Message[] getStoredMessages() { return Arrays.copyOf(storedMessages, storedCount); }
    public Message[] getDisregardedMessages() { return Arrays.copyOf(disregardedMessages, disregardedCount); }
    public String[] getMessageHashes() { return Arrays.copyOf(messageHashes, totalCount); }
    public String[] getMessageIDs() { return Arrays.copyOf(messageIDs, totalCount); }

    /** Returns just the text of each sent message. */
    public String[] getSentMessageTexts() {
        String[] texts = new String[sentCount];
        for (int i = 0; i < sentCount; i++) {
            texts[i] = sentMessages[i].getMessageText();
        }
        return texts;
    }

    public boolean isStored(String hash) {
        for (int i = 0; i < storedCount; i++) {
            if (storedMessages[i].getMessageHash().equals(hash)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Message[] allMessages() {
        Message[] all = new Message[sentCount + storedCount + disregardedCount];
        System.arraycopy(sentMessages, 0, all, 0, sentCount);
        System.arraycopy(storedMessages, 0, all, sentCount, storedCount);
        System.arraycopy(disregardedMessages, 0, all, sentCount + storedCount, disregardedCount);
        return all;
    }

    private static Message[] ensureSpace(Message[] array, int count) {
        return count == array.length ? Arrays.copyOf(array, array.length * 2) : array;
    }

    /** Removes the message with the given hash by shifting the rest left. Returns it, or null. */
    private static Message removeFrom(Message[] array, int count, String hash) {
        for (int i = 0; i < count; i++) {
            if (array[i].getMessageHash().equals(hash)) {
                Message removed = array[i];
                for (int j = i; j < count - 1; j++) {
                    array[j] = array[j + 1];
                }
                array[count - 1] = null;
                return removed;
            }
        }
        return null;
    }

    private static void appendSection(StringBuilder sb, String title, Message[] array, int count) {
        sb.append(title).append(System.lineSeparator());
        if (count == 0) {
            sb.append("  (none)").append(System.lineSeparator());
        }
        for (int i = 0; i < count; i++) {
            sb.append("  Message Hash: ").append(array[i].getMessageHash()).append(System.lineSeparator())
              .append("  Recipient:    ").append(array[i].getRecipient()).append(System.lineSeparator())
              .append("  Message:      ").append(array[i].getMessageText()).append(System.lineSeparator())
              .append("  ----------------------------------------").append(System.lineSeparator());
        }
    }
}
