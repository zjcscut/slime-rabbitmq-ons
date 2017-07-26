package org.throwable.exception;

/**
 * @author throwable
 * @version v1.0
 * @description 事务中断
 * @since 2017/7/26 10:26
 */
public class LocalTransactionInterruptedException extends RuntimeException{



    public LocalTransactionInterruptedException(String message) {
        super(message);
    }

    public LocalTransactionInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
