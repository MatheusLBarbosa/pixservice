package com.br.pixservice.infrastructure.rest.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PixKeyResponse {
    private String walletId;
    private String key;
    private String type;
}
