package com.br.pixservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PixWebhook {
    private String endToEndId;
    private String eventId;
    private PixStatus status;
    private Instant occurredAt;
}
