package org.throwable.support;

import org.throwable.common.constants.LocalTransactionStats;
import org.throwable.common.model.TransactionCallbackResult;
import org.throwable.exception.LocalTransactionExecutionException;
import org.throwable.exception.LocalTransactionInterruptedException;
import org.throwable.exception.LocalTransactionTimeoutException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/26 9:32
 */
public class BlockingLocalTransactionExecutorConsumer {

	private final static int DEFAULT_PREFETCH_COUNT = 1;
	private final BlockingQueue<LocalTransactionExecutor> localTransactionExecutors;

	private String transactionId;

	public BlockingLocalTransactionExecutorConsumer() {
		this(DEFAULT_PREFETCH_COUNT);
	}

	public BlockingLocalTransactionExecutorConsumer(int prefetchCount) {
		this.localTransactionExecutors = new LinkedBlockingQueue<>(prefetchCount);
	}

	public void addLocalTransactionExecutor(LocalTransactionExecutor transactionExecutor) {
		try {
			this.localTransactionExecutors.put(transactionExecutor);
		} catch (InterruptedException e) {
			throw new LocalTransactionInterruptedException("Process addLocalTransactionExecutor interrupted!", e);
		}
	}

	public TransactionCallbackResult processLocalTransactionExecutor(long timeoutSeconds) {
		return processLocalTransactionExecutor(timeoutSeconds, TimeUnit.SECONDS);
	}

	public TransactionCallbackResult processLocalTransactionExecutor(long timeout, TimeUnit unit) {
		LocalTransactionExecutor executor;
		try {
			executor = localTransactionExecutors.poll(timeout, unit);
		} catch (InterruptedException e) {
			throw new LocalTransactionInterruptedException("Process processLocalTransactionExecutor interrupted!", e);
		}
		if (null == executor) {
			throw new LocalTransactionTimeoutException("Process processLocalTransactionExecutor timeout!");
		}
		try {
			TransactionCallbackResult callbackResult = new TransactionCallbackResult();
			LocalTransactionStats localTransactionStats = executor.doInLocalTransaction();
			callbackResult.setLocalTransactionStats(localTransactionStats);
			callbackResult.setTransactionId(getTransactionId());
			return callbackResult;
		} catch (Exception e) {
			throw new LocalTransactionExecutionException("Process processLocalTransactionExecutor failed!", e);
		}
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
}
