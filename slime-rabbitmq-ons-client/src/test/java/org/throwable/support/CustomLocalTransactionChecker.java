package org.throwable.support;

import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import org.throwable.common.constants.LocalTransactionStats;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/28 0:24
 */
@Component
public class CustomLocalTransactionChecker implements LocalTransactionChecker {

	@Override
	public LocalTransactionStats doInTransactionCheck(Message message) {
		return LocalTransactionStats.COMMITTED;
	}
}
