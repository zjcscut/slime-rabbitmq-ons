package org.throwable.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/29 2:19
 */
@Component
@ConfigurationProperties(prefix = OnsServerProperties.PREFIX)
public class OnsServerProperties {

	public static final String PREFIX = "slime.ons.server";

	public static final String HALFMESSAGE_QUEUE_KEY = PREFIX + ".halfMessageQueue";
	public static final String DEFAULT_HALFMESSAGE_QUEUE = HALFMESSAGE_QUEUE_KEY;

	public static final String TRANSACTIONCHECKER_QUEUE_KEY = PREFIX + ".transactionCheckerQueue";
	public static final String DEFAULT_TRANSACTIONCHECKER_QUEUE = TRANSACTIONCHECKER_QUEUE_KEY;

	public static final String FIRETRANSACTION_QUEUE_KEY = PREFIX + ".fireTransactionQueue";
	public static final String DEFAULT_FIRETRANSACTION_QUEUE = FIRETRANSACTION_QUEUE_KEY;
	public static final String CONFIRM_TIMEOUT_SECONDS_KEY = PREFIX + ".confirmTimeoutSeconds";

	private static final Integer DEFAULT_WORKER_NUMBER = Runtime.getRuntime().availableProcessors();
	private static final Integer DEFAULT_QUEUECAPACITY = 100;
	private static final Integer DEFAULT_BATCHSIZE = 100;
	public static final Integer DEFAULT_CONFIRM_TIMEOUT_SECONDS = 5;
	private static final Integer DEFAULT_INTERVAL_SECONDS = 60;
	private static final Integer DEFAULT_SCHEDULER_DELAY_SECONDS = 5;
	private static final Integer DEFAULT_TASK_DELAY_SECONDS = 10;
	private static final String DEFAULT_CHECKERFIREWORKERPREFIX = "checkerFireWorker";
	private static final String DEFAULT_PUSHSTATSINSPECTIONWORKERPREFIX = "pushStatsInspectionWorker";
	private static final String DEFAULT_WAITSTRATEGY = "BLOCKING_WAIT";
	private static final Integer DEFAULT_ATTEMPTTIME = 3;


	private String halfMessageQueue = DEFAULT_HALFMESSAGE_QUEUE;
	private String transactionCheckerQueue = DEFAULT_TRANSACTIONCHECKER_QUEUE;
	private String fireTransactionQueue = DEFAULT_FIRETRANSACTION_QUEUE;
	private Integer confirmTimeoutSeconds = DEFAULT_CONFIRM_TIMEOUT_SECONDS;

	private Integer concurrentCheckerFireWorkerNumber = DEFAULT_WORKER_NUMBER;
	private Integer maxCheckerFireWorkerNumber = DEFAULT_WORKER_NUMBER * 2;
	private Integer checkerFireQueueCapacity = DEFAULT_QUEUECAPACITY;
	private String checkerFireWorkerPrefix = DEFAULT_CHECKERFIREWORKERPREFIX;
	private String checkerFireWorkerWaitStrategy = DEFAULT_WAITSTRATEGY;
	private Integer checkerFireTaskBatchSize = DEFAULT_BATCHSIZE;
	private Integer maxCheckAttemptTime = DEFAULT_ATTEMPTTIME;

	private Integer concurrentPushStatsInspectionWorkerNumber = DEFAULT_WORKER_NUMBER;
	private Integer maxPushStatsInspectionWorkerNumber = DEFAULT_WORKER_NUMBER * 2;
	private Integer pushStatsInspectionQueueCapacity = DEFAULT_QUEUECAPACITY;
	private String pushStatsInspectionWorkerPrefix = DEFAULT_PUSHSTATSINSPECTIONWORKERPREFIX;
	private String pushStatsInspectionWorkerWaitStrategy = DEFAULT_WAITSTRATEGY;
	private Integer pushStatsInspectionBatchSize = DEFAULT_BATCHSIZE;
	private Integer maxPushStatsInspectionAttemptTime = DEFAULT_ATTEMPTTIME;

	private Integer checkerFireIntervalSeconds = DEFAULT_INTERVAL_SECONDS;
	private Integer checkerFireTaskStartDelaySeconds = DEFAULT_TASK_DELAY_SECONDS;
	private Integer pushStatsInspectionIntervalSeconds = DEFAULT_INTERVAL_SECONDS;
	private Integer pushStatsInspectionTaskStartDelaySeconds = DEFAULT_TASK_DELAY_SECONDS;

