package org.throwable.server.service;

import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.throwable.common.constants.Constants;
import org.throwable.common.constants.LocalTransactionStats;
import org.throwable.common.constants.RejectExecutionHandlerEnum;
import org.throwable.configuration.OnsServerProperties;
import org.throwable.server.constants.PushStats;
import org.throwable.server.dao.TransactionLogDao;
import org.throwable.server.dao.TransactionMessageDao;
import org.throwable.server.model.TransactionLog;
import org.throwable.server.model.TransactionMessage;
import org.throwable.server.task.TransactionMessagePushStatsInspectionTaskDispatcher;
import org.throwable.support.TransactionTemplateProvider;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/29 22:15
 */
@Service
@Slf4j
public class TransactionMessagePushStatsInspectionService extends AbstractRabbitmqSupportableService
		implements InitializingBean, BeanFactoryAware {

	private TransactionMessagePushStatsInspectionTaskDispatcher dispatcher;
	private Integer confirmTimeoutSeconds;

	@Autowired
	private OnsServerProperties onsServerProperties;

	@Autowired
	private TransactionLogDao transactionLogDao;

	@Autowired
	private TransactionMessageDao transactionMessageDao;

	@Autowired
	private TransactionTemplateProvider transactionTemplateProvider;

	private DefaultListableBeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (DefaultListableBeanFactory) beanFactory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.confirmTimeoutSeconds = onsServerProperties.getConfirmTimeoutSeconds();
		this.dispatcher = new TransactionMessagePushStatsInspectionTaskDispatcher(
				onsServerProperties.getConcurrentPushStatsInspectionWorkerNumber(),
				onsServerProperties.getMaxPushStatsInspectionWorkerNumber(),
				onsServerProperties.getPushStatsInspectionQueueCapacity(),
				onsServerProperties.getPushStatsInspectionKeepAliveSeconds(),
				RejectExecutionHandlerEnum.parse(onsServerProperties.getPushStatsInspectionRejectExecutionHandlerType()),
				onsServerProperties.getPushStatsInspectionWorkerPrefix()
		);
		beanFactory.registerSingleton(Constants.TRANSACTIONMESSAGEPUSHSTATSINSPECTIONTASKDISPATCHER_BEANNAME, dispatcher);
		this.dispatcher = beanFactory.getBean(Constants.TRANSACTIONMESSAGEPUSHSTATSINSPECTIONTASKDISPATCHER_BEANNAME,
				TransactionMessagePushStatsInspectionTaskDispatcher.class);
	}

	public void doPushStatsInspection() {
		int pageIndex = 1;
		int pageSize = onsServerProperties.getPushStatsInspectionBatchSize();
		int maxPushAttemptTime = onsServerProperties.getMaxPushStatsInspectionAttemptTime();
		int intervalSeconds = onsServerProperties.getPushStatsInspectionIntervalSeconds();
		Date delta = new Date(System.currentTimeMillis() - intervalSeconds * 1000);
		int recordSize;
		do {
			List<TransactionLog> records = transactionLogDao.queryRecordsToPush(LocalTransactionStats.COMMITTED.toString(),
					PushStats.INIT.toString(),
					PushStats.FAIL.toString(),
					(pageIndex - 1) * pageSize,
					pageSize,
					delta,
					maxPushAttemptTime);
			if (null == records || records.isEmpty()) {
				recordSize = 0;
			} else {
				recordSize = records.size();
				this.dispatcher.submit(createPushStatsInspectionTask(records));
				pageIndex++;
			}
		} while (recordSize > 0);
	}

	private Callable<Void> createPushStatsInspectionTask(final List<TransactionLog> records) {
		return new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				for (TransactionLog transactionLog : records) {
					PushStats pushStatsCallback
							= rabbitTemplate.execute(channel -> {
						TransactionMessage transactionMessage = transactionMessageDao.fetchById(transactionLog.getTransactionMessageId());
						if (null != transactionMessage) {
							String queue = transactionMessage.getQueue();
							String exchange = transactionMessage.getExchange();
							String routingKey = transactionMessage.getRoutingKey();
							String exchangeType = transactionMessage.getExchangeType();
							String headers = transactionMessage.getHeaders();
							declareIfNecessary(queue, exchange, routingKey, exchangeType, headers);
							MessagePropertiesBuilder builder = MessagePropertiesBuilder.newInstance();
							builder.setHeader(Constants.UNIQUECODE_KEY, transactionLog.getUniqueCode());
							channel.confirmSelect();
							AMQP.BasicProperties basicProperties = converter.convertToBasicProperties(builder.build());
							channel.basicPublish(exchange, routingKey, basicProperties,
									transactionMessage.getContent().getBytes(Constants.ENCODING));
							if (channel.waitForConfirms(confirmTimeoutSeconds * 1000)) {
								return PushStats.SUCCESS;
							}
							if (log.isWarnEnabled()) {
								log.warn(String.format("Publish and Confirm message for pushStats inspection failed,uniqueCode:%s,transactionId:%s",
										transactionLog.getUniqueCode(), transactionLog.getTransactionId()));
							}
							return PushStats.FAIL;
						}
						return PushStats.FAIL;
					});
					transactionLog.setPushStats(pushStatsCallback.toString());
					transactionLog.setPushTime(new Date());
					transactionLog.setPushAttemptTime(transactionLog.getPushAttemptTime() + 1);
				}
				transactionTemplateProvider.getTransactionTemplate(TransactionDefinition.PROPAGATION_REQUIRES_NEW,
						TransactionDefinition.ISOLATION_READ_COMMITTED)
						.execute(status -> transactionLogDao.batchUpdatePushStats(records));
				return null;
			}
		};
	}
}
