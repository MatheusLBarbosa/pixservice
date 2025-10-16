package com.br.pixservice.infrastructure.persistence.dao;

import com.br.pixservice.domain.model.LedgerEntry;
import com.br.pixservice.infrastructure.persistence.entity.LedgerEntryEntity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LedgerEntryDAO {

    public static LedgerEntryEntity toEntity(LedgerEntry domain) {
        return LedgerEntryEntity.builder()
                .walletId(domain.getWalletId())
                .amount(domain.getAmount())
                .type(domain.getType())
                .endToEndId(domain.getEndToEndId())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public static LedgerEntry toDomain(LedgerEntryEntity entity) {
        return LedgerEntry.builder()
                .walletId(entity.getWalletId())
                .amount(entity.getAmount())
                .endToEndId(entity.getEndToEndId())
                .type(entity.getType())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
