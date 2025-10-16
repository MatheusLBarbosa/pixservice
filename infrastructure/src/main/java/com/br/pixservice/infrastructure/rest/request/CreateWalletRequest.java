package com.br.pixservice.infrastructure.rest.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateWalletRequest {
    @NotBlank
    private String ownerName;
    @NotBlank
    private String documentNumber;
}
