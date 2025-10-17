package com.br.pixservice.usecase.wallet;

import com.br.pixservice.domain.model.Wallet;
import com.br.pixservice.domain.repository.LedgerRepository;
import com.br.pixservice.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class BalanceUseCase {

    private final WalletRepository walletRepository;
    private final LedgerRepository ledgerRepository;

    public String execute(String walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not exists"));

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nf.format(wallet.getBalance());
    }
}
