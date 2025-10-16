package com.br.pixservice.infrastructure.persistence.repository;

import com.br.pixservice.domain.model.PixKey;
import com.br.pixservice.domain.repository.PixKeyRepository;
import com.br.pixservice.infrastructure.persistence.dao.PixKeyDAO;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class PixKeyRepositoryImpl implements PixKeyRepository {

    private final PixKeyMongoRepository repository;

    @Override
    public PixKey save(PixKey pixKey) {
        return PixKeyDAO.toDomain(repository.save(PixKeyDAO.toEntity(pixKey)));
    }

    @Override
    public Optional<PixKey> findByKeyValue(String keyValue) {
        return Optional.empty();
    }

    @Override
    public boolean existsByKeyValue(String keyValue) {
        return repository.existsByKey(keyValue);
    }

    @Override
    public boolean existsByKeyValueAndWalletId(String keyValeu, String walletId) {
        return false;
    }
}
