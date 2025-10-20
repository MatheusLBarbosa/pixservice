package com.br.pixservice.infrastructure.persistence.repository;

import com.br.pixservice.infrastructure.persistence.entity.LedgerEntryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerEntryMongoRepository extends MongoRepository<LedgerEntryEntity, String> {
    List<LedgerEntryEntity> findByEndToEndId(String endToEndId);
}
