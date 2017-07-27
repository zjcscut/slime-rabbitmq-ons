package org.throwable.support;

import org.springframework.amqp.core.Message;
import org.throwable.common.constants.LocalTransactionStats;

/**
 * @author throwable
 * @version v1.0
 * @description 本地事务检查器
 * @since 2017/7/27 14:28
 */
public interface LocalTransactionChecker {

    LocalTransactionStats doInTransactionCheck(Message message);
}
