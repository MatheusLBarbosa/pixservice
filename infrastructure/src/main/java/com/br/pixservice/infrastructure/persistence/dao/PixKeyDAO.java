package com.br.pixservice.infrastructure.persistence.dao;

import com.br.pixservice.domain.model.PixKey;
import com.br.pixservice.infrastructure.persistence.entity.PixKeyEntity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PixKeyDAO {
    public static PixKey toDomain(PixKeyEntity entity) {
        return PixKey.builder().build();
    }

    public static PixKeyEntity toEntity(PixKey domain) {
        return PixKeyEntity.builder().build();
    }
}
