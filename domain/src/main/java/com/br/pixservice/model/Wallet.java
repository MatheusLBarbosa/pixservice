package com.br.pixservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    private String id;
    private String ownerName;
    private String document;
    private BigDecimal balance;
    private Long version;

    public boolean hasSufficientBalance(BigDecimal amount) {
        return balance != null && balance.compareTo(amount) >= 0;
    }

    public void debit(BigDecimal amount) {
        if (!hasSufficientBalance(amount)) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
        this.balance = this.balance.add(amount);
    }
}
