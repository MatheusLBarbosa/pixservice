package com.br.pixservice.repository;

import com.br.pixservice.model.LedgerEntry;

import java.time.OffsetDateTime;
import java.util.List;

public interface LedgerRepository {
    LedgerEntry save(LedgerEntry entry);

    List<LedgerEntry> findByWalletId(String walletId);

    List<LedgerEntry> findByWalletIdAndBefore(String walletId, OffsetDateTime before);

    List<LedgerEntry> findByEndToEndId(String endToEndId);
}
