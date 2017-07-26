package org.throwable.exception;

/**
 * @author throwable
 * @version v1.0
 * @description 事务超时
 * @since 2017/7/26 10:25
 */
public class LocalTransactionTimeoutException extends RuntimeException {

    public LocalTransactionTimeoutException(String message) {
        super(message);
    }

    public LocalTransactionTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
