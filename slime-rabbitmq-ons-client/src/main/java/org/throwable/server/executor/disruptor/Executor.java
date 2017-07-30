package org.throwable.server.executor.disruptor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/29 19:12
 */
public interface Executor<T> {

	Future<T> submit(Callable<T> callable);
}
