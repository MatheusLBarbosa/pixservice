package com.br.pixservice.infrastructure.rabbitmq;

import com.br.pixservice.domain.model.PixTransfer;
import com.br.pixservice.infrastructure.rest.request.WebhookRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class PixTransferListener {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Lock> transferLocks = new ConcurrentHashMap<>();

    @Value("${app.webhook.base-url:http://localhost:8080}")
    private String webhookBaseUrl;

    @Value("${app.webhook.endpoint:/pix/webhook}")
    private String webhookEndpoint;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void handlePixTransferEvent(String eventJson) {
        log.info("Received raw event: {}", eventJson);

        PixTransfer event = null;

        try {
            event = deserializeEvent(eventJson);

            if (event == null || event.getEndToEndId() == null) {
                log.error("Invalid event received: {}", eventJson);
                return;
            }

            log.info("Deserialized event - endToEndId: {}, amount: {}",
                    event.getEndToEndId(), event.getAmount());

            String lockKey = event.getEndToEndId();
            Lock lock = transferLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());

            lock.lock();
            try {
                callWebhookEndpoint(event);

                log.info("Successfully processed webhook event: {} with status: {}",
                        event.getEndToEndId(), event.getStatus());

                log.info("Successfully processed transfer event: {}", event.getEndToEndId());

            } finally {
                lock.unlock();

                if (lock.tryLock()) {
                    try {
                        transferLocks.remove(lockKey, lock);
                    } finally {
                        lock.unlock();
                    }
                }
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize event: {}", eventJson, e);
            handleDeserializationError(eventJson, e);

        } catch (Exception e) {
            log.error("Failed to process transfer event: endToEndId={}",
                    event != null ? event.getEndToEndId() : "unknown", e);
            handleProcessingError(event, e);
        }
    }

    private void callWebhookEndpoint(PixTransfer event) {
        String webhookUrl = webhookBaseUrl + webhookEndpoint;

        WebhookRequest request = WebhookRequest.builder()
                .endToEndId(event.getEndToEndId())
                .eventId(event.getId())
                .eventType(event.getStatus().name())
                .occurredAt(OffsetDateTime.now())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<WebhookRequest> httpEntity = new HttpEntity<>(request, headers);

        log.info("Calling webhook endpoint: {} with payload: {}", webhookUrl, request);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Webhook called successfully - endToEndId: {}, status: {}, response: {}",
                        event.getEndToEndId(), response.getStatusCode(), response.getBody());
            } else {
                log.warn("Webhook returned non-2xx status - endToEndId: {}, status: {}",
                        event.getEndToEndId(), response.getStatusCode());
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error calling webhook - endToEndId: {}, status: {}, body: {}",
                    event.getEndToEndId(), e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error calling webhook - endToEndId: {}", event.getEndToEndId(), e);
            throw new RuntimeException("Failed to call webhook endpoint", e);
        }
    }

    private PixTransfer deserializeEvent(String eventJson) throws JsonProcessingException {
        return objectMapper.readValue(eventJson, PixTransfer.class);
    }

    private void handleDeserializationError(String eventJson, Exception e) {
        // TODO: Enviar para DLQ ou tabela de erros
        log.error("Deserialization error - event moved to error queue: {}", eventJson);
    }

    private void handleProcessingError(PixTransfer event, Exception e) {
        // TODO: Implementar estratégia de retry ou DLQ
        log.error("Processing error - endToEndId: {}",
                event != null ? event.getEndToEndId() : "unknown");
    }
}