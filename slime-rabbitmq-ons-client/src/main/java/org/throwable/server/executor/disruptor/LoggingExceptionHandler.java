
package org.throwable.server.executor.disruptor;

import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;


/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/29 19:01
 */
@Slf4j
public class LoggingExceptionHandler implements ExceptionHandler<Object> {


    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        if (log.isWarnEnabled()) {
            log.warn("Exception processing: {} {}, {}.", sequence, event, ex);
        }
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        if (log.isWarnEnabled()) {
            log.warn("Exception during onStart(), {}.", ex);
        }
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        if (log.isWarnEnabled()) {
            log.warn("Exception during onShutdown(), {}.", ex);
        }
    }
}
