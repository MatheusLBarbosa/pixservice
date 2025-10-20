package com.br.pixservice.domain.event;

import com.br.pixservice.domain.model.PixKey;
import com.br.pixservice.domain.model.Wallet;
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
public class PixTransferEvent {
    private String endToEndId;
    private String sourceWalletId;
    private String targetPixKey;
    private String targetWalletId;
    private BigDecimal amount;
    private Instant timestamp;

    public static PixTransferEvent toPixTransferEvent(
            BigDecimal amount,
            String endToEndId,
            Wallet sender,
            PixKey receiverKey) {

        return PixTransferEvent.builder()
                .endToEndId(endToEndId)
                .sourceWalletId(sender.getId())
                .targetPixKey(receiverKey.getKey())
                .targetWalletId(receiverKey.getWalletId())
                .amount(amount)
                .timestamp(Instant.now())
                .build();
    }

    public boolean isValid() {
        return endToEndId != null && !endToEndId.trim().isEmpty()
                && sourceWalletId != null && !sourceWalletId.trim().isEmpty()
                && targetPixKey != null && !targetPixKey.trim().isEmpty()
                && amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
}