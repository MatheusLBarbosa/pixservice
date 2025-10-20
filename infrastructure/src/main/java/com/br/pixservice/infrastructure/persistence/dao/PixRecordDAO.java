package com.br.pixservice.infrastructure.persistence.dao;

import com.br.pixservice.domain.model.IdempotencyRecord;
import com.br.pixservice.infrastructure.persistence.entity.IdempotencyRecordEntity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PixRecordDAO {
    public static IdempotencyRecord toDomain(IdempotencyRecordEntity entity) {
        if (entity == null) {
            return null;
        }

        return IdempotencyRecord.builder()
                .id(entity.getId())
                .scope(entity.getScope())
                .key(entity.getKey())
                .payload(entity.getResult())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static IdempotencyRecordEntity toEntity(IdempotencyRecord domain) {
        if (domain == null) {
            return null;
        }

        return IdempotencyRecordEntity.builder()
                .id(domain.getId())
                .scope(domain.getScope())
                .key(domain.getKey())
                .result(domain.getPayload())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
