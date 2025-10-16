package com.br.pixservice.domain.repository;

import com.br.pixservice.domain.model.LedgerEntry;

import java.time.OffsetDateTime;
import java.util.List;

public interface LedgerRepository {
    LedgerEntry save(LedgerEntry entry);

    List<LedgerEntry> findByWalletId(String walletId);

    List<LedgerEntry> findByWalletIdAndBefore(String walletId, OffsetDateTime before);

    List<LedgerEntry> findByEndToEndId(String endToEndId);
}
