package com.br.pixservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PixKey {

    private String id;
    private String key;
    private String type;
    private String walletId;

    public boolean isValid() {
        return key != null && !key.trim().isEmpty() && 
               type != null && !type.trim().isEmpty() &&
               walletId != null && !walletId.trim().isEmpty();
    }

    public boolean isCpfKey() {
        return "CPF".equals(type);
    }

    public boolean isEmailKey() {
        return "EMAIL".equals(type);
    }

    public boolean isPhoneKey() {
        return "PHONE".equals(type);
    }

    public boolean isEvpKey() {
        return "EVP".equals(type);
    }
}
