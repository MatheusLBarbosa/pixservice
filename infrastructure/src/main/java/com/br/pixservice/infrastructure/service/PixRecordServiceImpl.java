package com.br.pixservice.infrastructure.service;

import com.br.pixservice.domain.service.PixRecordService;
import com.br.pixservice.infrastructure.persistence.entity.IdempotencyRecordEntity;
import com.br.pixservice.infrastructure.persistence.repository.PixRecordMongoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class PixRecordServiceImpl implements PixRecordService {

    private final PixRecordMongoRepository repository;
    private final ObjectMapper mapper;
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Cacheable(value = "pixRecords", key = "#scope + ':' + #key", unless = "#result == null")
    public <T> T execute(String scope, String key, Supplier<T> action, Class<T> resultType) {
        String lockKey = scope + ":" + key;
        ReentrantLock lock = locks.computeIfAbsent(lockKey, k -> new ReentrantLock());
        
        lock.lock();
        try {
            Optional<IdempotencyRecordEntity> existingRecord = repository.findByScopeAndKey(scope, key);
            
            if (existingRecord.isPresent()) {
                log.debug("Returning cached result for scope: {} key: {}", scope, key);
                return deserializeResult(existingRecord.get().getResult(), resultType)
                        .orElseThrow(() -> new RuntimeException("Failed to deserialize cached result"));
            }

            log.debug("Executing action for scope: {} key: {}", scope, key);
            T result = action.get();
            
            saveRecordAsync(scope, key, result);
            return result;
            
        } finally {
            lock.unlock();
        }
    }

    public <T> void saveRecord(String scope, String key, T result) {
        try {
            String serializedResult = mapper.writeValueAsString(result);
            String id = generateId(scope, key);
            
            IdempotencyRecordEntity record = IdempotencyRecordEntity.builder()
                    .id(id)
                    .scope(scope)
                    .key(key)
                    .result(serializedResult)
                    .createdAt(Instant.now())
                    .build();

            repository.save(record);
            log.debug("Saved record for scope: {} key: {}", scope, key);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize result for scope: {} key: {}", scope, key, e);
            throw new RuntimeException("Failed to serialize result", e);
        } catch (DuplicateKeyException e) {
            log.warn("Duplicate record detected for scope: {} key: {}", scope, key);
            throw e;
        }
    }

    public CompletableFuture<Void> saveRecordAsync(String scope, String key, Object result) {
        return CompletableFuture.runAsync(() -> {
            try {
                saveRecord(scope, key, result);
            } catch (Exception e) {
                log.error("Async save failed for scope: {} key: {}", scope, key, e);
            }
        });
    }

    private <T> Optional<T> deserializeResult(String jsonResult, Class<T> resultType) {
        try {
            return Optional.ofNullable(mapper.readValue(jsonResult, resultType));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize result for type: {}", resultType.getSimpleName(), e);
            return Optional.empty();
        }
    }

    private String generateId(String scope, String key) {
        return scope + ":" + key;
    }

    public void clearCache() {
        locks.clear();
        log.info("Cleared PixRecordService cache");
    }
}
