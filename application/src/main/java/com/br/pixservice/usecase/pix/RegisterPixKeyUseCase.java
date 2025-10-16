package com.br.pixservice.usecase.pix;

import com.br.pixservice.domain.model.PixKey;
import com.br.pixservice.domain.repository.PixKeyRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterPixKeyUseCase {

    private final PixKeyRepository repository;

    PixKey execute(String walletId, String keyType, String keyValue) {
        if(repository.existsByKeyValue(keyValue)){
            throw new IllegalArgumentException("Pix Key is already registered to this wallet");
        }

        PixKey pixKey = PixKey.builder().key(keyValue).type(keyType).walletId(walletId).build();

        return repository.save(pixKey);
    }

}
