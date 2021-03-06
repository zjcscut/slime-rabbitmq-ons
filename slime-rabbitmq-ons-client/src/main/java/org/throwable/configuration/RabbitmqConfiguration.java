package org.throwable.configuration;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.throwable.common.constants.Constants;
import org.throwable.support.TransactionMessageProducer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 0:03
 */
@EnableConfigurationProperties(value = {OnsClientProperties.class, RabbitProperties.class})
@Configuration
public class RabbitmqConfiguration {

    private static final int DEFAILT_CONCURRENTCONSUMERS = 20;
    private static final int DEFAILT_MAXCONCURRENTCONSUMERS = 50;

    private final OnsClientProperties onsClientProperties;
    private final RabbitProperties rabbitProperties;

    public RabbitmqConfiguration(OnsClientProperties onsClientProperties, RabbitProperties rabbitProperties) {
        this.onsClientProperties = onsClientProperties;
        this.rabbitProperties = rabbitProperties;
    }

    @Bean
    @ConditionalOnClass(ConnectionFactory.class)
    public CachingConnectionFactory rabbitConnectionFactory() throws Exception {
        RabbitConnectionFactoryBean factory = new RabbitConnectionFactoryBean();
        if (rabbitProperties.determineHost() != null) {
            factory.setHost(rabbitProperties.determineHost());
        }
        factory.setPort(rabbitProperties.determinePort());
        if (rabbitProperties.determineUsername() != null) {
            factory.setUsername(rabbitProperties.determineUsername());
        }
        if (rabbitProperties.determinePassword() != null) {
            factory.setPassword(rabbitProperties.determinePassword());
        }
        if (rabbitProperties.determineVirtualHost() != null) {
            factory.setVirtualHost(rabbitProperties.determineVirtualHost());
        }
        if (rabbitProperties.getRequestedHeartbeat() != null) {
            factory.setRequestedHeartbeat(rabbitProperties.getRequestedHeartbeat());
        }
        RabbitProperties.Ssl ssl = rabbitProperties.getSsl();
        if (ssl.isEnabled()) {
            factory.setUseSSL(true);
            if (ssl.getAlgorithm() != null) {
                factory.setSslAlgorithm(ssl.getAlgorithm());
            }
            factory.setKeyStore(ssl.getKeyStore());
            factory.setKeyStorePassphrase(ssl.getKeyStorePassword());
            factory.setTrustStore(ssl.getTrustStore());
            factory.setTrustStorePassphrase(ssl.getTrustStorePassword());
        }
        if (rabbitProperties.getConnectionTimeout() != null) {
            factory.setConnectionTimeout(rabbitProperties.getConnectionTimeout());
        }
        factory.afterPropertiesSet();
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(
                factory.getObject());
        connectionFactory.setAddresses(rabbitProperties.determineAddresses());
        connectionFactory.setPublisherConfirms(rabbitProperties.isPublisherConfirms());
        connectionFactory.setPublisherReturns(rabbitProperties.isPublisherReturns());
        if (rabbitProperties.getCache().getChannel().getSize() != null) {
            connectionFactory
                    .setChannelCacheSize(rabbitProperties.getCache().getChannel().getSize());
        }
        if (rabbitProperties.getCache().getConnection().getMode() != null) {
            connectionFactory
                    .setCacheMode(rabbitProperties.getCache().getConnection().getMode());
        }
        if (rabbitProperties.getCache().getConnection().getSize() != null) {
            connectionFactory.setConnectionCacheSize(
                    rabbitProperties.getCache().getConnection().getSize());
        }
        if (rabbitProperties.getCache().getChannel().getCheckoutTimeout() != null) {
            connectionFactory.setChannelCheckoutTimeout(
                    rabbitProperties.getCache().getChannel().getCheckoutTimeout());
        }
        connectionFactory.setPublisherConfirms(true);
        return connectionFactory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(CachingConnectionFactory cachingConnectionFactory) {
        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        containerFactory.setConnectionFactory(cachingConnectionFactory);
        containerFactory.setAcknowledgeMode(AcknowledgeMode.NONE);
        containerFactory.setConcurrentConsumers(DEFAILT_CONCURRENTCONSUMERS);
        containerFactory.setMaxConcurrentConsumers(DEFAILT_MAXCONCURRENTCONSUMERS);
        return containerFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(CachingConnectionFactory cachingConnectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(cachingConnectionFactory);
        List<String> queuesToDeclare = new ArrayList<>();
        queuesToDeclare.add(onsClientProperties.getHalfMessageQueue());
        queuesToDeclare.add(onsClientProperties.getTransactionCheckerQueue());
        queuesToDeclare.add(onsClientProperties.getFireTransactionQueue());
        for (String queueToDeclare : queuesToDeclare) {
            Queue queue = new Queue(queueToDeclare, true, false, false, null);
            rabbitAdmin.declareQueue(queue);
            DirectExchange exchange = new DirectExchange(queueToDeclare, true, false);
            rabbitAdmin.declareExchange(exchange);
            rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(queueToDeclare));
        }
        return rabbitAdmin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory cachingConnectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setEncoding(Constants.ENCODING);
        rabbitTemplate.setConnectionFactory(cachingConnectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public TransactionMessageProducer transactionMessageProducer(RabbitTemplate rabbitTemplate) {
        TransactionMessageProducer producer = new TransactionMessageProducer();
        producer.setRabbitTemplate(rabbitTemplate);
        return producer;
    }

}
