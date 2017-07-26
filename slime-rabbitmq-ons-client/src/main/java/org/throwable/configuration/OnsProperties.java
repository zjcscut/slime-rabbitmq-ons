package org.throwable.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/26 15:28
 */
@ConfigurationProperties(prefix = OnsProperties.PREFIX)
public class OnsProperties {

    public static final String PREFIX = "slime.ons";
    public static final String HALFMESSAGE_QUEUE_KEY = PREFIX + ".halfMessageQueue";
    public static final String DEFAULT_HALFMESSAGE_QUEUE = HALFMESSAGE_QUEUE_KEY;

    public static final String TRANSACTIONCALLBACK_QUEUE_KEY = PREFIX + ".transactionCallbackQueue";
    public static final String DEFAULT_TRANSACTIONCALLBACK_QUEUE = TRANSACTIONCALLBACK_QUEUE_KEY;

    private String halfMessageQueue;
    private String transactionCallbackQueue;

    public String getHalfMessageQueue() {
        return halfMessageQueue;
    }

    public void setHalfMessageQueue(String halfMessageQueue) {
        this.halfMessageQueue = halfMessageQueue;
    }

    public String getTransactionCallbackQueue() {
        return transactionCallbackQueue;
    }

    public void setTransactionCallbackQueue(String transactionCallbackQueue) {
        this.transactionCallbackQueue = transactionCallbackQueue;
    }
}
