package com.br.pixservice.infrastructure.persistence.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "pix_transfers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PixTransferEntity {

    @Id
    private String id;

    @Field("end_to_end_id")
    @Indexed(unique = true)
    private String endToEndId;

    @Field("source_wallet_id")
    @Indexed
    private String sourceWalletId;

    @Field("target_wallet_id")
    @Indexed
    private String targetWalletId;

    private BigDecimal amount;

    private PixStatus status;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;

    public enum PixStatus {
        PENDING, CONFIRMED, REJECTED
    }
}
