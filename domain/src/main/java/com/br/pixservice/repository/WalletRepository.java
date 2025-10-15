package com.br.pixservice.repository;

import com.br.pixservice.model.Wallet;

import java.math.BigDecimal;
import java.util.Optional;

public interface WalletRepository {
    Wallet save(Wallet wallet);

    Optional<Wallet> findById(String walletId);

    Optional<Wallet> findByDocumentNumber(String documentNumber);

    void updateBalance(String walletId, BigDecimal newBalance, Long expectedVersion);

    boolean existsByDocumentNumber(String documentNumber);
}
