package org.throwable.server.executor.disruptor;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.TimeoutHandler;
import com.lmax.disruptor.WorkHandler;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.throwable.common.constants.Constants;
import org.throwable.server.executor.disruptor.MessageEvent;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/29 21:35
 */
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component(value = Constants.CALLABLETASKHANDLER_BEANNAME)
public class CallableTaskHandler implements EventHandler<MessageEvent<Callable<Void>>>,
		WorkHandler<MessageEvent<Callable<Void>>>, TimeoutHandler, LifecycleAware {

	private LongAdder counter;

	@Override
	public void onEvent(MessageEvent<Callable<Void>> event) throws Exception {
		getCounter().increment();
		try {
			event.getMessage().call();
		} finally {
			event.clear();
			getCounter().decrement();
		}
	}

	@Override
	public void onEvent(MessageEvent<Callable<Void>> event, long sequence, boolean endOfBatch) throws Exception {
		getCounter().increment();
		try {
			event.getMessage().call();
		} finally {
			event.clear();
			getCounter().decrement();
		}
	}

	@Override
	public void onStart() {

	}

	@Override
	public void onShutdown() {

	}

	@Override
	public void onTimeout(long sequence) throws Exception {

	}

	public LongAdder getCounter() {
		return counter;
	}

	public void setCounter(LongAdder counter) {
		this.counter = counter;
	}
}
