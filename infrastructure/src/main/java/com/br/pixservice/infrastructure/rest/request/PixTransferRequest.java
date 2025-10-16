package com.br.pixservice.infrastructure.rest.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PixTransferRequest {
    @NotBlank
    private String fromWalletId;
    @NotBlank
    private String toPixKey;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}