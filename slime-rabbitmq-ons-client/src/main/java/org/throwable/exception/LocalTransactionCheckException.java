package org.throwable.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 14:51
 */
public class LocalTransactionCheckException extends RuntimeException {

    public LocalTransactionCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public LocalTransactionCheckException(Throwable cause) {
        super(cause);
    }
}