	private Integer schedulerStartDelaySeconds = DEFAULT_SCHEDULER_DELAY_SECONDS;

	public String getHalfMessageQueue() {
		return halfMessageQueue;
	}

	public void setHalfMessageQueue(String halfMessageQueue) {
		this.halfMessageQueue = halfMessageQueue;
	}

	public String getTransactionCheckerQueue() {
		return transactionCheckerQueue;
	}

	public void setTransactionCheckerQueue(String transactionCheckerQueue) {
		this.transactionCheckerQueue = transactionCheckerQueue;
	}

	public String getFireTransactionQueue() {
		return fireTransactionQueue;
	}

	public void setFireTransactionQueue(String fireTransactionQueue) {
		this.fireTransactionQueue = fireTransactionQueue;
	}

	public Integer getConcurrentCheckerFireWorkerNumber() {
		return concurrentCheckerFireWorkerNumber;
	}

	public void setConcurrentCheckerFireWorkerNumber(Integer concurrentCheckerFireWorkerNumber) {
		this.concurrentCheckerFireWorkerNumber = concurrentCheckerFireWorkerNumber;
	}

	public Integer getMaxCheckerFireWorkerNumber() {
		return maxCheckerFireWorkerNumber;
	}

	public void setMaxCheckerFireWorkerNumber(Integer maxCheckerFireWorkerNumber) {
		this.maxCheckerFireWorkerNumber = maxCheckerFireWorkerNumber;
	}

	public Integer getCheckerFireQueueCapacity() {
		return checkerFireQueueCapacity;
	}

	public void setCheckerFireQueueCapacity(Integer checkerFireQueueCapacity) {
		this.checkerFireQueueCapacity = checkerFireQueueCapacity;
	}

	public Integer getConcurrentPushStatsInspectionWorkerNumber() {
		return concurrentPushStatsInspectionWorkerNumber;
	}

	public void setConcurrentPushStatsInspectionWorkerNumber(Integer concurrentPushStatsInspectionWorkerNumber) {
		this.concurrentPushStatsInspectionWorkerNumber = concurrentPushStatsInspectionWorkerNumber;
	}

	public Integer getMaxPushStatsInspectionWorkerNumber() {
		return maxPushStatsInspectionWorkerNumber;
	}

	public void setMaxPushStatsInspectionWorkerNumber(Integer maxPushStatsInspectionWorkerNumber) {
		this.maxPushStatsInspectionWorkerNumber = maxPushStatsInspectionWorkerNumber;
	}

	public Integer getPushStatsInspectionQueueCapacity() {
		return pushStatsInspectionQueueCapacity;
	}

	public void setPushStatsInspectionQueueCapacity(Integer pushStatsInspectionQueueCapacity) {
		this.pushStatsInspectionQueueCapacity = pushStatsInspectionQueueCapacity;
	}

	public Integer getConfirmTimeoutSeconds() {
		return confirmTimeoutSeconds;
	}

	public void setConfirmTimeoutSeconds(Integer confirmTimeoutSeconds) {
		this.confirmTimeoutSeconds = confirmTimeoutSeconds;
	}

	public Integer getCheckerFireIntervalSeconds() {
		return checkerFireIntervalSeconds;
	}

	public void setCheckerFireIntervalSeconds(Integer checkerFireIntervalSeconds) {
		Assert.isTrue(checkerFireIntervalSeconds > 0, "checkerFireIntervalSeconds cannot be negative");
		this.checkerFireIntervalSeconds = checkerFireIntervalSeconds;
	}

	public Integer getPushStatsInspectionIntervalSeconds() {
		return pushStatsInspectionIntervalSeconds;
	}

	public void setPushStatsInspectionIntervalSeconds(Integer pushStatsInspectionIntervalSeconds) {
		Assert.isTrue(pushStatsInspectionIntervalSeconds > 0, "pushStatsInspectionIntervalSeconds cannot be negative");
		this.pushStatsInspectionIntervalSeconds = pushStatsInspectionIntervalSeconds;
	}

	public Integer getCheckerFireTaskStartDelaySeconds() {
		return checkerFireTaskStartDelaySeconds;
	}

