package org.throwable.support;

import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.throwable.common.constants.LocalTransactionStats;
import org.throwable.common.constants.SendStats;
import org.throwable.common.model.TransactionCallbackResult;
import org.throwable.common.model.TransactionSendResult;
import org.throwable.configuration.OnsProperties;

import java.util.Date;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/26 15:45
 */
@Slf4j
@Component
public class TransactionMessageProducer implements EnvironmentAware {

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final long DEFAULT_COMFIRM_TIMEOUT_SECONDS = 5;
    private volatile MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
    private String halfMessageQueue;

    @Override
    public void setEnvironment(Environment environment) {
        this.halfMessageQueue = environment.getProperty(OnsProperties.HALFMESSAGE_QUEUE_KEY, OnsProperties.DEFAULT_HALFMESSAGE_QUEUE);
    }

    private RabbitTemplate rabbitTemplate;

    public RabbitTemplate getRabbitTemplate() {
        return rabbitTemplate;
    }

    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public TransactionSendResult sendMessageInTransaction(final String messageId,
                                                          final String uniqueCode,
                                                          final Message message,
                                                          final LocalTransactionExecutor executor,
                                                          long exexuteTimeoutSeconds) {
        return sendMessageInTransaction(messageId, uniqueCode, message, executor,
                DEFAULT_COMFIRM_TIMEOUT_SECONDS, exexuteTimeoutSeconds);
    }

    public TransactionSendResult sendMessageInTransaction(final String messageId,
                                                          final String uniqueCode,
                                                          final Message message,
                                                          final LocalTransactionExecutor executor,
                                                          long comfirmTimeoutSeconds,
                                                          long exexuteTimeoutSeconds) {
        TransactionSendResult sendResult = new TransactionSendResult();
        sendResult.setSendStats(SendStats.PREPARE);
        sendResult.setMessageId(messageId);
        sendResult.setUniqueCode(uniqueCode);
        sendResult.setLocalTransactionStats(LocalTransactionStats.UNKNOWN);
        BlockingLocalTransactionExecutorConsumer consumer = new BlockingLocalTransactionExecutorConsumer();
        consumer.addLocalTransactionExecutor(executor);
        LocalTransactionExecutorSynchronizer.addTransactionComsumer(uniqueCode, consumer);
        try {
            wrapMessageProperties(message, messageId, uniqueCode);
            AMQP.BasicProperties basicProperties = this.messagePropertiesConverter
                    .fromMessageProperties(message.getMessageProperties(), DEFAULT_ENCODING);
            sendResult.setSendStats(rabbitTemplate.execute(channel -> {
                basicProperties.getHeaders().put("localTransactionStat", sendResult.getLocalTransactionStats());
                channel.confirmSelect();
                channel.basicPublish(halfMessageQueue, halfMessageQueue, basicProperties, message.getBody());
                try {
                    if (channel.waitForConfirms(comfirmTimeoutSeconds * 1000)) {
                        return SendStats.SUCCESS;
                    }
                } catch (Exception e) {
                    log.error("Comfirm message failed, messageId:{},uniqueCode:{}", messageId, uniqueCode, e);
                }
                return SendStats.FAIL;
            }));
            if (SendStats.SUCCESS.equals(sendResult.getSendStats())) {
                try {
                    TransactionCallbackResult callbackResult = consumer.processLocalTransactionExecutor(exexuteTimeoutSeconds);
                    sendResult.setLocalTransactionStats(callbackResult.getLocalTransactionStats());
                    sendResult.setTransactionId(callbackResult.getTransactionId());
                } catch (Exception e) {
                    sendResult.setLocalTransactionStats(LocalTransactionStats.ROLLBACK);
                    log.error(String.format("Execute transaction failed,messageId:%s,uniqueCode:%s,message:%s",
                            messageId,
                            uniqueCode,
                            new String(message.getBody())
                    ), e);
                }
            }
            if (LocalTransactionStats.COMMITED.equals(sendResult.getLocalTransactionStats()) ||
                    LocalTransactionStats.ROLLBACK.equals(sendResult.getLocalTransactionStats())) {
                sendResult.setSendStats(rabbitTemplate.execute(channel -> {
                    basicProperties.getHeaders().put("localTransactionStat", sendResult.getLocalTransactionStats());
                    channel.confirmSelect();
                    channel.basicPublish(halfMessageQueue, halfMessageQueue, basicProperties, message.getBody());
                    try {
                        if (channel.waitForConfirms(comfirmTimeoutSeconds * 1000)) {
                            return SendStats.SUCCESS;
                        }
                    } catch (Exception e) {
                        log.error("Comfirm message failed, messageId:{},uniqueCode:{}", messageId, uniqueCode, e);
                    }
                    return SendStats.FAIL;
                }));
            }
            return sendResult;
        } finally {
            LocalTransactionExecutorSynchronizer.removeTransactionComsumer(uniqueCode);
        }
    }

    private void wrapMessageProperties(final Message message,
                                       final String messageId,
                                       final String uniqueCode) {
        MessageProperties messageProperties = message.getMessageProperties();
        messageProperties.setHeader("messageId", messageId);
        messageProperties.setHeader("uniqueCode", uniqueCode);
        messageProperties.setTimestamp(new Date());
        messageProperties.setContentEncoding(DEFAULT_ENCODING);
    }
}
