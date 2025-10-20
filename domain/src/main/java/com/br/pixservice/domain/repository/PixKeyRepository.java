package com.br.pixservice.domain.repository;

import com.br.pixservice.domain.model.PixKey;

import java.util.Optional;

public interface PixKeyRepository {

    PixKey save(PixKey pixKey);

    Optional<PixKey> findByKey(String keyValue);

    boolean existsByKey(String keyValue);

    boolean existsByKeyValueAndWalletId(String keyValeu, String walletId);
}
