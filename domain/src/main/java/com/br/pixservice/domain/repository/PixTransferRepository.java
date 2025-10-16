package com.br.pixservice.domain.repository;

import com.br.pixservice.domain.model.PixTransfer;

import java.util.Optional;

public interface PixTransferRepository {
    PixTransfer save(PixTransfer transfer);

    Optional<PixTransfer> findByEndToEndId(String endToEndId);

    void updateStatus(String endToEndId, String status);
}
