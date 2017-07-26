package org.throwable.support;

import org.throwable.common.constants.LocalTransactionStats;

/**
 * @author throwable
 * @version v1.0
 * @description 本地事务提交器
 * @since 2017/7/26 9:28
 */
public interface LocalTransactionExecutor{

    LocalTransactionStats doInLocalTransaction();
}
