package com.br.pixservice.infrastructure.rabbitmq;

import com.br.pixservice.domain.messaging.RabbitPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitPublisherImpl implements RabbitPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publish(String topic, String key, String payload) {
        try {
            rabbitTemplate.convertAndSend(topic, key, payload);
            log.info("Event published to RabbitMQ: topic={}, key={}", topic, key);
        } catch (Exception e) {
            log.error("Failed to publish event to RabbitMQ", e);
            throw new RuntimeException("RabbitMQ publish failed", e);
        }
    }
}
