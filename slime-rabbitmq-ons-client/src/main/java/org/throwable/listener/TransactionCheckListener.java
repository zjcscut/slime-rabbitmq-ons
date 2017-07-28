package org.throwable.listener;

import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.throwable.common.constants.Constants;
import org.throwable.common.constants.LocalTransactionStats;
import org.throwable.common.constants.SendStats;
import org.throwable.configuration.OnsProperties;
import org.throwable.exception.LocalTransactionCheckException;
import org.throwable.exception.SendMqMessageException;
import org.throwable.support.LocalTransactionChecker;
import org.throwable.support.RabbitmqMessagePropertiesConverter;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 14:26
 */
@Slf4j
@Component
public class TransactionCheckListener implements EnvironmentAware, BeanFactoryAware {

    private static final long DEFAULT_CONFIRM_TIMEOUT_SECONDS = 5;
    private DefaultListableBeanFactory beanFactory;
    private String halfMessageQueue;
    private volatile RabbitmqMessagePropertiesConverter converter = new RabbitmqMessagePropertiesConverter();

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void setEnvironment(Environment environment) {
        this.halfMessageQueue = environment.getProperty(OnsProperties.HALFMESSAGE_QUEUE_KEY,
                OnsProperties.DEFAULT_HALFMESSAGE_QUEUE);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = Constants.TRANSACTIONCHECKERQUEUE_PROPERTIES_KEY,
            containerFactory = Constants.CONTAINERFACTORY_KEY,
            admin = Constants.RABBITADMIN_KEY)
    public void onMessage(Message message) throws Exception {
        MessageProperties messageProperties = message.getMessageProperties();
        String uniqueCode = converter.getHeaderValue(messageProperties, Constants.UNIQUECODE_KEY);
        String checkerClassName = converter.getHeaderValue(messageProperties, Constants.CHECKERCLASSNAME_KEY);
        if (null != checkerClassName) {
            LocalTransactionStats localTransactionStats;
            try {
                Class<? extends LocalTransactionChecker> checkerClazz = (Class<? extends LocalTransactionChecker>) Class.forName(checkerClassName);
                LocalTransactionChecker transactionChecker = beanFactory.getBean(checkerClazz);
                localTransactionStats = transactionChecker.doInTransactionCheck(message);
                final LocalTransactionStats finalLocalTransactionStats = localTransactionStats;
                rabbitTemplate.execute((ChannelCallback<Void>) channel -> {
                    message.getMessageProperties().setHeader(Constants.LOCALTRANSACTIONSTATS_KEY, finalLocalTransactionStats);
                    message.getMessageProperties().setHeader(Constants.SENDSTATS_KEY, SendStats.HALF_SUCCESS);
                    channel.confirmSelect();
                    AMQP.BasicProperties basicProperties = converter.convertToBasicProperties(message.getMessageProperties());
                    channel.basicPublish(halfMessageQueue, halfMessageQueue, basicProperties, null);
                    if (!channel.waitForConfirms(DEFAULT_CONFIRM_TIMEOUT_SECONDS * 1000)) {
                        throw new SendMqMessageException(String.format("LocalTransactionChecker send confirm message " +
                                "failed,check transaction for uniqueCode:%s failed!", uniqueCode));
                    }
                    return null;
                });
            } catch (Exception e) {
                log.error("TransactionCheckListener process transactionStats check failed,uniqueCode:{}", uniqueCode, e);
                throw new LocalTransactionCheckException(e);
            }
        }
    }
}