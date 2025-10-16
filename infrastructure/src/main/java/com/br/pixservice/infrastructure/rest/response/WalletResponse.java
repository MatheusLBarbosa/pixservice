package com.br.pixservice.infrastructure.rest.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WalletResponse {
    private String id;
    private String ownerName;
    private String documentNumber;
    private BigDecimal balance;
}
