package com.shopsphere.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology:
 *
 * orders.exchange (topic) → notifications.queue (via order.# routing key)
 *                         → inventory.events.queue (via inventory.# routing key)
 *
 * Dead-letter: notifications.dlq (if delivery fails after retries)
 */
@Configuration
public class RabbitMQConfig {

    public static final String ORDERS_EXCHANGE        = "orders.exchange";
    public static final String NOTIFICATIONS_QUEUE    = "notifications.queue";
    public static final String INVENTORY_EVENTS_QUEUE = "inventory.events.queue";
    public static final String NOTIFICATIONS_DLQ      = "notifications.dlq";
    public static final String NOTIFICATIONS_DL_EXCHANGE = "notifications.dl.exchange";

    @Bean
    public TopicExchange ordersExchange() {
        return ExchangeBuilder.topicExchange(ORDERS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(NOTIFICATIONS_DL_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(NOTIFICATIONS_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATIONS_DL_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dead-letter")
                .withArgument("x-message-ttl", 86400000) // 24h TTL
                .build();
    }

    @Bean
    public Queue inventoryEventsQueue() {
        return QueueBuilder.durable(INVENTORY_EVENTS_QUEUE).build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(NOTIFICATIONS_DLQ).build();
    }

    @Bean
    public Binding notificationsBinding() {
        return BindingBuilder.bind(notificationsQueue())
                .to(ordersExchange())
                .with("order.#");
    }

    @Bean
    public Binding paymentNotificationsBinding() {
        return BindingBuilder.bind(notificationsQueue())
                .to(ordersExchange())
                .with("payment.#");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("dead-letter");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        return template;
    }
}
