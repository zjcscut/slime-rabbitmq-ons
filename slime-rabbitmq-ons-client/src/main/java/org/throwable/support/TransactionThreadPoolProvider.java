package org.throwable.support;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.throwable.configuration.OnsProperties;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/28 14:12
 */
@Component
public class TransactionThreadPoolProvider implements InitializingBean {

    private final OnsProperties onsProperties;
    private ThreadPoolExecutor executor;

    public TransactionThreadPoolProvider(OnsProperties onsProperties) {
        this.onsProperties = onsProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.executor = new ThreadPoolExecutor(onsProperties.getTransactionThreadPoolCoreSize(),
                onsProperties.getTransactionThreadPoolMaxSize(),
                onsProperties.getTransactionThreadKeepAliveSeconds(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(onsProperties.getTransactionThreadPoolQueueCapacity()),
                new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName(onsProperties.getTransactionThreadNamePrefix() + "-thread-" + counter.getAndIncrement());
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public ExecutorService getTransactionExecutor(){
        return this.executor;
    }
}
