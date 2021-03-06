package org.throwable.support;

import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.throwable.common.constants.Constants;
import org.throwable.common.constants.ExchangeEnum;
import org.throwable.common.constants.LocalTransactionStats;
import org.throwable.common.constants.SendStats;
import org.throwable.common.model.TransactionCallbackResult;
import org.throwable.common.model.TransactionSendResult;
import org.throwable.configuration.OnsClientProperties;

import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/26 15:45
 */
@Slf4j
public class TransactionMessageProducer implements EnvironmentAware, BeanFactoryAware {


	private static final long DEFAULT_CONFIRM_TIMEOUT_SECONDS = 5;
	private static final long DEFAULT_FIRE_TRANSACTION_TIMEOUT_SECONDS = 15;
	private static final long DEFAULT_EXECUTE_TRANSACTION_TIMEOUT_SECONDS = 15;
	private String halfMessageQueue;
	private volatile RabbitmqMessagePropertiesConverter converter = new RabbitmqMessagePropertiesConverter();
	private DefaultListableBeanFactory beanFactory;

	@Autowired
	private TransactionThreadPoolProvider transactionThreadPoolProvider;

	@Override
	public void setEnvironment(Environment environment) {
		this.halfMessageQueue = environment.getProperty(OnsClientProperties.HALFMESSAGE_QUEUE_KEY,
				OnsClientProperties.DEFAULT_HALFMESSAGE_QUEUE);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (DefaultListableBeanFactory) beanFactory;
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
														  final Class<? extends LocalTransactionChecker> checkerClass,
														  final String destinationQueue,
														  final String destinationExchange,
														  final String destinationExchangeType,
														  final String destinationRoutingKey) {
		return sendMessageInTransaction(messageId, uniqueCode, message, executor, checkerClass,
				DEFAULT_CONFIRM_TIMEOUT_SECONDS, DEFAULT_FIRE_TRANSACTION_TIMEOUT_SECONDS,
				DEFAULT_EXECUTE_TRANSACTION_TIMEOUT_SECONDS,
				destinationQueue, destinationExchange, destinationExchangeType, destinationRoutingKey, null);
	}

	public TransactionSendResult sendMessageInTransaction(final String messageId,
														  final String uniqueCode,
														  final Message message,
														  final LocalTransactionExecutor executor,
														  final Class<? extends LocalTransactionChecker> checkerClass,
														  long confirmTimeoutSeconds,
														  long fireTransactionTimeoutSeconds,
														  long executeTransactionTimeoutSeconds,
														  final String destinationQueue,
														  final String destinationExchange,
														  final String destinationExchangeType,
														  final String destinationRoutingKey,
														  final Map<String, Object> headers) {
		Assert.hasText(messageId, "messageId must not be empty!");
		Assert.hasText(uniqueCode, "uniqueCode must not be empty!");
		Assert.notNull(message, "message must not be null!");
		Assert.notNull(executor, "executor must not be null!");
		Assert.notNull(checkerClass, "checkerClass must not be null!");
		checkRabbitmqArgumentsValid(destinationQueue, destinationExchange,destinationExchangeType ,destinationRoutingKey, headers);
		checkTransactionCheckerValid(checkerClass);
		TransactionSendResult sendResult = new TransactionSendResult();
		sendResult.setSendStats(SendStats.PREPARE);
		sendResult.setMessageId(messageId);
		sendResult.setUniqueCode(uniqueCode);
		sendResult.setLocalTransactionStats(LocalTransactionStats.UNKNOWN);
		LocalTransactionExecutionSynchronizer.addTransactionExecutor(uniqueCode, executor);
		BlockingLocalTransactionExecutorConsumer consumer = new BlockingLocalTransactionExecutorConsumer(fireTransactionTimeoutSeconds,
				executeTransactionTimeoutSeconds, uniqueCode, transactionThreadPoolProvider.getTransactionExecutor());
		LocalTransactionExecutionSynchronizer.addTransactionConsumer(uniqueCode, consumer);
		try {
			converter.wrapMessageProperties(message, messageId, uniqueCode, destinationQueue, destinationExchange,
					destinationExchangeType, destinationRoutingKey, checkerClass.getName(), headers);
			sendResult.setSendStats(rabbitTemplate.execute(channel -> {
				message.getMessageProperties().setHeader(Constants.LOCALTRANSACTIONSTATS_KEY, sendResult.getLocalTransactionStats());
				message.getMessageProperties().setHeader(Constants.SENDSTATS_KEY, sendResult.getSendStats());
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
					TransactionCallbackResult callbackResult = consumer.processLocalTransactionExecutor();
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
			if (LocalTransactionStats.COMMITTED.equals(sendResult.getLocalTransactionStats()) ||
					LocalTransactionStats.ROLLBACK.equals(sendResult.getLocalTransactionStats())) {
				sendResult.setSendStats(rabbitTemplate.execute(channel -> {
					message.getMessageProperties().setHeader(Constants.LOCALTRANSACTIONSTATS_KEY, sendResult.getLocalTransactionStats());
					message.getMessageProperties().setHeader(Constants.SENDSTATS_KEY, sendResult.getSendStats());
					channel.confirmSelect();
					AMQP.BasicProperties basicProperties = converter.convertToBasicProperties(message.getMessageProperties());
					channel.basicPublish(halfMessageQueue, halfMessageQueue, basicProperties, message.getBody());
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

	private void checkTransactionCheckerValid(Class<? extends LocalTransactionChecker> checkerClass) {
		String[] beanNamesForType = beanFactory.getBeanNamesForType(checkerClass);
		Assert.isTrue(null != beanNamesForType && beanNamesForType.length == 1, "checkerClass must be a bean managed by Spring!");
	}

	private void checkRabbitmqArgumentsValid(String destinationQueue,
											 String destinationExchange,
											 String destinationExchangeType,
											 String destinationRoutingKey,
											 Map<String, Object> headers) {
		Assert.hasText(destinationQueue, "destinationQueue must not be empty!");
		Assert.hasText(destinationExchangeType, "destinationExchangeType must not be empty!");
		Assert.hasText(destinationExchange, "destinationExchange must not be empty!");
		ExchangeEnum exchangeEnum = ExchangeEnum.parse(destinationExchangeType);
		if (ExchangeEnum.DIRECT.equals(exchangeEnum) || ExchangeEnum.TOPIC.equals(exchangeEnum))
			Assert.hasText(destinationRoutingKey, "destinationRoutingKey must not be empty!");
		if (ExchangeEnum.HEADERS.equals(exchangeEnum))
			Assert.notNull(headers, "headers must not be empty!");
	}

}
