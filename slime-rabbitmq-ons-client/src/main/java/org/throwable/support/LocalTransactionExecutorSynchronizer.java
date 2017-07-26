package org.throwable.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/26 11:08
 */
public abstract class LocalTransactionExecutorSynchronizer {

	private static final Map<String, BlockingLocalTransactionExecutorConsumer> executorConsumers = new ConcurrentHashMap<>(256);
	private static final Map<String, LocalTransactionExecutor> localTransactionExecutors = new ConcurrentHashMap<>(256);

	public static void addTransactionExecutor(String uniqueCode, LocalTransactionExecutor transactionExecutor) {
		localTransactionExecutors.putIfAbsent(uniqueCode, transactionExecutor);
	}

	public static LocalTransactionExecutor getTransactionExecutor(String uniqueCode) {
		return localTransactionExecutors.get(uniqueCode);
	}

	public static boolean existTransactionExecutor(String uniqueCode) {
		return localTransactionExecutors.containsKey(uniqueCode);
	}

	public static void removeTransactionExecutor(String uniqueCode) {
		localTransactionExecutors.remove(uniqueCode);
	}


	public static void addTransactionConsumer(String uniqueCode, BlockingLocalTransactionExecutorConsumer consumer) {
		executorConsumers.putIfAbsent(uniqueCode, consumer);
	}

	public static BlockingLocalTransactionExecutorConsumer getTransactionConsumer(String uniqueCode) {
		return executorConsumers.get(uniqueCode);
	}

	public static boolean existTransactionConsumer(String uniqueCode) {
		return executorConsumers.containsKey(uniqueCode);
	}

	public static void removeTransactionConsumer(String uniqueCode) {
		executorConsumers.remove(uniqueCode);
	}
}
