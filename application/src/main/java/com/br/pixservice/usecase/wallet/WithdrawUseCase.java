package com.br.pixservice.usecase.wallet;

import com.br.pixservice.domain.model.LedgerEntry;
import com.br.pixservice.domain.model.Wallet;
import com.br.pixservice.domain.repository.LedgerRepository;
import com.br.pixservice.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WithdrawUseCase {
    private final WalletRepository walletRepository;
    private final LedgerRepository ledgerRepository;

    @Transactional
    public Wallet execute(String walletId, BigDecimal amount, String reason) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not exists"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        BigDecimal newBalance = wallet.getBalance().subtract(amount);

        boolean updated = walletRepository.updateBalance(walletId, newBalance, wallet.getVersion());
        if (!updated) {
            throw new ConcurrentModificationException("Wallet version mismatch");
        }

        LedgerEntry entry = LedgerEntry.createDebit(
                walletId,
                UUID.randomUUID().toString(),
                amount.negate()
        );

        ledgerRepository.save(entry);

        wallet.setBalance(newBalance);
        wallet.setVersion(wallet.getVersion() + 1);
        return wallet;
    }
}
