package org.throwable.support;

import org.throwable.common.constants.LocalTransactionStats;
import org.throwable.common.model.TransactionCallbackResult;
import org.throwable.exception.LocalTransactionExecutionException;
import org.throwable.exception.LocalTransactionExecutionTimeoutException;
import org.throwable.exception.LocalTransactionInterruptedException;
import org.throwable.exception.LocalTransactionTimeoutException;

import java.util.concurrent.*;

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
    private long fireTransactionTimeoutSeconds;
    private long executeTransactionTimeoutSeconds;
    private String uniqueCode;
    private ExecutorService executorService;

    public BlockingLocalTransactionExecutorConsumer(long fireTransactionTimeoutSeconds,
                                                    long executeTransactionTimeoutSeconds,
                                                    String uniqueCode,
                                                    ExecutorService executor) {
        this(DEFAULT_PREFETCH_COUNT, fireTransactionTimeoutSeconds, executeTransactionTimeoutSeconds, uniqueCode, executor);
    }

    public BlockingLocalTransactionExecutorConsumer(int prefetchCount,
                                                    long fireTransactionTimeoutSeconds,
                                                    long executeTransactionTimeoutSeconds,
                                                    String uniqueCode,
                                                    ExecutorService executor) {
        this.fireTransactionTimeoutSeconds = fireTransactionTimeoutSeconds;
        this.executeTransactionTimeoutSeconds = executeTransactionTimeoutSeconds;
        this.executorService = executor;
        this.uniqueCode = uniqueCode;
        this.localTransactionExecutors = new LinkedBlockingQueue<>(prefetchCount);
    }

    public void addLocalTransactionExecutor(LocalTransactionExecutor transactionExecutor) {
        try {
            this.localTransactionExecutors.put(transactionExecutor);
        } catch (InterruptedException e) {
            throw new LocalTransactionInterruptedException("Process addLocalTransactionExecutor interrupted!", e);
        }
    }

    public TransactionCallbackResult processLocalTransactionExecutor() {
        return processLocalTransactionExecutor(fireTransactionTimeoutSeconds, executeTransactionTimeoutSeconds, TimeUnit.SECONDS);
    }

    public TransactionCallbackResult processLocalTransactionExecutor(long fireTransactionTimeoutSeconds,
                                                                     long executeTransactionTimeoutSeconds,
                                                                     TimeUnit unit) {
        LocalTransactionExecutor executor;
        try {
            executor = localTransactionExecutors.poll(fireTransactionTimeoutSeconds, unit);
        } catch (InterruptedException e) {
            throw new LocalTransactionInterruptedException(String.format("Fire transactionExecutor interrupted,uniqueCode:%s", uniqueCode), e);
        }
        if (null == executor) {
            throw new LocalTransactionTimeoutException(String.format("Fire transactionExecutor timeout,uniqueCode:%s", uniqueCode));
        }
        try {
            TransactionCallbackResult callbackResult = new TransactionCallbackResult();
            final CompletableFuture<LocalTransactionStats> future = new CompletableFuture<>();
            executorService.submit((Callable<Void>) () -> {
                try {
                    future.complete(executor.doInLocalTransaction());
                } catch (Exception e) {
                    future.completeExceptionally(new LocalTransactionExecutionException(e));
                }
                return null;
            });
            LocalTransactionStats localTransactionStats = future.get(executeTransactionTimeoutSeconds, TimeUnit.SECONDS);
            callbackResult.setLocalTransactionStats(localTransactionStats);
            callbackResult.setTransactionId(getTransactionId());
            return callbackResult;
        } catch (Exception e) {
            handleTransactionException(e, uniqueCode);
        }
        throw new LocalTransactionExecutionException(String.format("Process processLocalTransactionExecutor failed,uniqueCode:%s", uniqueCode));
    }

    private void handleTransactionException(Exception e, String uniqueCode) {
        if (e instanceof InterruptedException) {
            throw new LocalTransactionExecutionException(String.format("Process processLocalTransactionExecutor interrupted,uniqueCode:%s", uniqueCode), e);
        } else if (e instanceof ExecutionException) {
            throw new LocalTransactionExecutionException(String.format("Process processLocalTransactionExecutor failed,uniqueCode:%s", uniqueCode), e);
        } else if (e instanceof TimeoutException) {
            throw new LocalTransactionExecutionTimeoutException(String.format("Process processLocalTransactionExecutor timeout,uniqueCode:%s", uniqueCode), e);
        } else if (e instanceof LocalTransactionExecutionException) {
            throw new LocalTransactionExecutionException(String.format("Process processLocalTransactionExecutor failed,uniqueCode:%s", uniqueCode), e);
        } else {
            throw new LocalTransactionExecutionException(String.format("Process processLocalTransactionExecutor failed,uniqueCode:%s", uniqueCode), e);
        }
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

	public String getUniqueCode() {
		return uniqueCode;
	}
}
