package com.br.pixservice.infrastructure.persistence.repository;

import com.br.pixservice.infrastructure.persistence.entity.LedgerEntryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface LedgerEntryMongoRepository extends MongoRepository<LedgerEntryEntity, String> {

    List<LedgerEntryEntity> findByWalletId(String walletId);

    List<LedgerEntryEntity> findByEndToEndId(String endToEndId);

    List<LedgerEntryEntity> findByWalletIdAndOccurredAtBefore(String walletId, OffsetDateTime before);

}
