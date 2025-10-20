package com.br.pixservice.infrastructure.persistence.repository;

import com.br.pixservice.domain.model.Wallet;
import com.br.pixservice.domain.repository.WalletRepository;
import com.br.pixservice.infrastructure.persistence.dao.WalletDAO;
import com.br.pixservice.infrastructure.persistence.entity.WalletEntity;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WalletRepositoryImpl implements WalletRepository {

    private final MongoTemplate template;
    private final WalletMongoRepository repository;

    @Override
    public Wallet save(Wallet wallet) {
        return WalletDAO.toDomain(repository.save(WalletDAO.toEntity(wallet)));
    }

    @Override
    public Optional<Wallet> findById(String walletId) {
        return repository.findById(walletId).map(WalletDAO::toDomain);
    }

    @Override
    public Optional<Wallet> findByDocumentNumber(String documentNumber) {
        return repository.findByDocument(documentNumber).map(WalletDAO::toDomain);
    }

    @Override
    public boolean updateBalance(String walletId, BigDecimal newBalance,  Long expectedVersion) {
        Query query = new Query(Criteria.where("_id").is(walletId)
                .and("version").is(expectedVersion));

        Update update = new Update()
                .set("balance", newBalance)
                .inc("version", 1);

        UpdateResult result = template.updateFirst(query, update, WalletEntity.class);

        return result.getModifiedCount() > 0;
    }

}
