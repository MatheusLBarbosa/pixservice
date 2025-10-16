package com.br.pixservice.infrastructure.service;

import com.br.pixservice.domain.model.LedgerEntry;
import com.br.pixservice.domain.model.PixTransfer;
import com.br.pixservice.domain.repository.PixTransferRepository;
import com.br.pixservice.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PixWebhookService {

    private final PixTransferRepository pixTransferRepository;
    private final WalletRepository walletRepository;

    //buscar transferencia
    //verificar status
    //se não ta pending,

    public void execute(PixTransfer receivedPix){

        PixTransfer existingTransfer = pixTransferRepository.findByEndToEndId(receivedPix.getEndToEndId())
                .orElseThrow(() -> new IllegalArgumentException("Pix does not exists"));



//        // Débito
//        BigDecimal newBalance = sender.getBalance().subtract(amount);
//        walletRepository.updateBalance(fromWalletId, newBalance, sender.getVersion());
//
//        // Ledger
//        LedgerEntry entry = LedgerEntry.createDebit(fromWalletId, endToEndId, amount);
//        ledgerRepository.save(entry);
    }
}
