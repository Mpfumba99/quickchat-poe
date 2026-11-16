package chatapp;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * PROG5121 POE Part 3 - unit tests for the MessageStore arrays and reports.
 * Test data from the brief:
 *   1: +27834557896  "Did you get the cake?"                                        Sent
 *   2: +27838884567  "Where are you? You are late! I have asked you to be on time."  Stored
 *   3: +27834484567  "Yohoooo, I am at your gate."                                  Disregard
 *   4: 0838884567    "It is dinner time!"                                           Sent
 *   5: +27838884567  "Ok, I am leaving without you."                                Stored
 */
public class MessageStoreTest {

    private MessageStore store;
    private Message message1, message2, message3, message4, message5;

    @BeforeEach
    public void setUp() {
        Message.resetMessages();
        store = new MessageStore("Kyle Smith");

        message1 = new Message(0, "+27834557896", "Did you get the cake?");
        message2 = new Message(1, "+27838884567", "Where are you? You are late! I have asked you to be on time.");
        message3 = new Message(2, "+27834484567", "Yohoooo, I am at your gate.");
        message4 = new Message(3, "0838884567", "It is dinner time!");
        message5 = new Message(4, "+27838884567", "Ok, I am leaving without you.");

        store.addMessage(message1, MessageStore.SENT);
        store.addMessage(message2, MessageStore.STORED);
        store.addMessage(message3, MessageStore.DISREGARD);
        store.addMessage(message4, MessageStore.SENT);
        store.addMessage(message5, MessageStore.STORED);
    }

    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        assertArrayEquals(new String[] {"Did you get the cake?", "It is dinner time!"},
                store.getSentMessageTexts());
    }

    @Test
    public void testStoredAndDisregardedArraysPopulated() {
        assertEquals(2, store.getStoredMessages().length);
        assertEquals(1, store.getDisregardedMessages().length);
        assertEquals("Yohoooo, I am at your gate.", store.getDisregardedMessages()[0].getMessageText());
    }

    @Test
    public void testMessageHashAndIdArraysPopulated() {
        assertEquals(5, store.getMessageHashes().length);
        assertEquals(5, store.getMessageIDs().length);
        assertEquals(message1.getMessageHash(), store.getMessageHashes()[0]);
        assertEquals(message4.getMessageID(), store.getMessageIDs()[3]);
    }

    @Test
    public void testDisplayLongestMessage() {
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                store.getLongestMessage());
    }

    @Test
    public void testSearchForMessageID() {
        String result = store.searchByMessageID(message4.getMessageID());
        assertTrue(result.contains("0838884567"));
        assertTrue(result.contains("It is dinner time!"));
    }

    @Test
    public void testSearchForMessageIDNotFound() {
        assertEquals("Message ID not found.", store.searchByMessageID("9999999999"));
    }

    @Test
    public void testSearchByRecipient() {
        String result = store.searchByRecipient("+27838884567");
        assertTrue(result.contains("Where are you? You are late! I have asked you to be on time."));
        assertTrue(result.contains("Ok, I am leaving without you."));
        assertFalse(result.contains("Did you get the cake?"));
    }

    @Test
    public void testDeleteMessageUsingHash() {
        String result = store.deleteByMessageHash(message2.getMessageHash());
        assertEquals("Message \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.",
                result);
        assertEquals(1, store.getStoredMessages().length);
        assertEquals(4, store.getMessageHashes().length);
    }

    @Test
    public void testDeleteMessageHashNotFound() {
        assertEquals("Message hash not found.", store.deleteByMessageHash("00:0:NOTHING"));
    }

    @Test
    public void testDisplayReport() {
        String report = store.displayReport();
        assertTrue(report.contains("Message Hash: " + message1.getMessageHash()));
        assertTrue(report.contains("Recipient:    +27834557896"));
        assertTrue(report.contains("Message:      Did you get the cake?"));
        assertTrue(report.contains("Message:      It is dinner time!"));
        assertTrue(report.contains("Total sent: 2"));
    }

    @Test
    public void testDisplaySenderAndRecipient() {
        String result = store.displaySenderAndRecipient();
        assertTrue(result.contains("Sender: Kyle Smith | Recipient: +27834557896"));
        assertTrue(result.contains("Sender: Kyle Smith | Recipient: +27838884567"));
    }

    @Test
    public void testLoadStoredMessagesFromJson(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("stored.json");
        assertTrue(message2.storeMessage(file));
        assertTrue(message5.storeMessage(file));

        MessageStore loaded = new MessageStore("Kyle Smith");
        assertEquals(2, loaded.loadStoredMessages(file));
        assertEquals("Ok, I am leaving without you.", loaded.getStoredMessages()[1].getMessageText());
        assertEquals(message2.getMessageHash(), loaded.getStoredMessages()[0].getMessageHash());
    }

    @Test
    public void testSaveStoredMessagesAfterDelete(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("stored.json");
        store.deleteByMessageHash(message2.getMessageHash());
        store.saveStoredMessages(file);

        String json = Files.readString(file);
        assertFalse(json.contains("Where are you?"));
        assertTrue(json.contains("Ok, I am leaving without you."));
    }
}
