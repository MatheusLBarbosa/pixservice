package com.br.pixservice.usecase.wallet;

import com.br.pixservice.domain.model.LedgerEntry;
import com.br.pixservice.domain.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class HistoricalBalanceUseCase {

    private final LedgerRepository ledgerRepository;

    public String execute(String walletId, OffsetDateTime occurredAt) {
        List<LedgerEntry> entries = ledgerRepository.findByWalletIdAndOccurredAt(walletId, occurredAt);

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        BigDecimal historicalBalance = entries.stream()
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return nf.format(historicalBalance);
    }
}
