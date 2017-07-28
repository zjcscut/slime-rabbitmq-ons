package org.throwable.listener;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.throwable.common.constants.Constants;
import org.throwable.support.BlockingLocalTransactionExecutorConsumer;
import org.throwable.support.LocalTransactionExecutionSynchronizer;
import org.throwable.support.RabbitmqMessagePropertiesConverter;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 0:44
 */
@Component
public class FireTransactionListener {

    private volatile RabbitmqMessagePropertiesConverter converter = new RabbitmqMessagePropertiesConverter();

    @RabbitListener(queues = Constants.FIRETRANSACTIONQUEUE_PROPERTIES_KEY,
            containerFactory = Constants.CONTAINERFACTORY_KEY,
            admin = Constants.RABBITADMIN_KEY)
    public void onMessage(Message message) throws Exception {
        MessageProperties messageProperties = message.getMessageProperties();
        String uniqueCode = converter.getHeaderValue(messageProperties, Constants.UNIQUECODE_KEY);
        String transactionId = converter.getHeaderValue(messageProperties, Constants.TRANSACTIONID_KEY);
        if (LocalTransactionExecutionSynchronizer.existTransactionConsumer(uniqueCode)) {
            BlockingLocalTransactionExecutorConsumer consumer = LocalTransactionExecutionSynchronizer.getTransactionConsumer(uniqueCode);
            consumer.setTransactionId(transactionId);
            if (LocalTransactionExecutionSynchronizer.existTransactionExecutor(uniqueCode)) {
                consumer.addLocalTransactionExecutor(LocalTransactionExecutionSynchronizer.getTransactionExecutor(uniqueCode));
            }
        }
    }
}
