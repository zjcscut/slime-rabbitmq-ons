package org.throwable.listener;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.throwable.support.BlockingLocalTransactionExecutorConsumer;
import org.throwable.support.LocalTransactionExecutionSynchronizer;

import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 0:44
 */
//@Component
public class FireTransactionListener {

    @RabbitListener(queues = "${slime.ons.fireTransactionQueue}",
            containerFactory = "simpleRabbitListenerContainerFactory",
            admin = "rabbitAdmin")
    public void onMessage(Message message) throws Exception {
        MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> headers = messageProperties.getHeaders();
        String uniqueCode = headers.get("uniqueCode").toString();
        String transactionId = headers.get("transactionId").toString();
        if (LocalTransactionExecutionSynchronizer.existTransactionConsumer(uniqueCode)) {
            BlockingLocalTransactionExecutorConsumer consumer = LocalTransactionExecutionSynchronizer.getTransactionConsumer(uniqueCode);
            consumer.setTransactionId(transactionId);
            if (LocalTransactionExecutionSynchronizer.existTransactionExecutor(uniqueCode)) {
                consumer.addLocalTransactionExecutor(LocalTransactionExecutionSynchronizer.getTransactionExecutor(uniqueCode));
            }
        }
    }
}
