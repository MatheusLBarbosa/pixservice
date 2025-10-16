package com.br.pixservice.usecase.wallet;

import com.br.pixservice.domain.model.LedgerEntry;
import com.br.pixservice.domain.model.Wallet;
import com.br.pixservice.domain.repository.LedgerRepository;
import com.br.pixservice.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WithdrawUseCase {
    private final WalletRepository walletRepository;
    private final LedgerRepository ledgerRepository;

    public Wallet execute(String walletId, BigDecimal amount, String reason) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Saldo insuficiente");
        }

        BigDecimal newBalance = wallet.getBalance().subtract(amount);

        walletRepository.updateBalance(walletId, newBalance, wallet.getVersion());

        LedgerEntry entry = LedgerEntry.createDebit(walletId, UUID.randomUUID().toString(), amount.negate());

        ledgerRepository.save(entry);

        wallet.setBalance(newBalance);
        return wallet;
    }
}
