package com.br.pixservice.infrastructure.rest.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterPixKeyRequest {
    @NotBlank
    private String keyType;
    @NotBlank
    private String keyValue;
}