package org.throwable.exception;

/**
 * @author throwable
 * @version v1.0
 * @description 事务执行超时
 * @since 2017/7/26 10:24
 */
public class LocalTransactionExecutionTimeoutException extends RuntimeException{

    public LocalTransactionExecutionTimeoutException(String message) {
        super(message);
    }

    public LocalTransactionExecutionTimeoutException(Throwable cause) {
        super(cause);
    }

    public LocalTransactionExecutionTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
