package com.br.pixservice.usecase.wallet;

import com.br.pixservice.domain.model.LedgerEntry;
import com.br.pixservice.domain.model.Wallet;
import com.br.pixservice.domain.repository.LedgerRepository;
import com.br.pixservice.domain.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DepositUseCaseTest {

    private WalletRepository walletRepository;
    private LedgerRepository ledgerRepository;
    private DepositUseCase useCase;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        ledgerRepository = mock(LedgerRepository.class);
        useCase = new DepositUseCase(walletRepository, ledgerRepository);
    }

    @Test
    @DisplayName("Should deposit and persist ledger entry")
    void shouldDeposit() {
        String walletId = "w1";
        BigDecimal initial = new BigDecimal("100.00");
        BigDecimal amount = new BigDecimal("50.00");
        Wallet wallet = Wallet.builder().id(walletId).balance(initial).version(1L).build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        Wallet result = useCase.execute(walletId, amount, "source");

        BigDecimal expected = initial.add(amount);
        assertEquals(expected, result.getBalance());

        verify(walletRepository).updateBalance(walletId, expected, 1L);
        verify(ledgerRepository).save(any(LedgerEntry.class));
    }

    @Test
    @DisplayName("Should throw when wallet is not found")
    void shouldThrowWhenWalletNotFound() {
        when(walletRepository.findById("missing")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("missing", BigDecimal.ONE, "src"));

        assertEquals("Wallet not exists", ex.getMessage());
        verify(walletRepository, never()).updateBalance(anyString(), any(), anyLong());
        verify(ledgerRepository, never()).save(any());
    }
}
