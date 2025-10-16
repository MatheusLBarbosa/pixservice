package com.br.pixservice.usecase.pix;

import com.br.pixservice.domain.PixRecordService;
import com.br.pixservice.domain.model.*;
import com.br.pixservice.domain.repository.LedgerRepository;
import com.br.pixservice.domain.repository.PixKeyRepository;
import com.br.pixservice.domain.repository.PixTransferRepository;
import com.br.pixservice.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class PixTransferUseCase {
    private final WalletRepository walletRepository;
    private final PixKeyRepository pixKeyRepository;
    private final PixTransferRepository pixTransferRepository;
    private final LedgerRepository ledgerRepository;
    private final PixRecordService service;

    public PixTransfer execute(String idempotencyKey, String fromWalletId, String toPixKey, BigDecimal amount) {
        return service.execute("PIX_TRANSFER", idempotencyKey, () -> {
            Wallet sender = walletRepository.findById(fromWalletId)
                    .orElseThrow(() -> new IllegalArgumentException("Carteira de origem não encontrada"));

            PixKey receiverKey = pixKeyRepository.findByKeyValue(toPixKey)
                    .orElseThrow(() -> new IllegalArgumentException("Chave Pix destino não encontrada"));

            if (sender.getBalance().compareTo(amount) < 0)
                throw new IllegalStateException("Saldo insuficiente para transferência Pix");

            // Gera endToEndId
            String endToEndId = UUID.randomUUID().toString();

            // Cria transferência
            PixTransfer transfer = new PixTransfer(null, endToEndId, fromWalletId, toPixKey,
                    amount, PixStatus.PENDING, Instant.now(), Instant.now());
            pixTransferRepository.save(transfer);

            // Débito
            BigDecimal newBalance = sender.getBalance().subtract(amount);
            walletRepository.updateBalance(fromWalletId, newBalance, sender.getVersion());

            // Ledger
            LedgerEntry entry = LedgerEntry.createDebit(fromWalletId, endToEndId, amount);
            ledgerRepository.save(entry);

            sender.setBalance(newBalance);

            pixSimulatorService(endToEndId);
            return transfer;
        }, PixTransfer.class);
    }

    private void pixSimulatorService(String endToEndId) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(2000,5000));
            boolean isSuccess = Math.random() < 0.9;

            PixWebhook webhookPayload = PixWebhook.builder().endToEndId(endToEndId.toString())
                    .eventId(UUID.randomUUID().toString())
                    .occurredAt(Instant.now())
                    .build();

            if (isSuccess) {
                webhookPayload.setStatus(PixStatus.CONFIRMED);
            } else {
                webhookPayload.setStatus(PixStatus.REJECTED);
            }

            //Resttemplate para chamar endpoint webhook

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
