package com.br.pixservice.domain.messaging;

public interface RabbitPublisher {
    void publish(String topic, String key, String payload);
}
