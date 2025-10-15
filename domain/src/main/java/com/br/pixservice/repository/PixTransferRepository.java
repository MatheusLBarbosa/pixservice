package com.br.pixservice.repository;

import com.br.pixservice.model.PixTransfer;

import java.util.Optional;

public interface PixTransferRepository {
    PixTransfer save(PixTransfer transfer);

    Optional<PixTransfer> findByEndToEndId(String endToEndId);

    void updateStatus(String endToEndId, String status);
}
