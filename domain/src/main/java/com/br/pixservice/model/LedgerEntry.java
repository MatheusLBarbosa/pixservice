package com.br.pixservice.model;

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
    private String type;
    private Instant createdAt;

    public boolean isValid() {
        return walletId != null && !walletId.trim().isEmpty() &&
               endToEndId != null && !endToEndId.trim().isEmpty() &&
               amount != null && amount.compareTo(BigDecimal.ZERO) > 0 &&
               type != null && (isCredit() || isDebit());
    }

    public boolean isCredit() {
        return "CREDIT".equals(type);
    }

    public boolean isDebit() {
        return "DEBIT".equals(type);
    }

    public static LedgerEntry createCredit(String walletId, String endToEndId, BigDecimal amount) {
        return LedgerEntry.builder()
                .walletId(walletId)
                .endToEndId(endToEndId)
                .amount(amount)
                .type("CREDIT")
                .createdAt(Instant.now())
                .build();
    }

    public static LedgerEntry createDebit(String walletId, String endToEndId, BigDecimal amount) {
        return LedgerEntry.builder()
                .walletId(walletId)
                .endToEndId(endToEndId)
                .amount(amount)
                .type("DEBIT")
                .createdAt(Instant.now())
                .build();
    }
}
