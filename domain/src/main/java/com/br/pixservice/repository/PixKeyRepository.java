package com.br.pixservice.repository;

import com.br.pixservice.model.PixKey;

import java.util.Optional;

public interface PixKeyRepository {

    PixKey save(PixKey pixKey);

    Optional<PixKey> findByKeyValue(String keyValue);

    boolean existsByKeyValue(String keyValue);
}
