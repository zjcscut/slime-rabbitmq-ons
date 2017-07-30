package org.throwable.server.task;

import org.throwable.common.constants.RejectExecutionHandlerEnum;
import org.throwable.server.executor.disruptor.AbstractTaskDispatcher;
import org.throwable.server.executor.disruptor.CallableTaskHandler;
import org.throwable.server.executor.disruptor.WaitStrategyType;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/29 21:30
 */
public class TransactionMessagePushStatsInspectionTaskDispatcher extends AbstractTaskDispatcher<Void> {

	public TransactionMessagePushStatsInspectionTaskDispatcher(int concurrentWorkerNumbers,
															   int maxWorkerNumbers,
															   int queueCapacity,
															   int keepAliveSeconds,
															   RejectExecutionHandlerEnum rejectHandlerType,
															   String threadNamePrefix) {
		super(concurrentWorkerNumbers, maxWorkerNumbers, queueCapacity, keepAliveSeconds, rejectHandlerType, threadNamePrefix);
	}
}
