package com.br.pixservice.domain.repository;

import com.br.pixservice.domain.model.PixRecord;

import java.util.Optional;

public interface PixRecordRepository {
    Optional<PixRecord> findByScopeAndKey(String scope, String key);

    PixRecord save(PixRecord record);
}
