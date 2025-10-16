package com.br.pixservice.usecase.wallet;

import com.br.pixservice.domain.model.Wallet;
import com.br.pixservice.domain.repository.WalletRepository;
import com.br.pixservice.domain.util.DocumentUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CreateWalletUseCase {

    private final WalletRepository repository;

    public Wallet execute(String owner, String document) {
        String normalizedDocument = DocumentUtils.normalize(document);

        if (!DocumentUtils.hasValidCpfLength(normalizedDocument) && !DocumentUtils.hasValidCnpjLength(normalizedDocument)) {
            throw new IllegalArgumentException("Document " + document + " is invalid");
        }

        if (repository.findByDocumentNumber(normalizedDocument).isPresent()) {
            throw new IllegalArgumentException("Wallet already exists");
        }

        Wallet wallet = Wallet.builder().ownerName(owner).document(normalizedDocument).balance(BigDecimal.ZERO).build();

        return repository.save(wallet);
    }

}
