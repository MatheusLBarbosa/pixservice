package com.br.pixservice.infrastructure.persistence.repository;

import com.br.pixservice.domain.model.PixTransfer;
import com.br.pixservice.domain.repository.PixTransferRepository;
import com.br.pixservice.infrastructure.persistence.dao.PixTransferDAO;
import com.br.pixservice.infrastructure.persistence.entity.PixTransferEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PixTranssferRepositoryImpl implements PixTransferRepository {

    private final PixTransferMongoRepository repository;
    private MongoTemplate template;

    @Override
    public PixTransfer save(PixTransfer transfer) {
        return PixTransferDAO.toDomain(repository.save(PixTransferDAO.toEntity(transfer)));
    }

    @Override
    public Optional<PixTransfer> findByEndToEndId(String endToEndId) {
        return repository.findByEndToEndId(endToEndId).map(PixTransferDAO::toDomain);
    }

    @Override
    public void updateStatus(String endToEndId, String status) {
        Query query = new Query(Criteria.where("end_to_end_id").is(endToEndId));
        Update update = new Update().set("status", status);

        template.updateFirst(query, update, PixTransferEntity.class);
    }
}
