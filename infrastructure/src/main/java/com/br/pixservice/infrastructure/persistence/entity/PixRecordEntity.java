package com.br.pixservice.infrastructure.persistence.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Document(collection = "pix_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PixRecordEntity {

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