	public void setCheckerFireTaskStartDelaySeconds(Integer checkerFireTaskStartDelaySeconds) {
		Assert.isTrue(checkerFireTaskStartDelaySeconds > 0, "checkerFireTaskStartDelaySeconds cannot be negative");
		this.checkerFireTaskStartDelaySeconds = checkerFireTaskStartDelaySeconds;
	}

	public Integer getPushStatsInspectionTaskStartDelaySeconds() {
		return pushStatsInspectionTaskStartDelaySeconds;
	}

	public void setPushStatsInspectionTaskStartDelaySeconds(Integer pushStatsInspectionTaskStartDelaySeconds) {
		Assert.isTrue(pushStatsInspectionTaskStartDelaySeconds > 0, "pushStatsInspectionTaskStartDelaySeconds cannot be negative");
		this.pushStatsInspectionTaskStartDelaySeconds = pushStatsInspectionTaskStartDelaySeconds;
	}

	public Integer getSchedulerStartDelaySeconds() {
		return schedulerStartDelaySeconds;
	}

	public void setSchedulerStartDelaySeconds(Integer schedulerStartDelaySeconds) {
		Assert.isTrue(schedulerStartDelaySeconds > 0, "schedulerStartDelaySeconds cannot be negative");
		this.schedulerStartDelaySeconds = schedulerStartDelaySeconds;
	}

	public String getCheckerFireWorkerPrefix() {
		return checkerFireWorkerPrefix;
	}

	public void setCheckerFireWorkerPrefix(String checkerFireWorkerPrefix) {
		this.checkerFireWorkerPrefix = checkerFireWorkerPrefix;
	}

	public String getPushStatsInspectionWorkerPrefix() {
		return pushStatsInspectionWorkerPrefix;
	}

	public void setPushStatsInspectionWorkerPrefix(String pushStatsInspectionWorkerPrefix) {
		this.pushStatsInspectionWorkerPrefix = pushStatsInspectionWorkerPrefix;
	}

	public String getCheckerFireWorkerWaitStrategy() {
		return checkerFireWorkerWaitStrategy;
	}

	public void setCheckerFireWorkerWaitStrategy(String checkerFireWorkerWaitStrategy) {
		this.checkerFireWorkerWaitStrategy = checkerFireWorkerWaitStrategy;
	}

	public String getPushStatsInspectionWorkerWaitStrategy() {
		return pushStatsInspectionWorkerWaitStrategy;
	}

	public void setPushStatsInspectionWorkerWaitStrategy(String pushStatsInspectionWorkerWaitStrategy) {
		this.pushStatsInspectionWorkerWaitStrategy = pushStatsInspectionWorkerWaitStrategy;
	}

	public Integer getCheckerFireTaskBatchSize() {
		return checkerFireTaskBatchSize;
	}

	public void setCheckerFireTaskBatchSize(Integer checkerFireTaskBatchSize) {
		Assert.isTrue(checkerFireTaskBatchSize > 0, "checkerFireTaskBatchSize cannot be negative");
		this.checkerFireTaskBatchSize = checkerFireTaskBatchSize;
	}

	public Integer getPushStatsInspectionBatchSize() {
		return pushStatsInspectionBatchSize;
	}

	public void setPushStatsInspectionBatchSize(Integer pushStatsInspectionBatchSize) {
		Assert.isTrue(pushStatsInspectionBatchSize > 0, "pushStatsInspectionBatchSize cannot be negative");
		this.pushStatsInspectionBatchSize = pushStatsInspectionBatchSize;
	}

	public Integer getMaxCheckAttemptTime() {
		return maxCheckAttemptTime;
	}

	public void setMaxCheckAttemptTime(Integer maxCheckAttemptTime) {
		Assert.isTrue(maxCheckAttemptTime > 0, "maxCheckAttemptTime cannot be negative");
		this.maxCheckAttemptTime = maxCheckAttemptTime;
	}

	public Integer getMaxPushStatsInspectionAttemptTime() {
		return maxPushStatsInspectionAttemptTime;
	}

	public void setMaxPushStatsInspectionAttemptTime(Integer maxPushStatsInspectionAttemptTime) {
		Assert.isTrue(maxPushStatsInspectionAttemptTime > 0, "maxPushStatsInspectionAttemptTime cannot be negative");
		this.maxPushStatsInspectionAttemptTime = maxPushStatsInspectionAttemptTime;
	}
}
