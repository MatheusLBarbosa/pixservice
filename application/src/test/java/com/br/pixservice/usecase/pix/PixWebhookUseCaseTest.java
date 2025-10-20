package com.br.pixservice.usecase.pix;

import com.br.pixservice.domain.model.*;
import com.br.pixservice.domain.repository.LedgerRepository;
import com.br.pixservice.domain.repository.PixTransferRepository;
import com.br.pixservice.domain.repository.WalletRepository;
import com.br.pixservice.domain.service.PixRecordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PixWebhookUseCaseTest {

    @Test
    @DisplayName("Should no-op when eventType equals current status")
    void shouldNoOpOnSameStatus() {
        PixTransferRepository pixTransferRepository = mock(PixTransferRepository.class);
        WalletRepository walletRepository = mock(WalletRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        PixRecordService pixRecordService = mock(PixRecordService.class);

        PixWebhookUseCase useCase = new PixWebhookUseCase(pixTransferRepository, walletRepository, null, ledgerRepository, pixRecordService);

        PixTransfer transfer = PixTransfer.builder()
                .endToEndId("e2e-1")
                .status(PixStatus.PENDING)
                .build();

        when(pixTransferRepository.findByEndToEndId("e2e-1")).thenReturn(Optional.of(transfer));
        when(pixRecordService.execute(anyString(), anyString(), any(), eq(Void.class)))
                .thenAnswer(inv -> {
                    ((java.util.function.Supplier<?>) inv.getArgument(2)).get();
                    return null;
                });

        useCase.execute("evt-1", "e2e-1", "PENDING", OffsetDateTime.now());

        verify(pixTransferRepository, never()).updateStatus(anyString(), anyString());
        verifyNoInteractions(walletRepository, ledgerRepository);
    }

    @Test
    @DisplayName("Should process CONFIRMED and update balances and ledger")
    void shouldProcessConfirmed() {
        PixTransferRepository pixTransferRepository = mock(PixTransferRepository.class);
        WalletRepository walletRepository = mock(WalletRepository.class);
        LedgerRepository ledgerRepository = mock(LedgerRepository.class);
        PixRecordService pixRecordService = mock(PixRecordService.class);

        PixWebhookUseCase useCase = new PixWebhookUseCase(pixTransferRepository, walletRepository, null, ledgerRepository, pixRecordService);

        PixTransfer transfer = PixTransfer.builder()
                .endToEndId("e2e-2")
                .sourceWalletId("w-1")
                .targetWalletId("w-2")
                .amount(new BigDecimal("10.00"))
                .status(PixStatus.PENDING)
                .build();

        when(pixTransferRepository.findByEndToEndId("e2e-2")).thenReturn(Optional.of(transfer));
        when(walletRepository.findById("w-1")).thenReturn(Optional.of(Wallet.builder().id("w-1").balance(new BigDecimal("20.00")).version(1L).build()));
        when(walletRepository.findById("w-2")).thenReturn(Optional.of(Wallet.builder().id("w-2").balance(new BigDecimal("5.00")).version(1L).build()));
        when(walletRepository.updateBalance(eq("w-1"), any(), anyLong())).thenReturn(true);
        when(walletRepository.updateBalance(eq("w-2"), any(), anyLong())).thenReturn(true);
        when(pixRecordService.execute(anyString(), anyString(), any(), eq(Void.class)))
                .thenAnswer(inv -> {
                    ((java.util.function.Supplier<?>) inv.getArgument(2)).get();
                    return null;
                });

        useCase.execute("evt-2", "e2e-2", "CONFIRMED", OffsetDateTime.now());

        verify(walletRepository, times(2)).updateBalance(anyString(), any(), anyLong());
        verify(ledgerRepository, times(2)).save(any(LedgerEntry.class));
        verify(pixTransferRepository).updateStatus("e2e-2", "CONFIRMED");
    }
}


