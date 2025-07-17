package com.llmserver.backend.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.DocumentReference;
import com.llmserver.backend.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@Service
public class ConversationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConversationService.class);
    private final Firestore firestore;
    
    public ConversationService(Firestore firestore) {
        this.firestore = firestore;
        logger.info("ConversationService initialized");
    }
    
    public String createConversation(String userId, Message message) {
        logger.info("Creating new conversation", "userId", userId);
        
        try {
            DocumentReference conversation = firestore
                .collection("users")
                .document(userId)
                .collection("conversations")
                .document();
            
            conversation.collection("messages").add(message).get();
            String conversationId = conversation.getId();
            
            logger.info("Successfully created conversation", 
                "conversationId", conversationId,
                "userId", userId);
            
            return conversationId;

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to create conversation", "userId", userId, e);
            throw new RuntimeException("Failed to create conversation", e);
        }
    }
    
    public void addMessageToConversation(String userId, String conversationId, Message message) {
        logger.debug("Adding message to conversation", 
            "conversationId", conversationId,
            "userId", userId,
            "role", message.getRole());
        
        try {
            firestore
                .collection("users")
                .document(userId)
                .collection("conversations")
                .document(conversationId)
                .collection("messages")
                .add(message)
                .get();

            logger.debug("Successfully added message to conversation");

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to add message to conversation", 
                "conversationId", conversationId,
                "userId", userId, e);
            throw new RuntimeException("Failed to add message to conversation", e);
        }
    }
    
    public List<Message> getConversationHistory(String userId, String conversationId) {
        logger.debug("Retrieving conversation history", 
            "conversationId", conversationId,
            "userId", userId);
        
        try {
            List<QueryDocumentSnapshot> documents = firestore
                .collection("users")
                .document(userId)
                .collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .get()
                .getDocuments();
            
            List<Message> messages = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                Message message = document.toObject(Message.class);
                messages.add(message);
            }
            
            logger.debug("Successfully retrieved conversation history", 
                "messageCount", messages.size());
            
            return messages;

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to retrieve conversation history", 
                "conversationId", conversationId,
                "userId", userId, e);
            throw new RuntimeException("Failed to retrieve conversation history", e);
        }
    }
    
    public boolean conversationExists(String userId, String conversationId) {
        logger.debug("Checking if conversation exists", 
            "conversationId", conversationId,
            "userId", userId);
        
        try {
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
            
            boolean exists = !messages.isEmpty();
            logger.debug("Conversation existence check completed", "exists", exists);
            
            return exists;

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to check conversation existence", 
                "conversationId", conversationId,
                "userId", userId, e);
            throw new RuntimeException("Failed to check conversation existence", e);
        }
    }
}