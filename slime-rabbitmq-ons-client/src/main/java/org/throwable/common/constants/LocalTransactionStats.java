package org.throwable.common.constants;

/**
 * @author throwable
 * @version v1.0
 * @description 事务状态枚举
 * @since 2017/7/26 9:29
 */
public enum LocalTransactionStats {

    /**
     * 已提交
     */
    COMMITTED,

    /**
     * 回滚
     */
    ROLLBACK,

    /**
     * 未知状态
     */
    UNKNOWN
}
