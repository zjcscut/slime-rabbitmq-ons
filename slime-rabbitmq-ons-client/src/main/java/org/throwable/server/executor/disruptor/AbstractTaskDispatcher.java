package org.throwable.server.executor.disruptor;

import org.throwable.common.constants.RejectExecutionHandlerEnum;

import java.util.concurrent.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/29 19:15
 */
public abstract class AbstractTaskDispatcher<T> implements Executor<T> {

	private final ThreadPoolExecutor executor;

	public AbstractTaskDispatcher(int concurrentWorkerNumbers,
								  int maxWorkerNumbers,
								  int queueCapacity,
								  int keepAliveSeconds,
								  RejectExecutionHandlerEnum rejectHandlerType,
								  String threadNamePrefix) {
		RejectedExecutionHandler executionHandler;
		switch (rejectHandlerType) {
			case AbortPolicy:
				executionHandler = new ThreadPoolExecutor.AbortPolicy();
				break;
			case DiscardPolicy:
				executionHandler = new ThreadPoolExecutor.DiscardPolicy();
				break;
			case CallerRunsPolicy:
				executionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
				break;
			case DiscardOldestPolicy:
				executionHandler = new ThreadPoolExecutor.DiscardOldestPolicy();
				break;
			default: {
				executionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
			}
		}
		this.executor = new ThreadPoolExecutor(
				concurrentWorkerNumbers,
				maxWorkerNumbers,
				keepAliveSeconds,
				TimeUnit.SECONDS,
				new ArrayBlockingQueue<>(queueCapacity),
				new NamedThreadFactory(threadNamePrefix),
				executionHandler
		);
	}

	@Override
	public Future<T> submit(Callable<T> callable) {
		return executor.submit(callable);
	}

}
