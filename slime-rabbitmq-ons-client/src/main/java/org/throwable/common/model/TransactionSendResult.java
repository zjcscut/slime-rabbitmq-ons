package org.throwable.common.model;

import org.throwable.common.constants.LocalTransactionStats;
import org.throwable.common.constants.SendStats;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/26 15:55
 */
public class TransactionSendResult {

    private SendStats sendStats;
    private String messageId;
    private String uniqueCode;
    private String transactionId;
    private LocalTransactionStats localTransactionStats;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUniqueCode() {
        return uniqueCode;
    }

    public void setUniqueCode(String uniqueCode) {
        this.uniqueCode = uniqueCode;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalTransactionStats getLocalTransactionStats() {
        return localTransactionStats;
    }

    public void setLocalTransactionStats(LocalTransactionStats localTransactionStats) {
        this.localTransactionStats = localTransactionStats;
    }

    public SendStats getSendStats() {
        return sendStats;
    }

    public void setSendStats(SendStats sendStats) {
        this.sendStats = sendStats;
    }

	@Override
	public String toString() {
		return "TransactionSendResult{" +
				"sendStats=" + sendStats +
				", messageId='" + messageId + '\'' +
				", uniqueCode='" + uniqueCode + '\'' +
				", transactionId='" + transactionId + '\'' +
				", localTransactionStats=" + localTransactionStats +
				'}';
	}
}
