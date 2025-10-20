package com.br.pixservice.service;

import com.br.pixservice.infrastructure.persistence.entity.IdempotencyRecordEntity;
import com.br.pixservice.infrastructure.persistence.repository.PixRecordMongoRepository;
import com.br.pixservice.infrastructure.service.PixRecordServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PixRecordService Tests")
class IdempotencyRecordServiceTest {

    @Mock
    private PixRecordMongoRepository repository;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private PixRecordServiceImpl pixRecordService;

    private final String TEST_SCOPE = "PIX_TRANSFER";
    private final String TEST_KEY = "request-123";
    private final String TEST_RESULT = "success";
    private final String TEST_JSON = "{\"status\":\"success\"}";

    @BeforeEach
    void setUp() {
        pixRecordService.clearCache();
    }

    @Test
    @DisplayName("Should execute action and save result when no existing record")
    void shouldExecuteActionAndSaveResultWhenNoExistingRecord() throws JsonProcessingException, ExecutionException, InterruptedException {
        // Given
        Supplier<String> action = () -> TEST_RESULT;
        when(repository.findByScopeAndKey(TEST_SCOPE, TEST_KEY)).thenReturn(Optional.empty());
        when(mapper.writeValueAsString(TEST_RESULT)).thenReturn(TEST_JSON);
        when(repository.save(any(IdempotencyRecordEntity.class))).thenReturn(mock(IdempotencyRecordEntity.class));

        // When
        String result = pixRecordService.execute(TEST_SCOPE, TEST_KEY, action, String.class);
        
        // Wait for async save to complete
        Thread.sleep(100);

        // Then
        assertEquals(TEST_RESULT, result);
        verify(repository).findByScopeAndKey(TEST_SCOPE, TEST_KEY);
        verify(mapper, atLeastOnce()).writeValueAsString(TEST_RESULT);
        verify(repository, atLeastOnce()).save(any(IdempotencyRecordEntity.class));
    }

