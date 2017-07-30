package org.throwable.server.task;

import org.throwable.server.executor.disruptor.AbstractTaskDispatcher;
import org.throwable.server.executor.disruptor.CallableTaskHandler;
import org.throwable.server.executor.disruptor.WaitStrategyType;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/29 21:30
 */
public class TransactionCheckerFireTaskDisruptor extends AbstractTaskDispatcher {

	public TransactionCheckerFireTaskDisruptor(int workerNumbers,
											   String threadPrefix,
											   int bufferSize,
											   int reserveWorkersNumbers,
											   int keepAliveSeconds,
											   WaitStrategyType waitStrategyType,
											   CallableTaskHandler[] taskHandlers) {
		super(workerNumbers, threadPrefix, bufferSize, reserveWorkersNumbers,keepAliveSeconds, waitStrategyType, taskHandlers);
	}
}
