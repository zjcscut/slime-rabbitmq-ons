package org.throwable.server;

import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.throwable.common.constants.FireTransactionStats;
import org.throwable.common.constants.LocalTransactionStats;
import org.throwable.common.constants.SendStats;
import org.throwable.configuration.OnsProperties;
import org.throwable.exception.SendMqMessageException;
import org.throwable.server.constants.PushStats;
import org.throwable.server.dao.TransactionLogDao;
import org.throwable.server.dao.TransactionMessageDao;
import org.throwable.server.model.TransactionLog;
import org.throwable.server.model.TransactionMessage;
import org.throwable.support.RabbitmqMessagePropertiesConverter;
import org.throwable.support.RetryTemplateProvider;
import org.throwable.support.TransactionTemplateProvider;
import org.throwable.support.id.KeyGenerator;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 15:13
 */
@Slf4j
@Component
public class HalfMessageListener implements EnvironmentAware {

	private static final Set<String> declareHolders = new HashSet<>();

	private volatile RabbitmqMessagePropertiesConverter converter = new RabbitmqMessagePropertiesConverter();

	private String fireTransactionQueue;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private RetryTemplateProvider retryTemplateProvider;

	@Autowired
	private RabbitAdmin rabbitAdmin;

	@Autowired
	private TransactionLogDao transactionLogDao;

	@Autowired
	private TransactionMessageDao transactionMessageDao;

	@Autowired
	private KeyGenerator keyGenerator;

	@Autowired
	private TransactionTemplateProvider transactionTemplateProvider;

	@Override
	public void setEnvironment(Environment environment) {
		this.fireTransactionQueue = environment.getProperty(OnsProperties.FIRETRANSACTION_QUEUE_KEY,
				OnsProperties.DEFAULT_FIRETRANSACTION_QUEUE);
	}

	@RabbitListener(queues = "${slime.ons.halfMessageQueue}",
			containerFactory = "simpleRabbitListenerContainerFactory",
			admin = "rabbitAdmin")
	public void onMessage(Message message) throws Exception {
		String body = new String(message.getBody(), "UTF-8");
		MessageProperties messageProperties = message.getMessageProperties();
		String queue = converter.getHeaderValue(messageProperties, "queue");
		String exchange = converter.getHeaderValue(messageProperties, "exchange");
		String routingKey = converter.getHeaderValue(messageProperties, "routingKey");
		String messageId = converter.getHeaderValue(messageProperties, "messageId");
		String uniqueCode = converter.getHeaderValue(messageProperties, "uniqueCode");
		SendStats sendStats = converter.getHeaderValue(messageProperties, "sendStats", SendStats.class, SendStats.FAIL);
		LocalTransactionStats localTransactionStats =
				converter.getHeaderValue(messageProperties, "localTransactionStats", LocalTransactionStats.class, LocalTransactionStats.UNKNOWN);
		switch (sendStats) {
			case PREPARE:
				processPrepareTransaction(message, body, messageId, uniqueCode, queue, exchange, routingKey, localTransactionStats);
				break;
			case HALF_SUCCESS:
				processHalfMessageTransaction(message, queue, exchange, routingKey, uniqueCode, localTransactionStats);
				break;
			case SUCCESS:
				break;
			case FAIL:
				break;
		}
	}

