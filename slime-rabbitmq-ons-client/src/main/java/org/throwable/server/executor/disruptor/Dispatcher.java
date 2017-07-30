package org.throwable.server.executor.disruptor;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/29 19:01
 */
public interface Dispatcher<T> {

	/**
	 * Dispatch a task message.
	 */
	boolean dispatch(T message);

	void start();

	/**
	 * isStart
	 */
	boolean isStart();

	/**
	 * Shutdown
	 */
	void shutdown();

}
