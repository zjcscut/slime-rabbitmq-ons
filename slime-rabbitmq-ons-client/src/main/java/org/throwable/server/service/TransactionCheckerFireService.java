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
import org.throwable.common.constants.FireTransactionStats;
import org.throwable.common.constants.LocalTransactionStats;
import org.throwable.common.constants.RejectExecutionHandlerEnum;
import org.throwable.configuration.OnsServerProperties;
import org.throwable.server.dao.TransactionLogDao;
import org.throwable.server.dao.TransactionMessageDao;
import org.throwable.server.model.TransactionLog;
import org.throwable.server.model.TransactionMessage;
import org.throwable.server.task.TransactionCheckerFireTaskDispatcher;
import org.throwable.support.TransactionTemplateProvider;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/29 22:10
 */
@Service
@Slf4j
public class TransactionCheckerFireService extends AbstractRabbitmqSupportableService
		implements InitializingBean, BeanFactoryAware {

	private TransactionCheckerFireTaskDispatcher dispatcher;
	private Integer confirmTimeoutSeconds;
	private String transactionCheckerQueue;

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
		this.transactionCheckerQueue = onsServerProperties.getTransactionCheckerQueue();
		this.dispatcher = new TransactionCheckerFireTaskDispatcher(
				onsServerProperties.getConcurrentCheckerFireWorkerNumber(),
				onsServerProperties.getMaxCheckerFireWorkerNumber(),
				onsServerProperties.getCheckerFireQueueCapacity(),
				onsServerProperties.getCheckerFireWorkerKeepAliveSeconds(),
				RejectExecutionHandlerEnum.parse(onsServerProperties.getCheckerFireWorkerRejectExecutionHandlerType()),
				onsServerProperties.getCheckerFireWorkerPrefix()
		);
		beanFactory.registerSingleton(Constants.TRANSACTIONCHECKERFIRETASKDISPATCHER_BEANNAME, dispatcher);
		this.dispatcher = beanFactory.getBean(Constants.TRANSACTIONCHECKERFIRETASKDISPATCHER_BEANNAME,
				TransactionCheckerFireTaskDispatcher.class);
	}

	public void doFireTransactionChecker() {
		int pageIndex = 1;
		int pageSize = onsServerProperties.getCheckerFireTaskBatchSize();
		int maxCheckAttemptTime = onsServerProperties.getMaxCheckAttemptTime();
		int intervalSeconds = onsServerProperties.getCheckerFireIntervalSeconds();
		Date delta = new Date(System.currentTimeMillis() - intervalSeconds * 1000);
		int recordSize;
		do {
			List<TransactionLog> records = transactionLogDao.queryRecordsToFire(LocalTransactionStats.UNKNOWN.toString(),
					(pageIndex - 1) * pageSize,
					pageSize,
					delta,
					maxCheckAttemptTime);
			if (null == records || records.isEmpty()) {
				recordSize = 0;
			} else {
				recordSize = records.size();
				this.dispatcher.submit(createFireTransactionCheckerTask(records));
				pageIndex++;
			}
		} while (recordSize > 0);
	}

	private Callable<Void> createFireTransactionCheckerTask(final List<TransactionLog> records) {
		return new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				for (TransactionLog transactionLog : records) {
					FireTransactionStats fireTransactionStatsCallback
							= rabbitTemplate.execute(channel -> {
						TransactionMessage transactionMessage = transactionMessageDao.fetchById(transactionLog.getTransactionMessageId());
						if (null != transactionMessage) {
							MessagePropertiesBuilder builder = MessagePropertiesBuilder.newInstance();
							builder.setHeader(Constants.TRANSACTIONID_KEY, transactionLog.getTransactionId());
							builder.setHeader(Constants.QUEUE_KEY, transactionMessage.getQueue());
							builder.setHeader(Constants.EXCHANGE_KEY, transactionMessage.getExchange());
							builder.setHeader(Constants.ROUTINGKEY_KEY, transactionMessage.getRoutingKey());
							builder.setHeader(Constants.MESSAGEID_KEY, transactionLog.getMessageId());
							builder.setHeader(Constants.UNIQUECODE_KEY, transactionLog.getUniqueCode());
							builder.setHeader(Constants.CHECKERCLASSNAME_KEY, transactionLog.getCheckerClassName());
							channel.confirmSelect();
							AMQP.BasicProperties basicProperties = converter.convertToBasicProperties(builder.build());
							channel.basicPublish(transactionCheckerQueue, transactionCheckerQueue, basicProperties,
									transactionMessage.getContent().getBytes(Constants.ENCODING));
							if (channel.waitForConfirms(confirmTimeoutSeconds * 1000)) {
								return FireTransactionStats.SUCCESS;
							}
							if (log.isWarnEnabled()) {
								log.warn(String.format("Publish and Confirm message to fire transaction failed,uniqueCode:%s,transactionId:%s",
										transactionLog.getUniqueCode(), transactionLog.getTransactionId()));
							}
							return FireTransactionStats.FAIL;
						}
						return FireTransactionStats.FAIL;
					});
					transactionLog.setFireTransactionStats(fireTransactionStatsCallback.toString());
					transactionLog.setFireTransactionTime(new Date());
					transactionLog.setCheckAttemptTime(transactionLog.getCheckAttemptTime() + 1);
				}
				transactionTemplateProvider.getTransactionTemplate(TransactionDefinition.PROPAGATION_REQUIRES_NEW,
						TransactionDefinition.ISOLATION_READ_COMMITTED)
						.execute(status -> transactionLogDao.batchUpdateFireTransactionStats(records));
				return null;
			}
		};
	}
}
