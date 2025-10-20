package com.br.pixservice.infrastructure.persistence.dao;

import com.br.pixservice.domain.model.PixStatus;
import com.br.pixservice.domain.model.PixTransfer;
import com.br.pixservice.infrastructure.persistence.entity.PixTransferEntity;
import lombok.NoArgsConstructor;

import static com.br.pixservice.domain.model.PixStatus.*;

@NoArgsConstructor
public class PixTransferDAO {
    public static PixTransfer toDomain(PixTransferEntity entity) {
        if (entity == null) {
            return null;
        }

        return PixTransfer.builder()
                .id(entity.getId())
                .endToEndId(entity.getEndToEndId())
                .sourceWalletId(entity.getSourceWalletId())
                .targetWalletId(entity.getTargetWalletId())
                .amount(entity.getAmount())
                .status(mapStatusToDomain(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static PixTransferEntity toEntity(PixTransfer domain) {
        if (domain == null) {
            return null;
        }

        return PixTransferEntity.builder()
                .endToEndId(domain.getEndToEndId())
                .sourceWalletId(domain.getSourceWalletId())
                .targetWalletId(domain.getTargetWalletId())
                .amount(domain.getAmount())
                .status(mapStatusToEntity(domain.getStatus()))
                .build();
    }

    private static PixStatus mapStatusToDomain(PixStatus entityStatus) {
        if (entityStatus == null) {
            return null;
        }

        return switch (entityStatus) {
            case PENDING -> PENDING;
            case CONFIRMED -> CONFIRMED;
            case REJECTED -> REJECTED;
        };
    }

    private static PixStatus mapStatusToEntity(PixStatus domainStatus) {
        if (domainStatus == null) {
            return null;
        }

        return switch (domainStatus) {
            case PENDING -> PixStatus.PENDING;
            case CONFIRMED -> PixStatus.CONFIRMED;
            case REJECTED -> PixStatus.REJECTED;
        };
    }
}
