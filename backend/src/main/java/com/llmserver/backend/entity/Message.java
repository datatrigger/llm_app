package com.llmserver.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import com.google.cloud.Timestamp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record Message(
    @DocumentId
    String id,
    
    @NotBlank(message = "Message text cannot be empty")
    String text,

    @NotBlank(message = "Role cannot be empty")
    @Pattern(regexp = "^(user|model)$", message = "Role must be either 'user' or 'model'")
    String role,
    
    @ServerTimestamp
    Timestamp timestamp
) {
    public Message(String text, String role) {
        this(null, text, role, null);
    }
}