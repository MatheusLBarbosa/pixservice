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
public class DepositUseCase {

    private final WalletRepository walletRepository;
    private final LedgerRepository ledgerRepository;

    public Wallet execute(String walletId, BigDecimal amount, String source) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Carteira não encontrada"));

        BigDecimal newBalance = wallet.getBalance().add(amount);

        walletRepository.updateBalance(walletId, newBalance, wallet.getVersion());

        LedgerEntry entry = LedgerEntry.createCredit(walletId, UUID.randomUUID().toString(), amount);
        ledgerRepository.save(entry);

        wallet.setBalance(newBalance);
        return wallet;
    }

}
