package com.br.pixservice.infrastructure.persistence.repository;

import com.br.pixservice.domain.model.PixRecord;
import com.br.pixservice.domain.repository.PixRecordRepository;
import com.br.pixservice.infrastructure.persistence.dao.PixRecordDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PixRecordRepositoryImpl implements PixRecordRepository {

    private final PixRecordMongoRepository repository;

    @Override
    public Optional<PixRecord> findByScopeAndKey(String scope, String key) {
        return repository.findByScopeAndKey(scope, key).map(PixRecordDAO::toDomain);
    }

    @Override
    public PixRecord save(PixRecord record) {
        return PixRecordDAO.toDomain(repository.save(PixRecordDAO.toEntity(record)));
    }
}
