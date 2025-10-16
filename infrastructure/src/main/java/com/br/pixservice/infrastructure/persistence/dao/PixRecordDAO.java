package com.br.pixservice.infrastructure.persistence.dao;

import com.br.pixservice.domain.model.PixRecord;
import com.br.pixservice.infrastructure.persistence.entity.PixRecordEntity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PixRecordDAO {
    public static PixRecord toDomain(PixRecordEntity entity) {
        return PixRecord.builder().build();
    }

    public static PixRecordEntity toEntity(PixRecord domain) {
        return PixRecordEntity.builder().build();
    }
}
