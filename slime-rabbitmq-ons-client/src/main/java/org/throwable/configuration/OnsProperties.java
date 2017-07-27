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

	public static final String TRANSACTIONCHECKER_QUEUE_KEY = PREFIX + ".transactionCheckerQueue";
	public static final String DEFAULT_TRANSACTIONCHECKER_QUEUE = TRANSACTIONCHECKER_QUEUE_KEY;

	public static final String FIRETRANSACTION_QUEUE_KEY = PREFIX + ".fireTransactionQueue";
	public static final String DEFAULT_FIRETRANSACTION_QUEUE = FIRETRANSACTION_QUEUE_KEY;

	private String halfMessageQueue = DEFAULT_HALFMESSAGE_QUEUE;
	private String transactionCheckerQueue = DEFAULT_TRANSACTIONCHECKER_QUEUE;
	private String fireTransactionQueue = DEFAULT_FIRETRANSACTION_QUEUE;

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
}
