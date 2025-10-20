package com.br.pixservice.rest.controller;

import com.br.pixservice.domain.model.PixTransfer;
import com.br.pixservice.infrastructure.rest.request.PixTransferRequest;
import com.br.pixservice.infrastructure.rest.request.WebhookRequest;
import com.br.pixservice.infrastructure.rest.response.PixTransferResponse;
import com.br.pixservice.infrastructure.rest.response.WebhookResponse;
import com.br.pixservice.usecase.pix.PixTransferUseCase;
import com.br.pixservice.usecase.pix.PixWebhookUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pix")
@RequiredArgsConstructor
public class PixController {

    private final PixTransferUseCase pixTransferUseCase;
    private final PixWebhookUseCase processWebhookUseCase;

    @PostMapping("/transfers")
    public PixTransferResponse transfer(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                        @Valid @RequestBody PixTransferRequest request) {

        PixTransfer transfer = pixTransferUseCase.execute(
                idempotencyKey,
                request.getFromWalletId(),
                request.getToPixKey(),
                request.getAmount()
        );

        return PixTransferResponse.builder()
                .endToEndId(String.valueOf(transfer.getEndToEndId()))
                .status(transfer.getStatus().toString())
                .build();
    }

    @PostMapping("/webhook")
    public WebhookResponse webhook(@Valid @RequestBody WebhookRequest request) {
        processWebhookUseCase.execute(
                request.getEventId(),
                request.getEndToEndId(),
                request.getEventType(),
                request.getOccurredAt()
        );

        return WebhookResponse.builder()
                .message("Webhook processado com sucesso")
                .build();
    }
}