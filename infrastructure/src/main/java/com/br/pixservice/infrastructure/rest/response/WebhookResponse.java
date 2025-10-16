package com.br.pixservice.infrastructure.rest.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebhookResponse {
    private String message;
}