package com.br.pixservice.infrastructure.rest.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class WebhookRequest {
    @NotBlank
    private String endToEndId;
    @NotBlank
    private String eventId;
    @NotBlank
    private String eventType;
    private OffsetDateTime occurredAt;
}