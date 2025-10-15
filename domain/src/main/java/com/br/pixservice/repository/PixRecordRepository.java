package com.br.pixservice.repository;

import com.br.pixservice.model.PixRecord;

import java.util.Optional;

public interface PixRecordRepository {
    Optional<PixRecord> findByScopeAndKey(String scope, String key);

    PixRecord save(PixRecord record);
}
