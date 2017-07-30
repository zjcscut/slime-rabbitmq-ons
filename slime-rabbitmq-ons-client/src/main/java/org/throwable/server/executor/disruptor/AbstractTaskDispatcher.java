package org.throwable.server.executor.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.util.Assert;
import org.throwable.utils.Pow2;

import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/29 19:15
 */
public class AbstractTaskDispatcher implements Dispatcher<Callable<Void>>, Executor<Void> {

	private final Disruptor<MessageEvent<Callable<Void>>> disruptor;
	private final ExecutorService reserveExecutor;
	private final LongAdder counter = new LongAdder();
	private final int concurrentWorkerNumbers;
	private final Object MONITOR = new Object();
	private boolean start = false;

	public AbstractTaskDispatcher(int workerNumbers,
								  String threadPrefix,
								  int bufferSize,
								  int reserveWorkersNumbers,
								  WaitStrategyType waitStrategyType,
								  CallableTaskHandler... taskHandlers) {
		Assert.isTrue(workerNumbers > 0, "workerNumbers must be larger than 0!");
		Assert.isTrue(reserveWorkersNumbers > 0, "reserveWorkersNumber must be larger than 0!");
		Assert.isTrue(null != taskHandlers && taskHandlers.length >= 1, "taskHandlers must be defined!");
		if (!Pow2.isPowerOfTwo(bufferSize)) {
			bufferSize = Pow2.roundToPowerOfTwo(bufferSize);
		}
		this.concurrentWorkerNumbers = workerNumbers;
		int reserveMaxSize = reserveWorkersNumbers - workerNumbers;
		reserveExecutor = new ThreadPoolExecutor(
				0,
				reserveMaxSize,
				60L,
				TimeUnit.SECONDS,
				new SynchronousQueue<>(),
				new NamedThreadFactory(threadPrefix),
				new ThreadPoolExecutor.CallerRunsPolicy()
		);
		WaitStrategy waitStrategy;
		switch (waitStrategyType) {
			case BLOCKING_WAIT:
				waitStrategy = new BlockingWaitStrategy();
				break;
			case LITE_BLOCKING_WAIT:
				waitStrategy = new LiteBlockingWaitStrategy();
				break;
			case TIMEOUT_BLOCKING_WAIT:
				waitStrategy = new TimeoutBlockingWaitStrategy(1000, TimeUnit.MILLISECONDS);
				break;
			case LITE_TIMEOUT_BLOCKING_WAIT:
				waitStrategy = new LiteTimeoutBlockingWaitStrategy(1000, TimeUnit.MILLISECONDS);
				break;
			case PHASED_BACK_OFF_WAIT:
				waitStrategy = PhasedBackoffWaitStrategy.withLiteLock(1, 1, TimeUnit.MILLISECONDS);
				break;
			case SLEEPING_WAIT:
				waitStrategy = new SleepingWaitStrategy();
				break;
			case YIELDING_WAIT:
				waitStrategy = new YieldingWaitStrategy();
				break;
			case BUSY_SPIN_WAIT:
				waitStrategy = new BusySpinWaitStrategy();
				break;
			default: {
				throw new UnsupportedOperationException("unsupported disruptor waitStrategyType:" + waitStrategyType.toString());
			}
		}
		ThreadFactory threadFactory = new NamedThreadFactory(threadPrefix);
		EventFactory<MessageEvent<Callable<Void>>> eventFactory = MessageEvent::new;
		Disruptor<MessageEvent<Callable<Void>>> disruptorToUse = new Disruptor<>(eventFactory, bufferSize, threadFactory,
				ProducerType.SINGLE, waitStrategy);
		disruptorToUse.setDefaultExceptionHandler(new LoggingExceptionHandler());
		for (CallableTaskHandler handler : taskHandlers) {
			handler.setCounter(counter);
		}
		if (workerNumbers == 1 && taskHandlers.length == 1) {
			disruptorToUse.handleEventsWith(taskHandlers);
		} else {
			disruptorToUse.handleEventsWithWorkerPool(taskHandlers);
		}
		this.disruptor = disruptorToUse;
	}

	@Override
	public void start() {
		synchronized (MONITOR) {
			disruptor.start();
			this.start = true;
		}
	}

	@Override
	public void submit(Callable<Void> callable) {
		if (this.concurrentWorkerNumbers >= counter.intValue()) {
			if (!dispatch(callable)) {
				reserveExecutor.submit(callable);
			}
		} else {
			this.reserveExecutor.submit(callable);
		}
	}

	@Override
	public boolean dispatch(Callable<Void> message) {
		RingBuffer<MessageEvent<Callable<Void>>> ringBuffer = this.disruptor.getRingBuffer();
		try {
			long sequence = ringBuffer.tryNext();
			try {
				MessageEvent<Callable<Void>> event = ringBuffer.get(sequence);
				event.setMessage(message);
			} finally {
				ringBuffer.publish(sequence);
			}
			return true;
		} catch (InsufficientCapacityException e) {
			return false;
		}
	}

	@Override
	public boolean isStart() {
		synchronized (MONITOR) {
			return this.start;
		}
	}

	@Override
	public void shutdown() {
		synchronized (MONITOR) {
			if (null != disruptor && start) {
				start = false;
				disruptor.shutdown();
			}
		}
	}
}
