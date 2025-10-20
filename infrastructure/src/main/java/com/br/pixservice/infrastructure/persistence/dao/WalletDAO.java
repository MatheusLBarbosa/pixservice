package com.br.pixservice.infrastructure.persistence.dao;

import com.br.pixservice.domain.model.Wallet;
import com.br.pixservice.infrastructure.persistence.entity.WalletEntity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class WalletDAO {
    public static Wallet toDomain(WalletEntity entity) {

        return Wallet.builder()
                .id(entity.getId())
                .ownerName(entity.getOwnerName())
                .document(entity.getDocument())
                .balance(entity.getBalance())
                .version(entity.getVersion())
                .build();
    }

    public static WalletEntity toEntity(Wallet domain) {
        return WalletEntity.builder()
                .ownerName(domain.getOwnerName())
                .document(domain.getDocument())
                .balance(domain.getBalance())
                .build();
    }
}
