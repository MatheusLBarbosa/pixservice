package com.br.pixservice.domain.repository;

import com.br.pixservice.domain.model.IdempotencyRecord;

import java.util.Optional;

public interface PixRecordRepository {
    Optional<IdempotencyRecord> findByScopeAndKey(String scope, String key);

    IdempotencyRecord save(IdempotencyRecord record);
}
