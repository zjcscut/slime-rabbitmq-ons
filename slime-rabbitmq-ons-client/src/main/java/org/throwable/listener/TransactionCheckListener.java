package org.throwable.listener;

import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.throwable.common.constants.LocalTransactionStats;
import org.throwable.common.constants.SendStats;
import org.throwable.configuration.OnsProperties;
import org.throwable.exception.LocalTransactionCheckException;
import org.throwable.support.LocalTransactionChecker;
import org.throwable.support.LocalTransactionExecutionSynchronizer;
import org.throwable.support.RabbitmqMessagePropertiesConverter;

import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 14:26
 */
@Slf4j
//@Component
public class TransactionCheckListener implements EnvironmentAware {

    private static final long DEFAULT_CONFIRM_TIMEOUT_SECONDS = 5;
    private String halfMessageQueue;
    private volatile RabbitmqMessagePropertiesConverter converter = new RabbitmqMessagePropertiesConverter();

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void setEnvironment(Environment environment) {
        this.halfMessageQueue = environment.getProperty(OnsProperties.HALFMESSAGE_QUEUE_KEY,
                OnsProperties.DEFAULT_HALFMESSAGE_QUEUE);
    }

    @RabbitListener(queues = "${slime.ons.transactionCheckerQueue}",
            containerFactory = "simpleRabbitListenerContainerFactory",
            admin = "rabbitAdmin")
    public void onMessage(Message message) throws Exception {
        MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> headers = messageProperties.getHeaders();
        String uniqueCode = headers.get("uniqueCode").toString();
        if (LocalTransactionExecutionSynchronizer.existTransactionChecker(uniqueCode)) {
            LocalTransactionChecker transactionChecker = LocalTransactionExecutionSynchronizer.getTransactionChecker(uniqueCode);
            LocalTransactionStats localTransactionStats;
            try {
                localTransactionStats = transactionChecker.doInTransactionCheck(message);
                final LocalTransactionStats finalLocalTransactionStats = localTransactionStats;
                rabbitTemplate.execute((ChannelCallback<Void>) channel -> {
                    message.getMessageProperties().setHeader("localTransactionStat", finalLocalTransactionStats);
                    message.getMessageProperties().setHeader("sendStats", SendStats.HALF_SUCCESS);
                    channel.confirmSelect();
                    AMQP.BasicProperties basicProperties = converter.convertToBasicProperties(message.getMessageProperties());
                    channel.basicPublish(halfMessageQueue, halfMessageQueue, basicProperties, null);
                    if (channel.waitForConfirms(DEFAULT_CONFIRM_TIMEOUT_SECONDS * 1000)) {
                        LocalTransactionExecutionSynchronizer.removeTransactionChecker(uniqueCode);
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