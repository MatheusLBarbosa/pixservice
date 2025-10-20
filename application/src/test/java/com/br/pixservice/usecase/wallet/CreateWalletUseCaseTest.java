package com.br.pixservice.usecase.wallet;

import com.br.pixservice.domain.model.Wallet;
import com.br.pixservice.domain.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateWalletUseCaseTest {

    private WalletRepository repository;
    private CreateWalletUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(WalletRepository.class);
        useCase = new CreateWalletUseCase(repository);
    }

    @Test
    @DisplayName("Should create wallet when document is valid and not exists")
    void shouldCreateWalletWhenValidAndNotExists() {
        String owner = "Maria";
        String inputDocument = "123.456.789-09"; // 11 digits after normalization
        String normalized = "12345678909";

        when(repository.findByDocumentNumber(normalized)).thenReturn(Optional.empty());

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        when(repository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet toSave = invocation.getArgument(0);
            return Wallet.builder()
                    .id("generated-id")
                    .ownerName(toSave.getOwnerName())
                    .document(toSave.getDocument())
                    .balance(toSave.getBalance())
                    .version(toSave.getVersion())
                    .build();
        });

        Wallet created = useCase.execute(owner, inputDocument);

        verify(repository).findByDocumentNumber(normalized);
        verify(repository).save(walletCaptor.capture());

        Wallet saved = walletCaptor.getValue();
        assertEquals(owner, saved.getOwnerName());
        assertEquals(normalized, saved.getDocument());
        assertEquals(BigDecimal.ZERO, saved.getBalance());

        assertEquals(owner, created.getOwnerName());
        assertEquals(normalized, created.getDocument());
        assertEquals(BigDecimal.ZERO, created.getBalance());
    }

    @Test
    @DisplayName("Should throw when wallet already exists for document")
    void shouldThrowWhenDuplicateWallet() {
        String owner = "João";
        String inputDocument = "12.345.678/0001-90"; // 14 digits after normalization
        String normalized = "12345678000190";

        when(repository.findByDocumentNumber(normalized))
                .thenReturn(Optional.of(Wallet.builder().id("w1").document(normalized).build()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(owner, inputDocument));

        assertEquals("Wallet already exists", ex.getMessage());
        verify(repository).findByDocumentNumber(normalized);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when document has invalid length")
    void shouldThrowWhenInvalidDocument() {
        String owner = "Ana";
        String inputDocument = "1234567890"; // 10 digits (invalid for CPF and CNPJ)

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(owner, inputDocument));

        assertTrue(ex.getMessage().startsWith("Document "));
        assertTrue(ex.getMessage().endsWith(" is invalid"));
        verify(repository, never()).findByDocumentNumber(any());
        verify(repository, never()).save(any());
    }
}


