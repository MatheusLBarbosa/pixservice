package com.br.pixservice.infrastructure.rest.controller;

import com.br.pixservice.domain.model.PixKey;
import com.br.pixservice.domain.model.Wallet;
import com.br.pixservice.infrastructure.rest.request.CreateWalletRequest;
import com.br.pixservice.infrastructure.rest.request.DepositRequest;
import com.br.pixservice.infrastructure.rest.request.RegisterPixKeyRequest;
import com.br.pixservice.infrastructure.rest.request.WithdrawRequest;
import com.br.pixservice.infrastructure.rest.response.BalanceResponse;
import com.br.pixservice.infrastructure.rest.response.PixKeyResponse;
import com.br.pixservice.infrastructure.rest.response.WalletResponse;
import com.br.pixservice.usecase.pix.RegisterPixKeyUseCase;
import com.br.pixservice.usecase.wallet.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Slf4j
@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final CreateWalletUseCase createWalletUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final RegisterPixKeyUseCase registerPixKeyUseCase;
    private final BalanceUseCase balanceUseCase;
    private final HistoricalBalanceUseCase historicalBalanceUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse createWallet(@Valid @RequestBody CreateWalletRequest request) {
        Wallet wallet = createWalletUseCase.execute(request.getOwnerName(), request.getDocumentNumber());
        log.info("status=createdWallet, owner={}, document={}", wallet.getOwnerName(), wallet.getDocument());
        return WalletResponse.builder()
                .id(wallet.getId())
                .ownerName(wallet.getOwnerName())
                .documentNumber(wallet.getDocument())
                .balance(wallet.getBalance())
                .build();
    }

    @PostMapping("/{id}/pix-keys")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public PixKeyResponse registerPixKey(@PathVariable String id, @Valid @RequestBody RegisterPixKeyRequest request) {
        PixKey registeredKey = registerPixKeyUseCase.execute(id, request.getKeyType(), request.getKeyValue());

        return PixKeyResponse.builder()
                .walletId(registeredKey.getWalletId())
                .type(registeredKey.getType())
                .key(registeredKey.getKey())
                .build();
    }

    @GetMapping("/{id}/balance")
    public BalanceResponse getBalance(@PathVariable String id,
                                      @RequestParam(value = "at", required = false) String at) {
        OffsetDateTime timestamp = at != null ? OffsetDateTime.parse(at) : OffsetDateTime.now();

        if (at != null) {
            String balance = historicalBalanceUseCase.execute(id, timestamp);

            return BalanceResponse.builder()
                    .balance(balance)
                    .asOf(timestamp)
                    .build();
        }

        String balance = balanceUseCase.execute(id);

        return BalanceResponse.builder()
                .balance(balance)
                .asOf(OffsetDateTime.now())
                .build();
    }

    @PostMapping("/{id}/deposit")
    public BalanceResponse deposit(@PathVariable String id, @Valid @RequestBody DepositRequest request) {
        Wallet wallet = depositUseCase.execute(id, request.getAmount(), request.getSource());
        return BalanceResponse.builder()
                .balance(wallet.getBalance().toString())
                .asOf(OffsetDateTime.now())
                .build();
    }

    @PostMapping("/{id}/withdraw")
    public BalanceResponse withdraw(@PathVariable String id, @Valid @RequestBody WithdrawRequest request) {
        Wallet wallet = withdrawUseCase.execute(id, request.getAmount(), request.getReason());
        return BalanceResponse.builder()
                .balance(wallet.getBalance().toString())
                .asOf(OffsetDateTime.now())
                .build();
    }
}