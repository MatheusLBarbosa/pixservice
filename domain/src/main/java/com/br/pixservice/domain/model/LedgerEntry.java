package com.br.pixservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry {

    private String id;
    private String walletId;
    private String endToEndId;
    private BigDecimal amount;
    private LedgerEntryType type;
    private Instant createdAt;

    public boolean isValid() {
        return walletId != null && !walletId.trim().isEmpty() &&
               endToEndId != null &&
               amount != null && amount.compareTo(BigDecimal.ZERO) > 0 &&
               type != null && (isCredit() || isDebit());
    }

    public boolean isCredit() {
        return LedgerEntryType.CREDIT.equals(type);
    }

    public boolean isDebit() {
        return LedgerEntryType.DEBIT.equals(type);
    }

    public static LedgerEntry createCredit(String walletId, String endToEndId, BigDecimal amount) {
        return LedgerEntry.builder()
                .walletId(walletId)
                .endToEndId(endToEndId)
                .amount(amount)
                .type(LedgerEntryType.CREDIT)
                .createdAt(Instant.now())
                .build();
    }

    public static LedgerEntry createDebit(String walletId, String endToEndId, BigDecimal amount) {
        return LedgerEntry.builder()
                .walletId(walletId)
                .endToEndId(endToEndId)
                .amount(amount)
                .type(LedgerEntryType.DEBIT)
                .createdAt(Instant.now())
                .build();
    }
}
