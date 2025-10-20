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

class WithdrawUseCaseTest {

    private WalletRepository walletRepository;
    private LedgerRepository ledgerRepository;
    private WithdrawUseCase useCase;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        ledgerRepository = mock(LedgerRepository.class);
        useCase = new WithdrawUseCase(walletRepository, ledgerRepository);
    }

    @Test
    @DisplayName("Should withdraw and persist ledger entry")
    void shouldWithdraw() {
        String walletId = "w1";
        BigDecimal initial = new BigDecimal("100.00");
        BigDecimal amount = new BigDecimal("40.00");
        Wallet wallet = Wallet.builder().id(walletId).balance(initial).version(2L).build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        Wallet result = useCase.execute(walletId, amount, "reason");

        BigDecimal expected = initial.subtract(amount);
        assertEquals(expected, result.getBalance());

        verify(walletRepository).updateBalance(walletId, expected, 2L);
        verify(ledgerRepository).save(any(LedgerEntry.class));
    }

    @Test
    @DisplayName("Should throw when wallet not found")
    void shouldThrowWhenWalletNotFound() {
        when(walletRepository.findById("missing")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("missing", BigDecimal.ONE, "reason"));

        assertEquals("Wallet not found", ex.getMessage());
        verify(walletRepository, never()).updateBalance(anyString(), any(), anyLong());
        verify(ledgerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when insufficient funds")
    void shouldThrowWhenInsufficientFunds() {
        String walletId = "w1";
        Wallet wallet = Wallet.builder().id(walletId).balance(new BigDecimal("10.00")).version(1L).build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> useCase.execute(walletId, new BigDecimal("20.00"), "reason"));

        assertEquals("Saldo insuficiente", ex.getMessage());
        verify(walletRepository, never()).updateBalance(anyString(), any(), anyLong());
        verify(ledgerRepository, never()).save(any());
    }
}


