package com.br.pixservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyRecord {

    private String id;
    private String scope;
    private String key;
    private String payload;
    private Instant createdAt;

    public boolean isValid() {
        return scope != null && !scope.trim().isEmpty() &&
               key != null && !key.trim().isEmpty() &&
               payload != null && !payload.trim().isEmpty();
    }

    public boolean isPixTransferRecord() {
        return "PIX_TRANSFER".equals(scope);
    }

    public boolean isPixKeyRecord() {
        return "PIX_KEY".equals(scope);
    }

    public boolean isWalletRecord() {
        return "WALLET".equals(scope);
    }
}
