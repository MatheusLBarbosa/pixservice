package com.br.pixservice.infrastructure.persistence.repository;

import com.br.pixservice.infrastructure.persistence.entity.PixRecordEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PixRecordMongoRepository extends MongoRepository<PixRecordEntity, String> {

    List<PixRecordEntity> findByScope(String scope);

    List<PixRecordEntity> findByKey(String key);

    Optional<PixRecordEntity> findByScopeAndKey(String scope, String key);
}
