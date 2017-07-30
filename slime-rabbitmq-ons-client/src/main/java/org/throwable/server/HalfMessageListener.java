package org.throwable.server;

import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.throwable.common.constants.Constants;
import org.throwable.common.constants.FireTransactionStats;
import org.throwable.common.constants.LocalTransactionStats;
import org.throwable.common.constants.SendStats;
import org.throwable.configuration.OnsServerProperties;
import org.throwable.server.constants.PushStats;
import org.throwable.server.dao.TransactionLogDao;
import org.throwable.server.dao.TransactionMessageDao;
import org.throwable.server.model.TransactionLog;
import org.throwable.server.model.TransactionMessage;
import org.throwable.server.service.AbstractRabbitmqSupportableService;
import org.throwable.support.TransactionTemplateProvider;
import org.throwable.support.id.KeyGenerator;

import java.util.Date;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 15:13
 */
@Slf4j
@Component
public class HalfMessageListener extends AbstractRabbitmqSupportableService implements EnvironmentAware {


	private String fireTransactionQueue;
	private Integer confirmTimeoutSeconds;

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
		this.fireTransactionQueue = environment.getProperty(OnsServerProperties.FIRETRANSACTION_QUEUE_KEY,
				OnsServerProperties.DEFAULT_FIRETRANSACTION_QUEUE);
		this.confirmTimeoutSeconds = environment.getProperty(OnsServerProperties.CONFIRM_TIMEOUT_SECONDS_KEY,
				Integer.class, OnsServerProperties.DEFAULT_CONFIRM_TIMEOUT_SECONDS);
	}

	@RabbitListener(queues = Constants.HALFMESSAGEQUEUE_PROPERTIES_KEY,
			containerFactory = Constants.CONTAINERFACTORY_KEY,
			admin = Constants.RABBITADMIN_KEY)
	public void onMessage(Message message) throws Exception {
		String body = new String(message.getBody(), Constants.ENCODING);
		MessageProperties messageProperties = message.getMessageProperties();
		String queue = converter.getHeaderValue(messageProperties, Constants.QUEUE_KEY);
		String exchange = converter.getHeaderValue(messageProperties, Constants.EXCHANGE_KEY);
		String routingKey = converter.getHeaderValue(messageProperties, Constants.ROUTINGKEY_KEY);
		String exchangeType = converter.getHeaderValue(messageProperties, Constants.EXCHANGETYPE_KEY);
		String headers = converter.getHeaderValue(messageProperties, Constants.HEADERS_KEY);
		String messageId = converter.getHeaderValue(messageProperties, Constants.MESSAGEID_KEY);
		String uniqueCode = converter.getHeaderValue(messageProperties, Constants.UNIQUECODE_KEY);
		String checkerClassName = converter.getHeaderValue(messageProperties, Constants.CHECKERCLASSNAME_KEY);
		SendStats sendStats = converter.getHeaderValue(messageProperties, Constants.SENDSTATS_KEY, SendStats.class, SendStats.FAIL);
		LocalTransactionStats localTransactionStats =
				converter.getHeaderValue(messageProperties, Constants.LOCALTRANSACTIONSTATS_KEY, LocalTransactionStats.class, LocalTransactionStats.UNKNOWN);
		switch (sendStats) {
			case PREPARE:
				processPrepareTransaction(message, body, messageId, uniqueCode, queue, exchange, exchangeType, headers,
						routingKey, checkerClassName, localTransactionStats);
				break;
			case HALF_SUCCESS:
				processHalfMessageTransaction(message, uniqueCode, localTransactionStats);
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
										   String exchangeType,
										   String headers,
										   String routingKey,
										   String checkerClassName,
										   LocalTransactionStats localTransactionStats) {
		TransactionTemplate transactionTemplate =
				transactionTemplateProvider.getTransactionTemplate(TransactionDefinition.PROPAGATION_REQUIRES_NEW,
						TransactionDefinition.ISOLATION_READ_COMMITTED);
		final TransactionLog transactionLogCallback = transactionTemplate.execute(transactionStatus -> {
			TransactionMessage transactionMessage = new TransactionMessage();
			transactionMessage.setContent(body);
			transactionMessage.setQueue(queue);
			transactionMessage.setExchange(exchange);
			transactionMessage.setRoutingKey(routingKey);
			transactionMessage.setUniqueCode(uniqueCode);
			transactionMessage.setExchangeType(exchangeType);
			transactionMessage.setHeaders(headers);
			transactionMessageDao.save(transactionMessage);
			TransactionLog transactionLog = new TransactionLog();
			transactionLog.setMessageId(messageId);
			transactionLog.setUniqueCode(uniqueCode);
			transactionLog.setTransactionId(keyGenerator.generateKey());
			transactionLog.setTransactionStats(localTransactionStats.toString());
			transactionLog.setPushStats(PushStats.INIT.toString());
			transactionLog.setFireTransactionStats(FireTransactionStats.INIT.toString());
			transactionLog.setCheckerClassName(checkerClassName);
			transactionLogDao.save(transactionLog, transactionMessage.getId());
			return transactionLog;
		});
		FireTransactionStats fireTransactionStatsCallback
				= rabbitTemplate.execute(channel -> {
			message.getMessageProperties().setHeader(Constants.TRANSACTIONID_KEY, transactionLogCallback.getTransactionId());
			channel.confirmSelect();
			AMQP.BasicProperties basicProperties = converter.convertToBasicProperties(message.getMessageProperties());
			channel.basicPublish(fireTransactionQueue, fireTransactionQueue, basicProperties, body.getBytes(Constants.ENCODING));
			if (channel.waitForConfirms(confirmTimeoutSeconds * 1000)) {
				return FireTransactionStats.SUCCESS;
			}
			if (log.isWarnEnabled()) {
				log.warn(String.format("Publish and Confirm message to fire transaction failed,uniqueCode:%s,transactionId:%s",
						uniqueCode, transactionLogCallback.getTransactionId()));
			}
			return FireTransactionStats.FAIL;
		});
		transactionLogDao.updateFireTransactionStats(transactionLogCallback.getId(), fireTransactionStatsCallback.toString(), new Date());
	}

	private void processHalfMessageTransaction(Message message,
											   String uniqueCode,
											   LocalTransactionStats localTransactionStats) {
		TransactionLog transactionLog = transactionLogDao.fetchByUniqueCode(uniqueCode);
		if (null == transactionLog) {
			return;
		}
		if (LocalTransactionStats.COMMITTED.equals(localTransactionStats) ||
				LocalTransactionStats.ROLLBACK.equals(localTransactionStats)) {
			transactionLogDao.updateTransactionStats(transactionLog.getId(), localTransactionStats.toString(), new Date());

		}
		if (LocalTransactionStats.COMMITTED.equals(localTransactionStats)) {
			TransactionMessage transactionMessage = transactionMessageDao.fetchById(transactionLog.getTransactionMessageId());
			if (null != transactionMessage) {
				PushStats pushStatsCallback =
						rabbitTemplate.execute(channel -> {
							String queue = transactionMessage.getQueue();
							String exchange = transactionMessage.getExchange();
							String routingKey = transactionMessage.getRoutingKey();
							String exchangeType = transactionMessage.getExchangeType();
							String headers = transactionMessage.getHeaders();
							declareIfNecessary(queue, exchange, routingKey, exchangeType, headers);
							channel.confirmSelect();
							AMQP.BasicProperties basicProperties = converter.convertToBasicProperties(message.getMessageProperties());
							channel.basicPublish(transactionMessage.getExchange(),
									transactionMessage.getRoutingKey(),
									basicProperties,
									transactionMessage.getContent().getBytes(Constants.ENCODING));
							if (channel.waitForConfirms(confirmTimeoutSeconds * 1000)) {
								return PushStats.SUCCESS;
							}
							if (log.isWarnEnabled()) {
								log.warn(String.format("Publish and Confirm message failed,uniqueCode:%s," +
										"queue:%s,exchange:%s,routingKey:%s", uniqueCode, queue, exchange, routingKey));
							}
							return PushStats.FAIL;
						});
				transactionLogDao.updatePushStats(transactionLog.getId(), pushStatsCallback.toString(), new Date());
			}
		}
	}

}
