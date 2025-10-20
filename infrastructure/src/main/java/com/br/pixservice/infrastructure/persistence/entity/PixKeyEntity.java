package com.br.pixservice.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "pix_keys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PixKeyEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String key;

    private PixKeyType type;

    @Field("wallet_id")
    @Indexed
    private String walletId;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;

    public enum PixKeyType {
        CPF, EMAIL, PHONE, EVP
    }
}
