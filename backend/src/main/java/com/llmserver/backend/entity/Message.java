package com.llmserver.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.google.cloud.Timestamp;

public class Message {
    
    @DocumentId
    private String id;
    
    @NotBlank(message = "Message text cannot be empty")
    private String text;

    @NotBlank(message = "Role cannot be empty")
    @Pattern(regexp = "^(user|model)$", message = "Role must be either 'user' or 'model'")
    private String role;
    
    @ServerTimestamp
    private Timestamp timestamp;
    
    public Message() {
        // Default constructor required for Firestore
    }
    
    public Message(String text, String role) {
        this.text = text;
        this.role = role;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public Timestamp getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}