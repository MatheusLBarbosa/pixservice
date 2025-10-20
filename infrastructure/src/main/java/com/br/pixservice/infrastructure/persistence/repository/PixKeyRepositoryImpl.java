package com.br.pixservice.infrastructure.persistence.repository;

import com.br.pixservice.domain.model.PixKey;
import com.br.pixservice.domain.repository.PixKeyRepository;
import com.br.pixservice.infrastructure.persistence.dao.PixKeyDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PixKeyRepositoryImpl implements PixKeyRepository {

    private final PixKeyMongoRepository repository;

    @Override
    public PixKey save(PixKey pixKey) {
        return PixKeyDAO.toDomain(repository.save(PixKeyDAO.toEntity(pixKey)));
    }

    @Override
    public Optional<PixKey> findByKey(String keyValue) {
        return repository.findByKey(keyValue).map(PixKeyDAO::toDomain);
    }

    @Override
    public boolean existsByKey(String keyValue) {
        return repository.existsByKey(keyValue);
    }

    @Override
    public boolean existsByKeyValueAndWalletId(String keyValeu, String walletId) {
        return false;
    }
}
