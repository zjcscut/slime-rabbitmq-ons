package org.throwable.listener;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.throwable.support.BlockingLocalTransactionExecutorConsumer;
import org.throwable.support.LocalTransactionExecutorSynchronizer;

import java.util.Map;
import java.util.UUID;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 0:44
 */
@Component
public class HalfMessageListener {

	@RabbitListener(queues = "${slime.ons.halfMessageQueue}",
			containerFactory = "simpleRabbitListenerContainerFactory",
			admin = "rabbitAdmin")
	public void onMessage(Message message) throws Exception {
		String body = new String(message.getBody());
		MessageProperties messageProperties = message.getMessageProperties();
		Map<String, Object> headers = messageProperties.getHeaders();
		System.out.println("HalfMessageListener body --> " + body);
		System.out.println("HalfMessageListener headers --> " + headers);
		String code = headers.get("uniqueCode").toString();
		Thread.sleep(2000);
		if (LocalTransactionExecutorSynchronizer.existTransactionConsumer(code)) {
			BlockingLocalTransactionExecutorConsumer consumer = LocalTransactionExecutorSynchronizer.getTransactionConsumer(code);
			consumer.setTransactionId(UUID.randomUUID().toString());
			consumer.addLocalTransactionExecutor(LocalTransactionExecutorSynchronizer.getTransactionExecutor(code));
		}
	}
}
