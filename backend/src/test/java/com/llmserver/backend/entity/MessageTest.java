package com.llmserver.backend.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.google.cloud.Timestamp;

class MessageTest {

    @Test
    void shouldCreateMessageWithDefaultConstructor() {
        // When
        Message message = new Message();

        // Then
        assertNull(message.getId());
        assertNull(message.getText());
        assertNull(message.getRole());
        assertNull(message.getTimestamp());
    }

    @Test
    void shouldCreateMessageWithTextAndRole() {
        // Given
        String text = "Hello, world!";
        String role = "user";

        // When
        Message message = new Message(text, role);

        // Then
        assertNull(message.getId()); // ID is set by Firestore
        assertEquals(text, message.getText());
        assertEquals(role, message.getRole());
        assertNull(message.getTimestamp()); // Timestamp is set by Firestore
    }

    @Test
    void shouldSetAndGetId() {
        // Given
        Message message = new Message();
        String id = "msg123";

        // When
        message.setId(id);

        // Then
        assertEquals(id, message.getId());
    }

    @Test
    void shouldSetAndGetText() {
        // Given
        Message message = new Message();
        String text = "Test message";

        // When
        message.setText(text);

        // Then
        assertEquals(text, message.getText());
    }

    @Test
    void shouldSetAndGetRole() {
        // Given
        Message message = new Message();
        String role = "model";

        // When
        message.setRole(role);

        // Then
        assertEquals(role, message.getRole());
    }

    @Test
    void shouldSetAndGetTimestamp() {
        // Given
        Message message = new Message();
        Timestamp timestamp = Timestamp.now();

        // When
        message.setTimestamp(timestamp);

        // Then
        assertEquals(timestamp, message.getTimestamp());
    }

    @Test
    void shouldCreateUserMessage() {
        // Given
        String userText = "What's the weather like?";
        String userRole = "user";

        // When
        Message userMessage = new Message(userText, userRole);

        // Then
        assertEquals(userText, userMessage.getText());
        assertEquals(userRole, userMessage.getRole());
    }

    @Test
    void shouldCreateModelMessage() {
        // Given
        String modelText = "I don't have access to real-time weather data.";
        String modelRole = "model";

        // When
        Message modelMessage = new Message(modelText, modelRole);

        // Then
        assertEquals(modelText, modelMessage.getText());
        assertEquals(modelRole, modelMessage.getRole());
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        Message message = new Message();

        // When
        message.setText(null);
        message.setRole(null);
        message.setTimestamp(null);

        // Then
        assertNull(message.getText());
        assertNull(message.getRole());
        assertNull(message.getTimestamp());
    }

    @Test
    void shouldHandleEmptyStrings() {
        // Given
        String emptyText = "";
        String emptyRole = "";

        // When
        Message message = new Message(emptyText, emptyRole);

        // Then
        assertEquals(emptyText, message.getText());
        assertEquals(emptyRole, message.getRole());
    }
}