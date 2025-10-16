package com.br.pixservice.infrastructure.persistence.dao;

import com.br.pixservice.domain.model.Wallet;
import com.br.pixservice.infrastructure.persistence.entity.WalletEntity;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor
public class WalletDAO {
    public static Wallet toDomain(WalletEntity entity) {
        return Wallet.builder().build();
    }

    public static WalletEntity toEntity(Wallet domain) {
        return WalletEntity.builder().build();
    }
}
