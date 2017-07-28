package org.throwable.exception;

/**
 * @author throwable
 * @version v1.0
 * @description 事务执行失败
 * @since 2017/7/26 10:24
 */
public class LocalTransactionExecutionException extends RuntimeException{

    public LocalTransactionExecutionException(String message) {
        super(message);
    }

    public LocalTransactionExecutionException(Throwable cause) {
        super(cause);
    }

    public LocalTransactionExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
