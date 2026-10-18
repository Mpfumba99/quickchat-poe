package chatapp;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * PROG5121 POE Part 2 - unit tests for the Message class.
 * Test data from the brief:
 *   Message 1: +27718693002, "Hi Mike, can you join us for dinner tonight" - Send
 *   Message 2: 08575975889,  "Hi Keegan, did you receive the payment?"     - Disregard
 */
public class MessageTest {

    private Message message1;
    private Message message2;

    @BeforeEach
    public void setUp() {
        Message.resetMessages();
        message1 = new Message("0012345678", 0, "+27718693002",
                "Hi Mike, can you join us for dinner tonight");
        message2 = new Message("1234567890", 1, "08575975889",
                "Hi Keegan, did you receive the payment?");
    }

    // ---- Message length ----
    @Test
    public void testMessageLengthSuccess() {
        assertEquals("Message ready to send.", message1.checkMessageLength());
    }

    @Test
    public void testMessageLengthFailure() {
        String longText = "a".repeat(260);
        Message longMessage = new Message("0012345678", 0, "+27718693002", longText);
        assertEquals("Message exceeds 250 characters by 10, please reduce size.",
                longMessage.checkMessageLength());
    }

    // ---- Recipient number ----
    @Test
    public void testRecipientCellSuccess() {
        assertEquals("Cell phone number successfully captured.", message1.checkRecipientCell());
    }

    @Test
    public void testRecipientCellFailure() {
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.", message2.checkRecipientCell());
    }

    // ---- Message hash ----
    @Test
    public void testMessageHash() {
        assertEquals("00:0:HITONIGHT", message1.createMessageHash());
    }

    @Test
    public void testMessageHashSecondMessage() {
        assertEquals("12:1:HIPAYMENT", message2.createMessageHash());
    }

    // ---- Message ID ----
    @Test
    public void testMessageIDCreated() {
        Message generated = new Message(0, "+27718693002", "Hi there");
        assertTrue(generated.checkMessageID());
        assertEquals(10, generated.getMessageID().length());
        assertTrue(generated.getMessageID().matches("\\d{10}"));
    }

    @Test
    public void testMessageIDTooLong() {
        Message bad = new Message("12345678901", 0, "+27718693002", "Hi there");
        assertFalse(bad.checkMessageID());
    }

    // ---- Send / Disregard / Store ----
    @Test
    public void testSendMessage() {
        assertEquals("Message successfully sent.", message1.sentMessage(1));
    }

    @Test
    public void testDisregardMessage() {
        assertEquals("Press 0 to delete the message.", message2.sentMessage(2));
    }

    @Test
    public void testStoreMessageWritesJson(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("stored.json");
        assertTrue(message1.storeMessage(file));
        assertTrue(message2.storeMessage(file));

        JSONArray stored = new JSONArray(Files.readString(file));
        assertEquals(2, stored.length());
        assertEquals("+27718693002", stored.getJSONObject(0).getString("recipient"));
        assertEquals("00:0:HITONIGHT", stored.getJSONObject(0).getString("messageHash"));
    }

    // ---- Totals and printing ----
    @Test
    public void testReturnTotalMessages() {
        message1.sentMessage(1); // sent
        message2.sentMessage(2); // disregarded
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void testPrintMessages() {
        message1.sentMessage(1);
        String output = Message.printMessages();
        assertTrue(output.contains("Message ID: 0012345678"));
        assertTrue(output.contains("Message Hash: 00:0:HITONIGHT"));
        assertTrue(output.contains("Recipient: +27718693002"));
        assertTrue(output.contains("Message: Hi Mike, can you join us for dinner tonight"));
    }
}
