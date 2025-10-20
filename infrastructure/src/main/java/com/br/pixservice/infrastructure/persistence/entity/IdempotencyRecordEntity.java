package com.br.pixservice.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "idempotency_records")
public class IdempotencyRecordEntity {

    @Id
    private String id;

    @Indexed
    private String scope;

    @Indexed
    private String key;

    @Field("result")
    private String result;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;
}
