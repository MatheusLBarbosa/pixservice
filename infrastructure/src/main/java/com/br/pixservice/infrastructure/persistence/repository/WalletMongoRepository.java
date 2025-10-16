package com.br.pixservice.infrastructure.persistence.repository;

import com.br.pixservice.infrastructure.persistence.entity.WalletEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletMongoRepository extends MongoRepository<WalletEntity, String> {
    Optional<WalletEntity> findByDocument(String document);
    boolean existsByDocument(String document);
}
