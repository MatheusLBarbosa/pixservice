package com.br.pixservice.usecase.pix;

import com.br.pixservice.domain.messaging.RabbitPublisher;
import com.br.pixservice.domain.model.PixKey;
import com.br.pixservice.domain.model.PixStatus;
import com.br.pixservice.domain.model.PixTransfer;
import com.br.pixservice.domain.model.Wallet;
import com.br.pixservice.domain.repository.PixKeyRepository;
import com.br.pixservice.domain.repository.PixTransferRepository;
import com.br.pixservice.domain.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PixTransferUseCaseTest {

    @Test
    @DisplayName("Should create transfer and publish event when balance is sufficient")
    void shouldCreateTransferAndPublish() throws Exception {
        // Given
        WalletRepository walletRepository = mock(WalletRepository.class);
        PixKeyRepository pixKeyRepository = mock(PixKeyRepository.class);
        PixTransferRepository pixTransferRepository = mock(PixTransferRepository.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        RabbitPublisher publisher = mock(RabbitPublisher.class);

        PixTransferUseCase useCase = new PixTransferUseCase(walletRepository, pixKeyRepository, pixTransferRepository, objectMapper, publisher);

        // Inject @Value fields via reflection
        var pixExchangeField = PixTransferUseCase.class.getDeclaredField("pixExchange");
        pixExchangeField.setAccessible(true);
        pixExchangeField.set(useCase, "pixservice-exchange");

        var routingKeyField = PixTransferUseCase.class.getDeclaredField("routingKey");
        routingKeyField.setAccessible(true);
        routingKeyField.set(useCase, "pixservice.routing");

        String fromWalletId = "w-1";
        String targetPix = "cpf-123";
        BigDecimal amount = new BigDecimal("50.00");
        Wallet sender = Wallet.builder().id(fromWalletId).balance(new BigDecimal("100.00")).version(1L).build();
        PixKey key = PixKey.builder().walletId("w-2").key(targetPix).build();

        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.of(sender));
        when(pixKeyRepository.findByKey(targetPix)).thenReturn(Optional.of(key));
        when(pixTransferRepository.save(any(PixTransfer.class))).thenAnswer(inv -> {
            PixTransfer t = inv.getArgument(0);
            t.setId("t-1");
            return t;
        });
        when(objectMapper.writeValueAsString(any())).thenReturn("{}\n");

        // When
        PixTransfer result = useCase.execute("idem-1", fromWalletId, targetPix, amount);

        // Then
        assertNotNull(result);
        assertEquals(PixStatus.PENDING, result.getStatus());
        verify(pixTransferRepository).save(any(PixTransfer.class));
        verify(publisher).publish(eq("pixservice-exchange"), eq("pixservice.routing"), anyString());
    }

    @Test
    @DisplayName("Should fail when balance is insufficient")
    void shouldFailWhenInsufficientBalance() {
        // Given
        WalletRepository walletRepository = mock(WalletRepository.class);
        PixKeyRepository pixKeyRepository = mock(PixKeyRepository.class);
        PixTransferRepository pixTransferRepository = mock(PixTransferRepository.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        RabbitPublisher publisher = mock(RabbitPublisher.class);

        PixTransferUseCase useCase = new PixTransferUseCase(walletRepository, pixKeyRepository, pixTransferRepository, objectMapper, publisher);

        // Inject @Value fields via reflection
        try {
            var pixExchangeField = PixTransferUseCase.class.getDeclaredField("pixExchange");
            pixExchangeField.setAccessible(true);
            pixExchangeField.set(useCase, "pixservice-exchange");

            var routingKeyField = PixTransferUseCase.class.getDeclaredField("routingKey");
            routingKeyField.setAccessible(true);
            routingKeyField.set(useCase, "pixservice.routing");
        } catch (Exception ignored) {}

        String fromWalletId = "w-1";
        String targetPix = "cpf-123";
        BigDecimal amount = new BigDecimal("150.00");
        Wallet sender = Wallet.builder().id(fromWalletId).balance(new BigDecimal("100.00")).version(1L).build();
        PixKey key = PixKey.builder().walletId("w-2").key(targetPix).build();

        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.of(sender));
        when(pixKeyRepository.findByKey(targetPix)).thenReturn(Optional.of(key));

        // Then
        assertThrows(IllegalStateException.class, () ->
                useCase.execute("idem-1", fromWalletId, targetPix, amount)
        );

        verify(pixTransferRepository, never()).save(any());
        verify(publisher, never()).publish(anyString(), anyString(), anyString());
    }
}