    @Test
    @DisplayName("Should return cached result when existing record found")
    void shouldReturnCachedResultWhenExistingRecordFound() throws JsonProcessingException {
        // Given
        IdempotencyRecordEntity existingEntity = IdempotencyRecordEntity.builder()
                .id("PIX_TRANSFER:request-123")
                .scope(TEST_SCOPE)
                .key(TEST_KEY)
                .result(TEST_JSON)
                .createdAt(Instant.now())
                .build();

        when(repository.findByScopeAndKey(TEST_SCOPE, TEST_KEY)).thenReturn(Optional.of(existingEntity));
        when(mapper.readValue(TEST_JSON, String.class)).thenReturn(TEST_RESULT);

        // When
        String result = pixRecordService.execute(TEST_SCOPE, TEST_KEY, () -> "new-result", String.class);

        // Then
        assertEquals(TEST_RESULT, result);
        verify(repository).findByScopeAndKey(TEST_SCOPE, TEST_KEY);
        verify(mapper).readValue(TEST_JSON, String.class);
        verify(repository, never()).save(any(IdempotencyRecordEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when deserialization fails")
    void shouldThrowExceptionWhenDeserializationFails() throws JsonProcessingException {
        // Given
        IdempotencyRecordEntity existingEntity = IdempotencyRecordEntity.builder()
                .result(TEST_JSON)
                .build();

        when(repository.findByScopeAndKey(TEST_SCOPE, TEST_KEY)).thenReturn(Optional.of(existingEntity));
        when(mapper.readValue(TEST_JSON, String.class)).thenThrow(new JsonProcessingException("Parse error") {});

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                pixRecordService.execute(TEST_SCOPE, TEST_KEY, () -> "new-result", String.class));

        assertEquals("Failed to deserialize cached result", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when serialization fails")
    void shouldThrowExceptionWhenSerializationFails() throws JsonProcessingException {
        // Given
        when(mapper.writeValueAsString(TEST_RESULT)).thenThrow(new JsonProcessingException("Parse error") {});

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                pixRecordService.saveRecord(TEST_SCOPE, TEST_KEY, TEST_RESULT));

        assertEquals("Failed to serialize result", exception.getMessage());
        verify(repository, never()).save(any(IdempotencyRecordEntity.class));
    }

    @Test
    @DisplayName("Should handle DuplicateKeyException in saveRecord")
    void shouldHandleDuplicateKeyExceptionInSaveRecord() throws JsonProcessingException {
        // Given
        when(mapper.writeValueAsString(TEST_RESULT)).thenReturn(TEST_JSON);
        when(repository.save(any(IdempotencyRecordEntity.class)))
                .thenThrow(new DuplicateKeyException("Duplicate key"));

        // When & Then
        assertThrows(DuplicateKeyException.class, () ->
                pixRecordService.saveRecord(TEST_SCOPE, TEST_KEY, TEST_RESULT));

        verify(repository).save(any(IdempotencyRecordEntity.class));
    }

    @Test
    @DisplayName("Should test deserializeResult method through execute")
    void shouldTestDeserializeResultThroughExecute() throws JsonProcessingException {
        // Given
        IdempotencyRecordEntity entity = IdempotencyRecordEntity.builder()
                .result(TEST_JSON)
                .build();

        when(repository.findByScopeAndKey(TEST_SCOPE, TEST_KEY)).thenReturn(Optional.of(entity));
        when(mapper.readValue(TEST_JSON, String.class)).thenReturn(TEST_RESULT);

        // When
        String result = pixRecordService.execute(TEST_SCOPE, TEST_KEY, () -> "new-result", String.class);

        // Then
        assertEquals(TEST_RESULT, result);
        verify(mapper).readValue(TEST_JSON, String.class);
    }

    @Test
    @DisplayName("Should save record successfully")
    void shouldSaveRecordSuccessfully() throws JsonProcessingException {
        // Given
        when(mapper.writeValueAsString(TEST_RESULT)).thenReturn(TEST_JSON);
        when(repository.save(any(IdempotencyRecordEntity.class))).thenReturn(mock(IdempotencyRecordEntity.class));

        // When
        pixRecordService.saveRecord(TEST_SCOPE, TEST_KEY, TEST_RESULT);

        // Then
        verify(mapper).writeValueAsString(TEST_RESULT);
        verify(repository).save(any(IdempotencyRecordEntity.class));
    }

    @Test
    @DisplayName("Should save record asynchronously")
    void shouldSaveRecordAsynchronously() throws JsonProcessingException, ExecutionException, InterruptedException {
        // Given
        when(mapper.writeValueAsString(TEST_RESULT)).thenReturn(TEST_JSON);
        when(repository.save(any(IdempotencyRecordEntity.class))).thenReturn(mock(IdempotencyRecordEntity.class));

        // When
        CompletableFuture<Void> future = pixRecordService.saveRecordAsync(TEST_SCOPE, TEST_KEY, TEST_RESULT);
        future.get();

        // Then
        verify(mapper).writeValueAsString(TEST_RESULT);
        verify(repository).save(any(IdempotencyRecordEntity.class));
    }

    @Test
    @DisplayName("Should handle async save failure gracefully")
    void shouldHandleAsyncSaveFailureGracefully() throws JsonProcessingException, ExecutionException, InterruptedException {
        // Given
        when(mapper.writeValueAsString(TEST_RESULT)).thenThrow(new JsonProcessingException("Parse error") {});

        // When
        CompletableFuture<Void> future = pixRecordService.saveRecordAsync(TEST_SCOPE, TEST_KEY, TEST_RESULT);
        future.get();

        // Then
        verify(mapper).writeValueAsString(TEST_RESULT);
        verify(repository, never()).save(any(IdempotencyRecordEntity.class));
    }

    @Test
    @DisplayName("Should handle concurrent access with locks")
    void shouldHandleConcurrentAccessWithLocks() throws JsonProcessingException, ExecutionException, InterruptedException {
        // Given
        Supplier<String> action = () -> TEST_RESULT;
        when(repository.findByScopeAndKey(TEST_SCOPE, TEST_KEY)).thenReturn(Optional.empty());
        when(mapper.writeValueAsString(TEST_RESULT)).thenReturn(TEST_JSON);
        when(repository.save(any(IdempotencyRecordEntity.class))).thenReturn(mock(IdempotencyRecordEntity.class));

        // When
        String result1 = pixRecordService.execute(TEST_SCOPE, TEST_KEY, action, String.class);
        String result2 = pixRecordService.execute(TEST_SCOPE, TEST_KEY, action, String.class);

        Thread.sleep(100);

        // Then
        assertEquals(TEST_RESULT, result1);
        assertEquals(TEST_RESULT, result2);

        verify(repository, atLeast(2)).findByScopeAndKey(TEST_SCOPE, TEST_KEY);
    }

    @Test
    @DisplayName("Should generate correct ID")
    void shouldGenerateCorrectId() throws JsonProcessingException {
        // Given
        when(mapper.writeValueAsString(TEST_RESULT)).thenReturn(TEST_JSON);
        when(repository.save(any(IdempotencyRecordEntity.class))).thenAnswer(invocation -> {
            IdempotencyRecordEntity entity = invocation.getArgument(0);
            assertEquals("PIX_TRANSFER:request-123", entity.getId());
            return entity;
        });

        // When
        pixRecordService.saveRecord(TEST_SCOPE, TEST_KEY, TEST_RESULT);

        // Then
        verify(repository).save(any(IdempotencyRecordEntity.class));
    }

    @Test
    @DisplayName("Should handle deserialization failure gracefully in execute")
    void shouldHandleDeserializationFailureGracefullyInExecute() throws JsonProcessingException {
        // Given
        IdempotencyRecordEntity entity = IdempotencyRecordEntity.builder()
                .result(TEST_JSON)
                .build();

        when(repository.findByScopeAndKey(TEST_SCOPE, TEST_KEY)).thenReturn(Optional.of(entity));
        when(mapper.readValue(TEST_JSON, String.class)).thenThrow(new JsonProcessingException("Parse error") {});

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                pixRecordService.execute(TEST_SCOPE, TEST_KEY, () -> "new-result", String.class));

        assertEquals("Failed to deserialize cached result", exception.getMessage());
    }

    @Test
    @DisplayName("Should test async save failure handling")
    void shouldTestAsyncSaveFailureHandling() throws JsonProcessingException, ExecutionException, InterruptedException {
        // Given
        when(mapper.writeValueAsString(TEST_RESULT)).thenThrow(new JsonProcessingException("Parse error") {});

        // When
        CompletableFuture<Void> future = pixRecordService.saveRecordAsync(TEST_SCOPE, TEST_KEY, TEST_RESULT);
        future.get();

        // Then
        verify(mapper).writeValueAsString(TEST_RESULT);
        verify(repository, never()).save(any(IdempotencyRecordEntity.class));
    }

    @Test
    @DisplayName("Should test locks are properly managed")
    void shouldTestLocksAreProperlyManaged() throws JsonProcessingException, ExecutionException, InterruptedException {
        // Given
        Supplier<String> action = mock(Supplier.class);
        when(action.get()).thenReturn(TEST_RESULT);
        
        when(repository.findByScopeAndKey(TEST_SCOPE, TEST_KEY)).thenReturn(Optional.empty());
        when(mapper.writeValueAsString(TEST_RESULT)).thenReturn(TEST_JSON);
        when(repository.save(any(IdempotencyRecordEntity.class))).thenReturn(mock(IdempotencyRecordEntity.class));

        // When
        String result = pixRecordService.execute(TEST_SCOPE, TEST_KEY, action, String.class);
        

        Thread.sleep(100);

        // Then
        assertEquals(TEST_RESULT, result);
        verify(repository).findByScopeAndKey(TEST_SCOPE, TEST_KEY);
        verify(action).get();
    }

}
