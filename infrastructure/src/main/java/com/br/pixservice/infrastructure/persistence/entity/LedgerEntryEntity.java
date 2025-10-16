package com.br.pixservice.infrastructure.persistence.entity;

import com.br.pixservice.domain.model.LedgerEntryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Document(collection = "ledger_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryEntity {

    @Id
    private String id;

    @Field("wallet_id")
    @Indexed
    private String walletId;

    @Field("end_to_end_id")
    @Indexed
    private UUID endToEndId;

    private BigDecimal amount;

    private LedgerEntryType type;

    @Field("created_at")
    private Instant createdAt;

}
