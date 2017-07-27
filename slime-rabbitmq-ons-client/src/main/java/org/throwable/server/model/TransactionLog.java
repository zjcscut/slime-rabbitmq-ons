package org.throwable.server.model;

import java.util.Date;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 15:37
 */
public class TransactionLog {

    private Long id;
    private Long transactionId;
    private Long transactionMessageId;

    private String messageId;
    private String uniqueCode;

    private String sendStats;
    private String transactionStats;
    private String pushStats;
    private Integer checkAttemptTime;
    private Integer pushAttemptTime;

    private Date createTime;
    private Date updateTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTransactionMessageId() {
        return transactionMessageId;
    }

    public void setTransactionMessageId(Long transactionMessageId) {
        this.transactionMessageId = transactionMessageId;
    }

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

    public String getSendStats() {
        return sendStats;
    }

    public void setSendStats(String sendStats) {
        this.sendStats = sendStats;
    }

    public String getTransactionStats() {
        return transactionStats;
    }

    public void setTransactionStats(String transactionStats) {
        this.transactionStats = transactionStats;
    }

    public String getPushStats() {
        return pushStats;
    }

    public void setPushStats(String pushStats) {
        this.pushStats = pushStats;
    }

    public Integer getCheckAttemptTime() {
        return checkAttemptTime;
    }

    public void setCheckAttemptTime(Integer checkAttemptTime) {
        this.checkAttemptTime = checkAttemptTime;
    }

    public Integer getPushAttemptTime() {
        return pushAttemptTime;
    }

    public void setPushAttemptTime(Integer pushAttemptTime) {
        this.pushAttemptTime = pushAttemptTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }
}
