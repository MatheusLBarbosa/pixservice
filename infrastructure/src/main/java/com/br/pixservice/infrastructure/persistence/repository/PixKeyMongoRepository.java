package com.br.pixservice.infrastructure.persistence.repository;

import com.br.pixservice.infrastructure.persistence.entity.PixKeyEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PixKeyMongoRepository extends MongoRepository<PixKeyEntity, String> {
    Optional<PixKeyEntity> findByKey(String key);
    boolean existsByKey(String key);
}
