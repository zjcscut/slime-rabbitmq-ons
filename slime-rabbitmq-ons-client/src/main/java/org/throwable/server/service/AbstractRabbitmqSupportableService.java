package org.throwable.server.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.throwable.common.constants.ExchangeEnum;
import org.throwable.support.RabbitmqMessagePropertiesConverter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/29 22:38
 */
public abstract class AbstractRabbitmqSupportableService {

	private static final Set<String> declareHolders = new HashSet<>();

	protected volatile RabbitmqMessagePropertiesConverter converter = new RabbitmqMessagePropertiesConverter();

	@Autowired
	protected RabbitTemplate rabbitTemplate;

	@Autowired
	protected RabbitAdmin rabbitAdmin;

	protected void declareIfNecessary(String queue, String exchange, String routingKey, String exchangeType, String headers) {
		String key = appendKey(queue, exchange, routingKey, exchangeType, headers);
		if (!declareHolders.contains(key)) {
			if (null == exchange || exchange.isEmpty()) {
				rabbitAdmin.declareQueue(new Queue(queue, true, false, false));
			} else {
				switch (ExchangeEnum.parse(exchangeType)) {
					case DIRECT:
						Queue directQueue = new Queue(queue, true, false, false);
						rabbitAdmin.declareQueue(directQueue);
						DirectExchange directExchange = new DirectExchange(exchange, true, false, null);
						rabbitAdmin.declareExchange(directExchange);
						rabbitAdmin.declareBinding(BindingBuilder.bind(directQueue).to(directExchange).with(routingKey));
						break;
					case TOPIC:
						Queue topicQueue = new Queue(queue, true, false, false);
						rabbitAdmin.declareQueue(topicQueue);
						TopicExchange topicExchange = new TopicExchange(exchange, true, false, null);
						rabbitAdmin.declareExchange(topicExchange);
						rabbitAdmin.declareBinding(BindingBuilder.bind(topicQueue).to(topicExchange).with(routingKey));
						break;
					case FANOUT:
						Queue fanoutQueue = new Queue(queue, true, false, false);
						rabbitAdmin.declareQueue(fanoutQueue);
						FanoutExchange fanoutExchange = new FanoutExchange(exchange, true, false, null);
						rabbitAdmin.declareExchange(fanoutExchange);
						rabbitAdmin.declareBinding(BindingBuilder.bind(fanoutQueue).to(fanoutExchange));
						break;
					case HEADERS:
						Queue headersQueue = new Queue(queue, true, false, false);
						rabbitAdmin.declareQueue(headersQueue);
						HeadersExchange headersExchange = new HeadersExchange(exchange, true, false, null);
						rabbitAdmin.declareExchange(headersExchange);
						rabbitAdmin.declareBinding(BindingBuilder.bind(headersQueue).to(headersExchange).whereAll(
								JSON.parseObject(headers, new TypeReference<Map<String, Object>>() {
								})
						).match());
						break;
				}
			}
			declareHolders.add(key);
		}
	}

	protected String appendKey(String... objects) {
		StringBuilder keys = new StringBuilder();
		for (String each : objects) {
			keys.append(each).append("-");
		}
		return keys.substring(0, keys.lastIndexOf("-"));
	}
}
