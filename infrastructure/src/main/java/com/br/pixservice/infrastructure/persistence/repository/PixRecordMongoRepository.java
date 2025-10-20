package com.br.pixservice.infrastructure.persistence.repository;

import com.br.pixservice.infrastructure.persistence.entity.IdempotencyRecordEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PixRecordMongoRepository extends MongoRepository<IdempotencyRecordEntity, String> {
    Optional<IdempotencyRecordEntity> findByScopeAndKey(String scope, String key);
}
