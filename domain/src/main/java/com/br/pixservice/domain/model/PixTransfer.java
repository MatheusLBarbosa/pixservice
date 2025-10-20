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
public class PixTransfer {

    private String id;
    private String endToEndId;
    private String idempotencyKey;
    private String sourceWalletId;
    private String targetWalletId;
    private String targetPixKey;
    private BigDecimal amount;
    private PixStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public boolean isValid() {
        return endToEndId != null &&
               sourceWalletId != null && !sourceWalletId.trim().isEmpty() &&
               targetWalletId != null && !targetWalletId.trim().isEmpty() &&
               amount != null && amount.compareTo(BigDecimal.ZERO) > 0 &&
               status != null;
    }

    public boolean isPending() {
        return PixStatus.PENDING.equals(status);
    }

    public boolean isConfirmed() {
        return PixStatus.CONFIRMED.equals(status);
    }

    public boolean isRejected() {
        return PixStatus.REJECTED.equals(status);
    }

    public void confirm() {
        if (!isPending()) {
            throw new IllegalStateException("Only pending transfers can be confirmed");
        }
        this.status = PixStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public void reject() {
        if (!isPending()) {
            throw new IllegalStateException("Only pending transfers can be rejected");
        }
        this.status = PixStatus.REJECTED;
        this.updatedAt = Instant.now();
    }

    public boolean isSameWallet() {
        return sourceWalletId != null && sourceWalletId.equals(targetWalletId);
    }

    public static PixTransfer buildPixTransfer(BigDecimal amount, String endToEndId, Wallet sender, PixKey receiverKey, String idempotencyKey) {
        return PixTransfer.builder()
                .endToEndId(endToEndId)
                .idempotencyKey(idempotencyKey)
                .amount(amount)
                .status(PixStatus.PENDING)
                .sourceWalletId(sender.getId())
                .targetWalletId(receiverKey.getWalletId())
                .endToEndId(endToEndId)
                .build();
    }
}
