package com.br.pixservice.infrastructure.persistence.repository;

import com.br.pixservice.domain.model.Wallet;
import com.br.pixservice.domain.repository.WalletRepository;
import com.br.pixservice.infrastructure.persistence.dao.WalletDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WalletRepositoryImpl implements WalletRepository {

    private final WalletMongoRepository repository;

    @Override
    public Wallet save(Wallet wallet) {
        return WalletDAO.toDomain(repository.save(WalletDAO.toEntity(wallet)));
    }

    @Override
    public Optional<Wallet> findById(String walletId) {
        return repository.findById(walletId).map(WalletDAO::toDomain);
    }

    @Override
    public Optional<Wallet> findByDocumentNumber(String documentNumber) {
        return repository.findByDocument(documentNumber).map(WalletDAO::toDomain);
    }

    @Override
    public void updateBalance(String walletId, BigDecimal newBalance, Long expectedVersion) {
    }

    @Override
    public boolean existsByDocumentNumber(String documentNumber) {
        return false;
    }
}
