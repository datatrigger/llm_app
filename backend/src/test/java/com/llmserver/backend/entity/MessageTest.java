package com.llmserver.backend.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import com.google.cloud.Timestamp;

class MessageTest {

    @Test
    void shouldCreateMessageWithFullConstructor() {
        // Given
        String id = "msg123";
        String text = "Hello, world!";
        String role = "user";
        Timestamp timestamp = Timestamp.now();

        // When
        Message message = new Message(id, text, role, timestamp);

        // Then
        assertEquals(id, message.id());
        assertEquals(text, message.text());
        assertEquals(role, message.role());
        assertEquals(timestamp, message.timestamp());
    }

    @Test
    void shouldCreateMessageWithConvenienceConstructor() {
        // Given
        String text = "Hello, world!";
        String role = "user";

        // When
        Message message = new Message(text, role);

        // Then
        assertNull(message.id()); // ID is set by Firestore
        assertEquals(text, message.text());
        assertEquals(role, message.role());
        assertNull(message.timestamp()); // Timestamp is set by Firestore
    }

    @Test
    void shouldCreateUserMessage() {
        // Given
        String userText = "What's the weather like?";
        String userRole = "user";

        // When
        Message userMessage = new Message(userText, userRole);

        // Then
        assertEquals(userText, userMessage.text());
        assertEquals(userRole, userMessage.role());
        assertNull(userMessage.id());
        assertNull(userMessage.timestamp());
    }

    @Test
    void shouldCreateModelMessage() {
        // Given
        String modelText = "I don't have access to real-time weather data.";
        String modelRole = "model";

        // When
        Message modelMessage = new Message(modelText, modelRole);

        // Then
        assertEquals(modelText, modelMessage.text());
        assertEquals(modelRole, modelMessage.role());
        assertNull(modelMessage.id());
        assertNull(modelMessage.timestamp());
    }

    @Test
    void shouldHandleNullValues() {
        // Given & When
        Message message = new Message(null, null, null, null);

        // Then
        assertNull(message.id());
        assertNull(message.text());
        assertNull(message.role());
        assertNull(message.timestamp());
    }

    @Test
    void shouldHandleEmptyStrings() {
        // Given
        String emptyText = "";
        String emptyRole = "";

        // When
        Message message = new Message(emptyText, emptyRole);

        // Then
        assertEquals(emptyText, message.text());
        assertEquals(emptyRole, message.role());
        assertNull(message.id());
        assertNull(message.timestamp());
    }

    @Test
    void shouldCreateMessageWithNullIdAndTimestamp() {
        // Given
        String text = "Test message";
        String role = "user";

        // When
        Message message = new Message(null, text, role, null);

        // Then
        assertNull(message.id());
        assertEquals(text, message.text());
        assertEquals(role, message.role());
        assertNull(message.timestamp());
    }

    @Test
    void shouldCreateMessageWithAllFields() {
        // Given
        String id = "msg456";
        String text = "Complete message";
        String role = "model";
        Timestamp timestamp = Timestamp.now();

        // When
        Message message = new Message(id, text, role, timestamp);

        // Then
        assertEquals(id, message.id());
        assertEquals(text, message.text());
        assertEquals(role, message.role());
        assertEquals(timestamp, message.timestamp());
    }

    @Test
    void shouldBeImmutable() {
        // Given
        Message original = new Message("Original text", "user");
        
        // When - create a new message with different text (records are immutable)
        Message modified = new Message(original.id(), "Modified text", original.role(), original.timestamp());

        // Then
        assertEquals("Original text", original.text());
        assertEquals("Modified text", modified.text());
        assertEquals(original.role(), modified.role());
    }
}