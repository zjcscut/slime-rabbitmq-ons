package org.throwable.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/26 16:18
 */
public class SendMqMessageException extends RuntimeException {

    public SendMqMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public SendMqMessageException(Throwable cause) {
        super(cause);
    }
}
