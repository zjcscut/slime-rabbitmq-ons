package org.throwable.listener;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/26 23:52
 */

@Component
public class TransactionCallbackListener {

	@RabbitListener(queues = "${slime.ons.transactionCallbackQueue}",
			containerFactory = "simpleRabbitListenerContainerFactory",
			admin = "rabbitAdmin")
	public void onMessage(Message message) {
		String body = new String(message.getBody());
		MessageProperties messageProperties = message.getMessageProperties();
		Map<String, Object> headers = messageProperties.getHeaders();
		System.out.println("TransactionCallbackListener body --> " + body);
		System.out.println("TransactionCallbackListener headers --> " + headers);
	}

}
