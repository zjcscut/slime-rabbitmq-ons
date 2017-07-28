package org.throwable.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/26 15:28
 */
@Component
@ConfigurationProperties(prefix = OnsProperties.PREFIX)
public class OnsProperties {

    public static final String PREFIX = "slime.ons";
    public static final String HALFMESSAGE_QUEUE_KEY = PREFIX + ".halfMessageQueue";
    public static final String DEFAULT_HALFMESSAGE_QUEUE = HALFMESSAGE_QUEUE_KEY;

    public static final String TRANSACTIONCHECKER_QUEUE_KEY = PREFIX + ".transactionCheckerQueue";
    public static final String DEFAULT_TRANSACTIONCHECKER_QUEUE = TRANSACTIONCHECKER_QUEUE_KEY;

    public static final String FIRETRANSACTION_QUEUE_KEY = PREFIX + ".fireTransactionQueue";
    public static final String DEFAULT_FIRETRANSACTION_QUEUE = FIRETRANSACTION_QUEUE_KEY;

    private static final Integer DEFAULT_CORESIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final Integer DEFAULT_QUEUECAPACITY = 50;
    private static final Integer DEFAULT_KEEPALIVESECOND = 60;
    private static final String DEFAULT_THREADNAMEPREFIX = "smlie-ons";

    private String halfMessageQueue = DEFAULT_HALFMESSAGE_QUEUE;
    private String transactionCheckerQueue = DEFAULT_TRANSACTIONCHECKER_QUEUE;
    private String fireTransactionQueue = DEFAULT_FIRETRANSACTION_QUEUE;


    private Integer transactionThreadPoolCoreSize = DEFAULT_CORESIZE;
    private Integer transactionThreadPoolMaxSize = DEFAULT_CORESIZE * 2;
    private Integer transactionThreadPoolQueueCapacity = DEFAULT_QUEUECAPACITY;
    private Integer transactionThreadKeepAliveSeconds = DEFAULT_KEEPALIVESECOND;
    private String transactionThreadNamePrefix = DEFAULT_THREADNAMEPREFIX;

    public String getHalfMessageQueue() {
        return halfMessageQueue;
    }

    public void setHalfMessageQueue(String halfMessageQueue) {
        this.halfMessageQueue = halfMessageQueue;
    }

    public String getTransactionCheckerQueue() {
        return transactionCheckerQueue;
    }

    public void setTransactionCheckerQueue(String transactionCheckerQueue) {
        this.transactionCheckerQueue = transactionCheckerQueue;
    }

    public String getFireTransactionQueue() {
        return fireTransactionQueue;
    }

    public void setFireTransactionQueue(String fireTransactionQueue) {
        this.fireTransactionQueue = fireTransactionQueue;
    }

    public Integer getTransactionThreadPoolCoreSize() {
        return transactionThreadPoolCoreSize;
    }

    public void setTransactionThreadPoolCoreSize(Integer transactionThreadPoolCoreSize) {
        this.transactionThreadPoolCoreSize = transactionThreadPoolCoreSize;
    }

    public Integer getTransactionThreadPoolMaxSize() {
        return transactionThreadPoolMaxSize;
    }

    public void setTransactionThreadPoolMaxSize(Integer transactionThreadPoolMaxSize) {
        this.transactionThreadPoolMaxSize = transactionThreadPoolMaxSize;
    }

    public Integer getTransactionThreadPoolQueueCapacity() {
        return transactionThreadPoolQueueCapacity;
    }

    public void setTransactionThreadPoolQueueCapacity(Integer transactionThreadPoolQueueCapacity) {
        this.transactionThreadPoolQueueCapacity = transactionThreadPoolQueueCapacity;
    }

    public Integer getTransactionThreadKeepAliveSeconds() {
        return transactionThreadKeepAliveSeconds;
    }

    public void setTransactionThreadKeepAliveSeconds(Integer transactionThreadKeepAliveSeconds) {
        this.transactionThreadKeepAliveSeconds = transactionThreadKeepAliveSeconds;
    }

    public String getTransactionThreadNamePrefix() {
        return transactionThreadNamePrefix;
    }

    public void setTransactionThreadNamePrefix(String transactionThreadNamePrefix) {
        this.transactionThreadNamePrefix = transactionThreadNamePrefix;
    }
}
