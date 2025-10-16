package com.br.pixservice.domain.util;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class DocumentUtils {

    public static String normalize(String input) {
        if (input == null) return "";
        return input.replaceAll("\\D+", "");
    }

    public static boolean hasValidCpfLength(String cpf) {
        if (cpf == null) return false;
        String normalized = cpf.trim().replaceAll("[^0-9]", "");
        return normalized.matches("\\d{11}");
    }

    public static boolean hasValidCnpjLength(String cnpj) {
        if (cnpj == null) return false;
        String normalized = cnpj.trim().replaceAll("[^0-9]", "");
        return normalized.matches("\\d{14}");
    }
}
