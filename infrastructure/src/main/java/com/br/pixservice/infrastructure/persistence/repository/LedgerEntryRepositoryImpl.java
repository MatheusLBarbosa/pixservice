package com.br.pixservice.infrastructure.persistence.repository;

import com.br.pixservice.domain.model.LedgerEntry;
import com.br.pixservice.domain.repository.LedgerRepository;
import com.br.pixservice.infrastructure.persistence.dao.LedgerEntryDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LedgerEntryRepositoryImpl implements LedgerRepository {

    private final LedgerEntryMongoRepository repository;

    @Override
    public LedgerEntry save(LedgerEntry entry) {
        return LedgerEntryDAO.toDomain(repository.save(LedgerEntryDAO.toEntity(entry)));
    }

    @Override
    public List<LedgerEntry> findByWalletId(String walletId) {
        return List.of();
    }

    @Override
    public List<LedgerEntry> findByWalletIdAndOccurredAt(String walletId, OffsetDateTime occurredAt) {
        return List.of();
    }

    @Override
    public List<LedgerEntry> findByEndToEndId(String endToEndId) {
        return repository.findByEndToEndId(endToEndId).stream().map(LedgerEntryDAO::toDomain).toList();
    }
}

