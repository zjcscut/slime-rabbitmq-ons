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
		this.halfMessageQueue = environment.getProperty(OnsProperties.HALFMESSAGE_QUEUE_KEY, OnsProperties.DEFAULT_HALFMESSAGE_QUEUE);
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
														  long executeTimeoutSeconds,
														  final String queue,
														  final String exchange,
														  final String routingKey) {
		return sendMessageInTransaction(messageId, uniqueCode, message, executor,
				DEFAULT_CONFIRM_TIMEOUT_SECONDS, executeTimeoutSeconds, queue, exchange, routingKey, null);
	}

	public TransactionSendResult sendMessageInTransaction(final String messageId,
														  final String uniqueCode,
														  final Message message,
														  final LocalTransactionExecutor executor,
														  long confirmTimeoutSeconds,
														  long executeTimeoutSeconds,
														  final String queue,
														  final String exchange,
														  final String routingKey,
														  final String args) {
		Assert.hasText(queue);
		Assert.hasText(exchange);
		Assert.hasText(routingKey);
		Assert.hasText(messageId);
		Assert.hasText(uniqueCode);
		Assert.notNull(message);
		TransactionSendResult sendResult = new TransactionSendResult();
		sendResult.setSendStats(SendStats.PREPARE);
		sendResult.setMessageId(messageId);
		sendResult.setUniqueCode(uniqueCode);
		sendResult.setLocalTransactionStats(LocalTransactionStats.UNKNOWN);
		BlockingLocalTransactionExecutorConsumer consumer = new BlockingLocalTransactionExecutorConsumer();
		LocalTransactionExecutorSynchronizer.addTransactionExecutor(uniqueCode, executor);
		LocalTransactionExecutorSynchronizer.addTransactionConsumer(uniqueCode, consumer);
		try {
			converter.wrapMessageProperties(message, messageId, uniqueCode, queue, exchange, routingKey, args);
			sendResult.setSendStats(rabbitTemplate.execute(channel -> {
				message.getMessageProperties().setHeader("localTransactionStat", sendResult.getLocalTransactionStats());
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
			if (SendStats.SUCCESS.equals(sendResult.getSendStats())) {
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
			LocalTransactionExecutorSynchronizer.removeTransactionConsumer(uniqueCode);
			LocalTransactionExecutorSynchronizer.removeTransactionExecutor(uniqueCode);
		}
	}


}
