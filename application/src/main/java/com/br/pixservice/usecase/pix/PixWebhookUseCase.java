package com.br.pixservice.usecase.pix;

import com.br.pixservice.domain.PixRecordService;
import com.br.pixservice.domain.model.PixStatus;
import com.br.pixservice.domain.model.PixTransfer;
import com.br.pixservice.domain.repository.PixTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class PixWebhookUseCase {

    private final PixTransferRepository pixTransferRepository;
    private final PixRecordService service;


    public void execute(String eventId, String endToEndId, String eventType, OffsetDateTime occurredAt) {
        service.execute("PIX_WEBHOOK", eventId, () -> {
            PixTransfer transfer = pixTransferRepository.findByEndToEndId(endToEndId)
                    .orElseThrow(() -> new IllegalArgumentException("Transferência não encontrada"));

            // Respeita a máquina de estados
            if ("CONFIRMED".equals(eventType) && !"REJECTED".equals(transfer.getStatus())) {
                pixTransferRepository.updateStatus(endToEndId, "CONFIRMED");
            } else if ("REJECTED".equals(eventType) && "PENDING".equals(transfer.getStatus())) {
                pixTransferRepository.updateStatus(endToEndId, "REJECTED");
            }

            transfer.setStatus(PixStatus.valueOf(eventType));
            transfer.setUpdatedAt(occurredAt.toInstant());
            return transfer;
        }, PixTransfer.class);
    }
}

