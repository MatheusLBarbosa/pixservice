package com.br.pixservice.infrastructure.persistence.dao;

import com.br.pixservice.domain.model.PixTransfer;
import com.br.pixservice.infrastructure.persistence.entity.PixTransferEntity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PixTransferDAO {
    public static PixTransfer toDomain(PixTransferEntity entity) {
        return PixTransfer.builder().build();
    }

    public static PixTransferEntity toEntity(PixTransfer domain) {
        return PixTransferEntity.builder().build();
    }
}
