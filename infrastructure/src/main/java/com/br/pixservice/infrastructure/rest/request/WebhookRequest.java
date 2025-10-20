package com.br.pixservice.infrastructure.rest.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRequest {
    @NotBlank
    private String endToEndId;
    @NotBlank
    private String eventId;
    @NotBlank
    private String eventType;
    private OffsetDateTime occurredAt;
}