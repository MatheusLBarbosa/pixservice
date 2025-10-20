package com.br.pixservice.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitConfig {

    @Value("${app.rabbitmq.exchange.pix}")
    private String pixExchange;
    @Value("${app.rabbitmq.queue}")
    private String QUEUE_NAME;
    @Value("${app.rabbitmq.routing}")
    private String ROUTING_KEY;

    @Bean
    public DirectExchange pixDirectExchange() {
        return new DirectExchange(pixExchange, true, false);
    }

    @Bean
    public Queue pixQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding pixBinding(Queue pixQueue, DirectExchange pixDirectExchange) {
        return BindingBuilder
                .bind(pixQueue)
                .to(pixDirectExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        createConnection(connectionFactory);
        return admin;
    }

    private void createConnection(ConnectionFactory connectionFactory) {
        try (var connection = connectionFactory.createConnection()) {
            var delegate = connection.getDelegate();
            log.info("🐇 Conexão com RabbitMQ estabelecida com sucesso!");
            log.info("➡️  Host: {} | Porta: {}", delegate.getAddress(), delegate.getPort());
        } catch (Exception e) {
            log.error("❌ Falha ao conectar ao RabbitMQ: {}", e.getMessage(), e);
        }
    }
}

