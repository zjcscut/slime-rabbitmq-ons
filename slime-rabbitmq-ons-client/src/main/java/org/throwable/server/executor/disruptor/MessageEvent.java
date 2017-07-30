package org.throwable.server.executor.disruptor;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/29 19:03
 */
public class MessageEvent<T> implements ClearableEvent{

	private T message;

	public T getMessage() {
		return message;
	}

	public void setMessage(T message) {
		this.message = message;
	}

	@Override
	public void clear() {
		this.message = null;
	}
}
