package com.br.pixservice.infrastructure.persistence.repository;

import com.br.pixservice.infrastructure.persistence.entity.PixRecordEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PixRecordMongoRepository extends MongoRepository<PixRecordEntity, String> {
    Optional<PixRecordEntity> findByScopeAndKey(String scope, String key);
}
