package com.br.pixservice.usecase.pix;

import com.br.pixservice.domain.messaging.RabbitPublisher;
import com.br.pixservice.domain.model.PixKey;
import com.br.pixservice.domain.model.PixTransfer;
import com.br.pixservice.domain.model.Wallet;
import com.br.pixservice.domain.repository.PixKeyRepository;
import com.br.pixservice.domain.repository.PixTransferRepository;
import com.br.pixservice.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.br.pixservice.domain.model.PixTransfer.buildPixTransfer;

@Component
@RequiredArgsConstructor
public class PixTransferUseCase {
    private final WalletRepository walletRepository;
    private final PixKeyRepository pixKeyRepository;
    private final PixTransferRepository pixTransferRepository;
    private final ObjectMapper objectMapper;
    private final MongoTransactionManager txManager;
    private final RabbitPublisher publisher;

    @Value(value = "${app.rabbitmq.queue}")
    private String pixQueue;

    private final ConcurrentHashMap<String, Lock> walletLocks = new ConcurrentHashMap<>();

    @Transactional
    public PixTransfer execute(String idempotencyKey, String fromWalletId, String targetPixKey, BigDecimal amount) {

        Lock lock = walletLocks.computeIfAbsent(fromWalletId, k -> new ReentrantLock());

        lock.lock();
        try {
            TransactionTemplate tt = new TransactionTemplate(txManager);

            return tt.execute(status -> {
                Wallet sender = walletRepository.findById(fromWalletId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Source wallet not found: " + fromWalletId));

                PixKey receiverKey = pixKeyRepository.findByKeyValue(targetPixKey)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Destination Pix key not found: " + targetPixKey));

                if (sender.getBalance().compareTo(amount) < 0) {
                    throw new IllegalStateException(
                            "Insufficient balance - wallet: " + sender.getId() +
                                    ", balance: " + sender.getBalance() +
                                    ", required: " + amount);
                }

                String endToEndId = UUID.randomUUID().toString();

                PixTransfer transfer = buildPixTransfer(amount, endToEndId, sender, receiverKey, idempotencyKey);
                pixTransferRepository.save(transfer);

                publisher.publish(pixQueue, endToEndId, objectMapper.writeValueAsString(transfer));

                return transfer;
            });
        } finally {
            lock.unlock();

            if (lock.tryLock()) {
                try {
                    walletLocks.remove(fromWalletId, lock);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

}
