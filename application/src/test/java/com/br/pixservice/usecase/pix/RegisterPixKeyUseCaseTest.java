package com.br.pixservice.usecase.pix;

import com.br.pixservice.domain.model.PixKey;
import com.br.pixservice.domain.repository.PixKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RegisterPixKeyUseCaseTest {

    private PixKeyRepository repository;
    private RegisterPixKeyUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(PixKeyRepository.class);
        useCase = new RegisterPixKeyUseCase(repository);
    }

    @Test
    @DisplayName("Should register a new Pix key when not existing")
    void shouldRegisterPixKey() {
        String walletId = "w1";
        String type = "CPF";
        String key = "12345678901";

        when(repository.existsByKeyValue(key)).thenReturn(false);
        when(repository.save(any(PixKey.class))).thenAnswer(invocation -> {
            PixKey arg = invocation.getArgument(0);
            return PixKey.builder()
                    .id("k1")
                    .key(arg.getKey())
                    .type(arg.getType())
                    .walletId(arg.getWalletId())
                    .build();
        });

        PixKey created = useCase.execute(walletId, type, key);

        assertEquals("k1", created.getId());
        assertEquals(key, created.getKey());
        assertEquals(type, created.getType());
        assertEquals(walletId, created.getWalletId());

        verify(repository).existsByKeyValue(key);
        verify(repository).save(any(PixKey.class));
    }

    @Test
    @DisplayName("Should throw when Pix key already exists")
    void shouldThrowWhenKeyAlreadyExists() {
        String walletId = "w1";
        String type = "CPF";
        String key = "12345678901";

        when(repository.existsByKeyValue(key)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(walletId, type, key));

        assertEquals("Pix Key is already registered to this wallet", ex.getMessage());
        verify(repository).existsByKeyValue(key);
        verify(repository, never()).save(any());
    }
}
