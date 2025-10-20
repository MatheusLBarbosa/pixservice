package com.br.pixservice.domain.service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface PixRecordService {
    <T> T execute(String scope, String key, Supplier<T> action, Class<T> resultType);
    <T> void saveRecord(String scope, String key, T result);
    CompletableFuture<Void> saveRecordAsync(String scope, String key, Object result);
}
