package com.projectnexus.ingestion.infrastructure;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ infrastructure configuration for the payload ingestion pipeline.
 *
 * <p>Topology:
 * <ul>
 *   <li>Exchange: {@code nexus.payloads} (topic)</li>
 *   <li>Queue: {@code nexus.payloads.ingestion} (durable, with DLQ routing)</li>
 *   <li>Dead-letter queue: {@code nexus.payloads.ingestion.dlq}</li>
 *   <li>Routing key: {@code payload.submitted}</li>
 * </ul>
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "nexus.payloads";
    public static final String QUEUE = "nexus.payloads.ingestion";
    public static final String DLQ = "nexus.payloads.ingestion.dlq";
    public static final String ROUTING_KEY = "payload.submitted";
    public static final String DLQ_ROUTING_KEY = "payload.dead";

    @Bean
    public TopicExchange payloadsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue ingestionQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ)
                .withArgument("x-message-ttl", 604_800_000)  // 7 days retention
                .withArgument("x-max-length", 10_000)        // Max 10k messages
                .build();
    }

    @Bean
    public Binding ingestionBinding(Queue ingestionQueue, TopicExchange payloadsExchange) {
        return BindingBuilder.bind(ingestionQueue)
                .to(payloadsExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public Binding dlqBinding(Queue deadLetterQueue, TopicExchange payloadsExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(payloadsExchange)
                .with(DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
