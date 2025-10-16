package com.br.pixservice.infrastructure.persistence.dao;

import com.br.pixservice.domain.model.PixKey;
import com.br.pixservice.infrastructure.persistence.entity.PixKeyEntity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PixKeyDAO {
    public static PixKey toDomain(PixKeyEntity entity) {
        return PixKey.builder()
                .key(entity.getKey())
                .type(entity.getType().toString())
                .walletId(entity.getWalletId())
                .build();
    }

    public static PixKeyEntity toEntity(PixKey domain) {
        return PixKeyEntity.builder()
                .type(PixKeyEntity.PixKeyType.valueOf(domain.getType()))
                .key(domain.getKey())
                .walletId(domain.getWalletId())
        .build();
    }
}
