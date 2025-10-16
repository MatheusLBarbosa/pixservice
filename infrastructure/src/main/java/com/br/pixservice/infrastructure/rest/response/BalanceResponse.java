package com.br.pixservice.infrastructure.rest.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class BalanceResponse {
    private BigDecimal balance;
    private OffsetDateTime asOf;
}