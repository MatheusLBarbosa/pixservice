package com.br.pixservice.domain.repository;

import com.br.pixservice.domain.model.PixKey;

import java.util.Optional;

public interface PixKeyRepository {

    PixKey save(PixKey pixKey);

    Optional<PixKey> findByKeyValue(String keyValue);

    boolean existsByKeyValue(String keyValue);

    boolean existsByKeyValueAndWalletId(String keyValeu, String walletId);
}
