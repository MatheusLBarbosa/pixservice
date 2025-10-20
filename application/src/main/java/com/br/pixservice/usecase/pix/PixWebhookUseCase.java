package com.br.pixservice.usecase.pix;

import com.br.pixservice.domain.model.*;
import com.br.pixservice.domain.repository.LedgerRepository;
import com.br.pixservice.domain.repository.PixKeyRepository;
import com.br.pixservice.domain.repository.PixTransferRepository;
import com.br.pixservice.domain.repository.WalletRepository;
import com.br.pixservice.domain.service.PixRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ConcurrentModificationException;


@Slf4j
@Component
@RequiredArgsConstructor
public class PixWebhookUseCase {

    private final PixTransferRepository pixTransferRepository;
    private final WalletRepository walletRepository;
    private final PixKeyRepository pixKeyRepository;
    private final LedgerRepository ledgerRepository;
    private final PixRecordService service;

    public void execute(String eventId, String endToEndId, String eventType, OffsetDateTime occurredAt) {
        service.execute("PIX_WEBHOOK", eventId, () -> {
            processWebhook(endToEndId, eventType, occurredAt);
            return null;
        }, Void.class);
    }

    private void processWebhook(String endToEndId, String eventType, OffsetDateTime occurredAt) {
        log.info("Processing webhook - endToEndId: {}, eventType: {}", endToEndId, eventType);

        PixTransfer transfer = pixTransferRepository.findByEndToEndId(endToEndId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transfer not found for endToEndId: " + endToEndId));

        PixStatus currentStatus = transfer.getStatus();
        PixStatus newStatus = PixStatus.valueOf(eventType);

        log.info("Current status: {}, Requested status: {}", currentStatus, newStatus);

        if (currentStatus == newStatus) {
            log.info("No-op state update - transfer already in {}: {}", currentStatus, endToEndId);
            return;
        }

        if (!isValidTransition(currentStatus, newStatus)) {
            log.warn("Invalid state transition - from {} to {} for endToEndId: {}",
                    currentStatus, newStatus, endToEndId);
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s", currentStatus, newStatus));
        }

        switch (newStatus) {
            case CONFIRMED -> processConfirmation(transfer, occurredAt);
            case REJECTED -> processRejection(transfer, occurredAt);
            case PENDING -> log.info("Transfer already in PENDING state: {}", endToEndId);
        }


        pixTransferRepository.updateStatus(endToEndId, newStatus.name());
        log.info("Transfer status updated - endToEndId: {}, newStatus: {}", endToEndId, newStatus);
    }

    private boolean isValidTransition(PixStatus from, PixStatus to) {
        if (from == to) {
            return true;
        }
        return switch (from) {
            case PENDING -> to == PixStatus.CONFIRMED || to == PixStatus.REJECTED;
            case CONFIRMED, REJECTED -> false;
        };
    }

    private void processConfirmation(PixTransfer transfer, OffsetDateTime occurredAt) {
        log.info("Processing CONFIRMED status for transfer: {}", transfer.getEndToEndId());

        // 1. Buscar carteira origem
        Wallet sender = walletRepository.findById(transfer.getSourceWalletId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Source wallet not found: " + transfer.getSourceWalletId()));

        Wallet receiver = walletRepository.findById(transfer.getTargetWalletId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Receiver wallet not found: " + transfer.getTargetWalletId()));
//        // 2. Buscar chave PIX destino
//        PixKey receiverKey = pixKeyRepository.findByKey(transfer.getTargetPixKey())
//                .orElseThrow(() -> new IllegalArgumentException(
//                        "Destination Pix key not found: " + transfer.getTargetPixKey()));

        // 3. Validar saldo
        if (sender.getBalance().compareTo(transfer.getAmount()) < 0) {
            log.error("Insufficient balance - wallet: {}, balance: {}, required: {}",
                    sender.getId(), sender.getBalance(), transfer.getAmount());
            throw new IllegalStateException("Insufficient balance for transfer");
        }

        // 4. DÉBITO - Carteira origem
        BigDecimal newSenderBalance = sender.getBalance().subtract(transfer.getAmount());
        boolean senderUpdated = walletRepository.updateBalance(
                sender.getId(),
                newSenderBalance,
                sender.getVersion()
        );

        if (!senderUpdated) {
            throw new ConcurrentModificationException(
                    "Sender wallet version mismatch - concurrent update detected");
        }

        log.info("Debited sender wallet - walletId: {}, oldBalance: {}, newBalance: {}, amount: {}",
                sender.getId(), sender.getBalance(), newSenderBalance, transfer.getAmount());

        // 5. Registrar débito no ledger
        LedgerEntry debitEntry = LedgerEntry.createDebit(
                sender.getId(),
                transfer.getEndToEndId(),
                transfer.getAmount()
        );
        ledgerRepository.save(debitEntry);

        // 6. CRÉDITO - Carteira destino


        BigDecimal newReceiverBalance = receiver.getBalance().add(transfer.getAmount());
        boolean receiverUpdated = walletRepository.updateBalance(
                receiver.getId(),
                newReceiverBalance,
                receiver.getVersion()
        );

        if (!receiverUpdated) {
            throw new ConcurrentModificationException(
                    "Receiver wallet version mismatch - concurrent update detected");
        }

        log.info("Credited receiver wallet - walletId: {}, oldBalance: {}, newBalance: {}, amount: {}",
                receiver.getId(), receiver.getBalance(), newReceiverBalance, transfer.getAmount());

        LedgerEntry creditEntry = LedgerEntry.createCredit(
                receiver.getId(),
                transfer.getEndToEndId(),
                transfer.getAmount()
        );
        ledgerRepository.save(creditEntry);

        log.info("Transfer CONFIRMED successfully - endToEndId: {}, amount: {}",
                transfer.getEndToEndId(), transfer.getAmount());
    }

    private void processRejection(PixTransfer transfer, OffsetDateTime occurredAt) {
        log.info("Processing REJECTED status for transfer: {}", transfer.getEndToEndId());

        var ledgerEntries = ledgerRepository.findByEndToEndId(transfer.getEndToEndId());

        if (!ledgerEntries.isEmpty()) {
            log.warn("Transfer has ledger entries - processing reversal: {}", transfer.getEndToEndId());
            processReversal(transfer, ledgerEntries);
        } else {
            log.info("Transfer rejected before any financial movement: {}", transfer.getEndToEndId());
        }

        log.info("Transfer REJECTED - endToEndId: {}", transfer.getEndToEndId());
    }

    private void processReversal(PixTransfer transfer, java.util.List<LedgerEntry> existingEntries) {
        log.info("Processing reversal for transfer: {}", transfer.getEndToEndId());

        LedgerEntry debitEntry = existingEntries.stream()
                .filter(LedgerEntry::isDebit)
                .findFirst()
                .orElse(null);

        if (debitEntry == null) {
            log.info("No debit entry found - no reversal needed");
            return;
        }

        Wallet sender = walletRepository.findById(transfer.getSourceWalletId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Source wallet not found for reversal: " + transfer.getSourceWalletId()));

        BigDecimal reversalAmount = transfer.getAmount();
        BigDecimal newBalance = sender.getBalance().add(reversalAmount);

        boolean updated = walletRepository.updateBalance(
                sender.getId(),
                newBalance,
                sender.getVersion()
        );

        if (!updated) {
            throw new ConcurrentModificationException(
                    "Failed to update wallet during reversal - version mismatch");
        }

        LedgerEntry reversalEntry = LedgerEntry.createCredit(
                sender.getId(),
                transfer.getEndToEndId() + "-REVERSAL",
                reversalAmount
        );
        ledgerRepository.save(reversalEntry);

        log.info("Reversal completed - walletId: {}, amount: {}, newBalance: {}",
                sender.getId(), reversalAmount, newBalance);
    }
}