	private void processPrepareTransaction(Message message,
										   String body,
										   String messageId,
										   String uniqueCode,
										   String queue,
										   String exchange,
										   String routingKey,
										   LocalTransactionStats localTransactionStats) {
		TransactionTemplate transactionTemplate =
				transactionTemplateProvider.getTransactionTemplate(TransactionDefinition.PROPAGATION_REQUIRES_NEW,
						TransactionDefinition.ISOLATION_READ_COMMITTED);
		final TransactionLog callback = transactionTemplate.execute(transactionStatus -> {
			TransactionMessage transactionMessage = new TransactionMessage();
			transactionMessage.setContent(body);
			transactionMessage.setQueue(queue);
			transactionMessage.setExchange(exchange);
			transactionMessage.setRoutingKey(routingKey);
			transactionMessage.setUniqueCode(uniqueCode);
			transactionMessageDao.save(transactionMessage);
			TransactionLog transactionLog = new TransactionLog();
			transactionLog.setMessageId(messageId);
			transactionLog.setUniqueCode(uniqueCode);
			transactionLog.setTransactionId(keyGenerator.generateKey());
			transactionLog.setTransactionStats(localTransactionStats.toString());
			transactionLog.setPushStats(PushStats.INIT.toString());
			transactionLog.setFireTransactionStats(FireTransactionStats.INIT.toString());
			transactionLogDao.save(transactionLog, transactionMessage.getId());
			return transactionLog;
		});
		RetryTemplate retryTemplate = retryTemplateProvider.getDefaultRetryTemplate();
		FireTransactionStats fireTransactionStatsCallback
				= retryTemplate.execute((RetryCallback<FireTransactionStats, SendMqMessageException>) context -> {
			try {
				return rabbitTemplate.execute(channel -> {
					message.getMessageProperties().setHeader("transactionId", callback.getTransactionId());
					channel.confirmSelect();
					AMQP.BasicProperties basicProperties = converter.convertToBasicProperties(message.getMessageProperties());
					channel.basicPublish(fireTransactionQueue, fireTransactionQueue, basicProperties, body.getBytes("UTF-8"));
					if (channel.waitForConfirms(5000)) {
						return FireTransactionStats.SUCCESS;
					}
					throw new SendMqMessageException(String.format("Publish and Confirm message to fire transaction failed,uniqueCode:%s,transactionId:%s",
							uniqueCode, callback.getTransactionId()));
				});
			} catch (Exception e) {
				throw new SendMqMessageException(e);
			}
		}, context -> FireTransactionStats.FAIL);
		transactionLogDao.updateFireTransactionStats(callback.getId(), fireTransactionStatsCallback.toString(), new Date());
	}

	private void processHalfMessageTransaction(Message message,
											   String queue,
											   String exchange,
											   String routingKey,
											   String uniqueCode,
											   LocalTransactionStats localTransactionStats) {
		declareIfNecessary(queue, exchange, routingKey);
		if (LocalTransactionStats.COMMITED.equals(localTransactionStats) ||
				LocalTransactionStats.ROLLBACK.equals(localTransactionStats)) {
			transactionLogDao.updateTransactionStats(uniqueCode, localTransactionStats.toString(), new Date());

		}
		if (LocalTransactionStats.COMMITED.equals(localTransactionStats)) {
			TransactionLog transactionLog = transactionLogDao.fetchByUniqueCode(uniqueCode);
			TransactionMessage transactionMessage = transactionMessageDao.fetchById(transactionLog.getTransactionMessageId());
			if (null != transactionMessage) {
				RetryTemplate retryTemplate = retryTemplateProvider.getDefaultRetryTemplate();
				PushStats pushStatsCallback = retryTemplate.execute((RetryCallback<PushStats, SendMqMessageException>) context -> {
					try {
						return rabbitTemplate.execute(channel -> {
							channel.confirmSelect();
							AMQP.BasicProperties basicProperties = converter.convertToBasicProperties(message.getMessageProperties());
							channel.basicPublish(exchange, routingKey, basicProperties, transactionMessage.getContent().getBytes("UTF-8"));
							if (channel.waitForConfirms(5000)) {
								return PushStats.SUCCESS;
							}
							throw new SendMqMessageException(String.format("Publish and Confirm message failed,uniqueCode:%s," +
									"queue:%s,exchange:%s,routingKey:%s", uniqueCode, queue, exchange, routingKey));
						});
					} catch (Exception e) {
						throw new SendMqMessageException(e);
					}
				}, context -> PushStats.FAIL);
				transactionLogDao.updatePushStats(transactionLog.getId(), pushStatsCallback.toString(), new Date());
			}
		}
	}

	private void declareIfNecessary(String queue, String exchange, String routingKey) {
		String key = appendKey(queue, exchange, routingKey);
		if (!declareHolders.contains(key)) {
			if (null == exchange) {
				rabbitAdmin.declareQueue(new Queue(queue, true, false, false));
			} else {
				Queue queueToUse = new Queue(queue, true, false, false);
				rabbitAdmin.declareQueue(queueToUse);
				DirectExchange exchangeToUse = new DirectExchange(exchange, true, false, null);
				rabbitAdmin.declareExchange(exchangeToUse);
				rabbitAdmin.declareBinding(BindingBuilder.bind(queueToUse).to(exchangeToUse).with(routingKey));
			}
			declareHolders.add(key);
		}
	}

	private String appendKey(String... objects) {
		StringBuilder keys = new StringBuilder();
		for (String each : objects) {
			keys.append(each).append("-");
		}
		return keys.substring(0, keys.lastIndexOf("-"));
	}

}
