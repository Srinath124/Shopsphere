package com.shopsphere.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange:orders.exchange}")
    private String ordersExchange;

    @Value("${app.rabbitmq.notifications-queue:notifications.queue}")
    private String notificationsQueue;

    @Value("${app.rabbitmq.notifications-dlq:notifications.dlq}")
    private String notificationsDlq;

    @Value("${app.rabbitmq.dlx:notifications.dl.exchange}")
    private String dlx;

    @Bean
    public TopicExchange ordersExchange() {
        return ExchangeBuilder.topicExchange(ordersExchange)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(dlx)
                .durable(true)
                .build();
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(notificationsQueue)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", "dead-letter")
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(notificationsDlq).build();
    }

    @Bean
    public Binding notificationsOrderBinding() {
        return BindingBuilder.bind(notificationsQueue())
                .to(ordersExchange())
                .with("order.#");
    }

    @Bean
    public Binding notificationsPaymentBinding() {
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
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
