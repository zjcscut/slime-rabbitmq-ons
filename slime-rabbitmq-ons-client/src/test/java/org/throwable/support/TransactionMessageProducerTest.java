package org.throwable.support;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.throwable.Application;
import org.throwable.common.constants.LocalTransactionStats;
import org.throwable.common.model.TransactionSendResult;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 0:57
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class TransactionMessageProducerTest {

	@Autowired
	private TransactionMessageProducer transactionMessageProducer;

	@Test
	public void testSendMessage() throws Exception {
		for (int i = 0;i < 5000; i ++) {


			String messageId = UUID.randomUUID().toString();
			transactionMessageProducer.sendMessageInTransaction(
					messageId,
					messageId,
					new Message("hello".getBytes(), new MessageProperties()),
					new LocalTransactionExecutor() {
						@Override
						public LocalTransactionStats doInLocalTransaction() {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								//ignore
							}
							return LocalTransactionStats.COMMITED;
						}
					},
					CustomLocalTransactionChecker.class,
					"queue-1",
					"queue-1",
					"queue-1"

			);

		}

		Thread.sleep(Integer.MAX_VALUE);
	}
}