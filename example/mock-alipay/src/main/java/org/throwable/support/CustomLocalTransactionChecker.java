package org.throwable.support;

import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import org.throwable.common.constants.LocalTransactionStats;

import java.util.Random;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/31 1:32
 */
@Component
public class CustomLocalTransactionChecker implements LocalTransactionChecker{
	private static final Random RANDOM = new Random();

	@Override
	public LocalTransactionStats doInTransactionCheck(Message message) {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			//ignore
		}
		return LocalTransactionStats.values()[RANDOM.nextInt(3)];
	}
}
