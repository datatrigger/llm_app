package com.llmserver.backend.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.DocumentReference;
import com.llmserver.backend.entity.Message;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@Service
public class ConversationService {
    
    private final Firestore firestore;
    
    public ConversationService(Firestore firestore) {
        this.firestore = firestore;
    }
    
    /**
     * Creates a new conversation and adds the first message to it.
     * @param userId The user ID
     * @param message The first message in the conversation
     * @return The conversation ID
     */
    public String createConversation(String userId, Message message) {
        try {
            DocumentReference conversation = firestore
                .collection("users")
                .document(userId)
                .collection("conversations")
                .document();
            
            conversation.collection("messages").add(message).get();
            return conversation.getId();

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create conversation", e);
        }
    }
    
    /**
     * Adds a message to an existing conversation.
     * @param userId The user ID
     * @param conversationId The conversation ID
     * @param message The message to add
     */
    public void addMessageToConversation(String userId, String conversationId, Message message) {
        try {
            firestore
                .collection("users")
                .document(userId)
                .collection("conversations")
                .document(conversationId)
                .collection("messages")
                .add(message)
                .get();

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to add message to conversation", e);
        }
    }
    
    /**
     * Retrieves all messages from a conversation, ordered by timestamp.
     * @param userId The user ID
     * @param conversationId The conversation ID
     * @return List of messages in chronological order
     */
    public List<Message> getConversationHistory(String userId, String conversationId) {
        try {
            List<QueryDocumentSnapshot> documents = firestore
                .collection("users")
                .document(userId)
                .collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get() // Firestore execution
                .get() // Future resolution
                .getDocuments();
            
            List<Message> messages = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                Message message = document.toObject(Message.class);
                messages.add(message);
            }
            
            return messages;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to retrieve conversation history", e);
        }
    }
    
    /**
     * Checks if a conversation exists.
     * @param userId The user ID
     * @param conversationId The conversation ID
     * @return true if the conversation exists, false otherwise
     */
    public boolean conversationExists(String userId, String conversationId) {
        try {
            // .exists() returns False even on existing subcollections
            // because they are not actual documents. Hence the need to check
            // for any messages in the subcollection with the given `conversationId`
            List<QueryDocumentSnapshot> messages = firestore
                .collection("users")
                .document(userId)
                .collection("conversations")
                .document(conversationId)
                .collection("messages")
                .limit(1)
                .get()
                .get()
                .getDocuments();
            
            return !messages.isEmpty();

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to check conversation existence", e);
        }
    }
}