package com.br.pixservice.infrastructure.rest.response;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class BalanceResponse {
    private String balance;
    private OffsetDateTime asOf;
}