package com.br.pixservice.infrastructure.persistence.repository;

import com.br.pixservice.infrastructure.persistence.entity.PixTransferEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PixTransferMongoRepository extends MongoRepository<PixTransferEntity, String> {
    Optional<PixTransferEntity> findByEndToEndId(String endToEndId);
}
