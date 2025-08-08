package com.llmserver.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.llmserver.backend.entity.Message;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference usersCollection;

    @Mock
    private DocumentReference userDoc;

    @Mock
    private CollectionReference conversationsCollection;

    @Mock
    private DocumentReference conversationDoc;

    @Mock
    private CollectionReference messagesCollection;

    @Mock
    private ApiFuture<DocumentReference> documentReferenceFuture;

    @Mock
    private ApiFuture<WriteResult> writeResultFuture;

    @Mock
    private ApiFuture<QuerySnapshot> querySnapshotFuture;

    @Mock
    private QuerySnapshot querySnapshot;

    @Mock
    private Query query;

    private ConversationService conversationService;

    @BeforeEach
    void setUp() {
        conversationService = new ConversationService(firestore);
    }

    @Test
    void shouldCreateConversationSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        String userId = "user123";
        Message message = new Message("Hello", "user");
        String expectedConversationId = "conv123";

        when(firestore.collection("users")).thenReturn(usersCollection);
        when(usersCollection.document(userId)).thenReturn(userDoc);
        when(userDoc.collection("conversations")).thenReturn(conversationsCollection);
        when(conversationsCollection.document()).thenReturn(conversationDoc);
        when(conversationDoc.getId()).thenReturn(expectedConversationId);
        when(conversationDoc.collection("messages")).thenReturn(messagesCollection);
        when(messagesCollection.add(message)).thenReturn(documentReferenceFuture);
        when(documentReferenceFuture.get()).thenReturn(mock(DocumentReference.class));

        // When
        String conversationId = conversationService.createConversation(userId, message);

        // Then
        assertEquals(expectedConversationId, conversationId);
        verify(messagesCollection).add(message);
    }

    @Test
    void shouldAddMessageToConversationSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        String userId = "user123";
        String conversationId = "conv123";
        Message message = new Message("Hello", "user");

        when(firestore.collection("users")).thenReturn(usersCollection);
        when(usersCollection.document(userId)).thenReturn(userDoc);
        when(userDoc.collection("conversations")).thenReturn(conversationsCollection);
        when(conversationsCollection.document(conversationId)).thenReturn(conversationDoc);
        when(conversationDoc.collection("messages")).thenReturn(messagesCollection);
        when(messagesCollection.add(message)).thenReturn(documentReferenceFuture);
        when(documentReferenceFuture.get()).thenReturn(mock(DocumentReference.class));

        // When
        conversationService.addMessageToConversation(userId, conversationId, message);

        // Then
        verify(messagesCollection).add(message);
    }

    @Test
    void shouldGetConversationHistorySuccessfully() throws ExecutionException, InterruptedException {
        // Given
        String userId = "user123";
        String conversationId = "conv123";
        
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot doc2 = mock(QueryDocumentSnapshot.class);
        Message message1 = new Message("Hello", "user");
        Message message2 = new Message("Hi there!", "model");

        when(firestore.collection("users")).thenReturn(usersCollection);
        when(usersCollection.document(userId)).thenReturn(userDoc);
        when(userDoc.collection("conversations")).thenReturn(conversationsCollection);
        when(conversationsCollection.document(conversationId)).thenReturn(conversationDoc);
        when(conversationDoc.collection("messages")).thenReturn(messagesCollection);
        when(messagesCollection.orderBy("timestamp", Query.Direction.ASCENDING)).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(List.of(doc1, doc2));
        when(doc1.toObject(Message.class)).thenReturn(message1);
        when(doc2.toObject(Message.class)).thenReturn(message2);

        // When
        List<Message> history = conversationService.getConversationHistory(userId, conversationId);

        // Then
        assertEquals(2, history.size());
        assertEquals("Hello", history.get(0).text());
        assertEquals("user", history.get(0).role());
        assertEquals("Hi there!", history.get(1).text());
        assertEquals("model", history.get(1).role());
    }

    @Test
    void shouldReturnTrueWhenConversationExists() throws ExecutionException, InterruptedException {
        // Given
        String userId = "user123";
        String conversationId = "conv123";
        
        QueryDocumentSnapshot doc = mock(QueryDocumentSnapshot.class);

        when(firestore.collection("users")).thenReturn(usersCollection);
        when(usersCollection.document(userId)).thenReturn(userDoc);
        when(userDoc.collection("conversations")).thenReturn(conversationsCollection);
        when(conversationsCollection.document(conversationId)).thenReturn(conversationDoc);
        when(conversationDoc.collection("messages")).thenReturn(messagesCollection);
        when(messagesCollection.limit(1)).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(List.of(doc));

        // When
        boolean exists = conversationService.conversationExists(userId, conversationId);

        // Then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenConversationDoesNotExist() throws ExecutionException, InterruptedException {
        // Given
        String userId = "user123";
        String conversationId = "conv123";

        when(firestore.collection("users")).thenReturn(usersCollection);
        when(usersCollection.document(userId)).thenReturn(userDoc);
        when(userDoc.collection("conversations")).thenReturn(conversationsCollection);
        when(conversationsCollection.document(conversationId)).thenReturn(conversationDoc);
        when(conversationDoc.collection("messages")).thenReturn(messagesCollection);
        when(messagesCollection.limit(1)).thenReturn(query);
        when(query.get()).thenReturn(querySnapshotFuture);
        when(querySnapshotFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(List.of());

        // When
        boolean exists = conversationService.conversationExists(userId, conversationId);

        // Then
        assertFalse(exists);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenCreateConversationFails() throws ExecutionException, InterruptedException {
        // Given
        String userId = "user123";
        Message message = new Message("Hello", "user");

        when(firestore.collection("users")).thenReturn(usersCollection);
        when(usersCollection.document(userId)).thenReturn(userDoc);
        when(userDoc.collection("conversations")).thenReturn(conversationsCollection);
        when(conversationsCollection.document()).thenReturn(conversationDoc);
        when(conversationDoc.collection("messages")).thenReturn(messagesCollection);
        when(messagesCollection.add(message)).thenReturn(documentReferenceFuture);
        when(documentReferenceFuture.get()).thenThrow(new ExecutionException("Database error", new RuntimeException()));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            conversationService.createConversation(userId, message);
        });

        assertEquals("Failed to create conversation", exception.getMessage());
    }

    @Test
    void shouldThrowRuntimeExceptionWhenAddMessageFails() throws ExecutionException, InterruptedException {
        // Given
        String userId = "user123";
        String conversationId = "conv123";
        Message message = new Message("Hello", "user");

        when(firestore.collection("users")).thenReturn(usersCollection);
        when(usersCollection.document(userId)).thenReturn(userDoc);
        when(userDoc.collection("conversations")).thenReturn(conversationsCollection);
        when(conversationsCollection.document(conversationId)).thenReturn(conversationDoc);
        when(conversationDoc.collection("messages")).thenReturn(messagesCollection);
        when(messagesCollection.add(message)).thenReturn(documentReferenceFuture);
        when(documentReferenceFuture.get()).thenThrow(new ExecutionException("Database error", new RuntimeException()));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            conversationService.addMessageToConversation(userId, conversationId, message);
        });

        assertEquals("Failed to add message to conversation", exception.getMessage());
    }
}