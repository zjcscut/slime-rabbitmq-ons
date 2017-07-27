package org.throwable.support;

import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.throwable.common.constants.LocalTransactionStats;
import org.throwable.common.constants.SendStats;
import org.throwable.common.model.TransactionCallbackResult;
import org.throwable.common.model.TransactionSendResult;
import org.throwable.configuration.OnsProperties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/26 15:45
 */
@Slf4j
public class TransactionMessageProducer implements EnvironmentAware {


    private static final long DEFAULT_CONFIRM_TIMEOUT_SECONDS = 5;
    private String halfMessageQueue;
    private volatile RabbitmqMessagePropertiesConverter converter = new RabbitmqMessagePropertiesConverter();

    @Override
    public void setEnvironment(Environment environment) {
        this.halfMessageQueue = environment.getProperty(OnsProperties.HALFMESSAGE_QUEUE_KEY,
                OnsProperties.DEFAULT_HALFMESSAGE_QUEUE);
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
                                                          final LocalTransactionChecker checker,
                                                          long executeTimeoutSeconds,
                                                          final String destinationQueue,
                                                          final String destinationExchange,
                                                          final String destinationRoutingKey) {
        return sendMessageInTransaction(messageId, uniqueCode, message, executor,checker,
                DEFAULT_CONFIRM_TIMEOUT_SECONDS, executeTimeoutSeconds,
                destinationQueue, destinationExchange, destinationRoutingKey, null);
    }

    public TransactionSendResult sendMessageInTransaction(final String messageId,
                                                          final String uniqueCode,
                                                          final Message message,
                                                          final LocalTransactionExecutor executor,
                                                          final LocalTransactionChecker checker,
                                                          long confirmTimeoutSeconds,
                                                          long executeTimeoutSeconds,
                                                          final String destinationQueue,
                                                          final String destinationExchange,
                                                          final String destinationRoutingKey,
                                                          final String args) {
        Assert.hasText(destinationQueue, "destinationQueue must not be empty!");
        Assert.hasText(destinationExchange,"destinationExchange must not be empty!");
        Assert.hasText(destinationRoutingKey,"destinationRoutingKey must not be empty!");
        Assert.hasText(messageId,"messageId must not be empty!");
        Assert.hasText(uniqueCode,"uniqueCode must not be empty!");
        Assert.notNull(message,"message must not be null!");
        Assert.notNull(executor,"executor must not be null!");
        Assert.notNull(checker,"checker must not be null!");
        TransactionSendResult sendResult = new TransactionSendResult();
        sendResult.setSendStats(SendStats.PREPARE);
        sendResult.setMessageId(messageId);
        sendResult.setUniqueCode(uniqueCode);
        sendResult.setLocalTransactionStats(LocalTransactionStats.UNKNOWN);
        LocalTransactionExecutionSynchronizer.addTransactionChecker(uniqueCode, checker);
        LocalTransactionExecutionSynchronizer.addTransactionExecutor(uniqueCode, executor);
        BlockingLocalTransactionExecutorConsumer consumer = new BlockingLocalTransactionExecutorConsumer();
        LocalTransactionExecutionSynchronizer.addTransactionConsumer(uniqueCode, consumer);
        try {
            converter.wrapMessageProperties(message, messageId, uniqueCode, destinationQueue, destinationExchange, destinationRoutingKey, args);
            sendResult.setSendStats(rabbitTemplate.execute(channel -> {
                message.getMessageProperties().setHeader("localTransactionStat", sendResult.getLocalTransactionStats());
                message.getMessageProperties().setHeader("sendStats", sendResult.getSendStats());
                channel.confirmSelect();
                AMQP.BasicProperties basicProperties = converter.convertToBasicProperties(message.getMessageProperties());
                channel.basicPublish(halfMessageQueue, halfMessageQueue, basicProperties, message.getBody());
                try {
                    if (channel.waitForConfirms(confirmTimeoutSeconds * 1000)) {
                        return SendStats.HALF_SUCCESS;
                    }
                } catch (Exception e) {
                    log.error("Confirm message failed, messageId:{},uniqueCode:{}", messageId, uniqueCode, e);
                }
                return SendStats.FAIL;
            }));
            if (SendStats.HALF_SUCCESS.equals(sendResult.getSendStats())) {
                try {
                    TransactionCallbackResult callbackResult = consumer.processLocalTransactionExecutor(executeTimeoutSeconds);
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
                    message.getMessageProperties().setHeader("localTransactionStat", sendResult.getLocalTransactionStats());
                    message.getMessageProperties().setHeader("sendStats", sendResult.getSendStats());
                    channel.confirmSelect();
                    AMQP.BasicProperties basicProperties = converter.convertToBasicProperties(message.getMessageProperties());
                    channel.basicPublish(halfMessageQueue, halfMessageQueue, basicProperties, null);
                    try {
                        if (channel.waitForConfirms(confirmTimeoutSeconds * 1000)) {
                            return SendStats.SUCCESS;
                        }
                    } catch (Exception e) {
                        log.error("Confirm message failed, messageId:{},uniqueCode:{}", messageId, uniqueCode, e);
                    }
                    return SendStats.FAIL;
                }));
            }
            return sendResult;
        } finally {
            LocalTransactionExecutionSynchronizer.removeTransactionConsumer(uniqueCode);
            LocalTransactionExecutionSynchronizer.removeTransactionExecutor(uniqueCode);
        }
    }


}
