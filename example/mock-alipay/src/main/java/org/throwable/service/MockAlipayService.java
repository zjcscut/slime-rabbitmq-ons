package org.throwable.service;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.throwable.common.constants.LocalTransactionStats;
import org.throwable.model.AccountRequest;
import org.throwable.support.CustomLocalTransactionChecker;
import org.throwable.support.LocalTransactionExecutor;
import org.throwable.support.TransactionMessageProducer;

import java.util.Random;
import java.util.UUID;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/31 1:24
 */
@Service
public class MockAlipayService implements InitializingBean {

	@Autowired
	private AmqpAdmin amqpAdmin;

	@Override
	public void afterPropertiesSet() throws Exception {
		Queue queue = new Queue("alipay->yuEbao");
		amqpAdmin.declareQueue(queue);
		DirectExchange exchange = new DirectExchange("", true, false, null);
		amqpAdmin.declareExchange(exchange);
		amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("alipay->yuEbao"));
	}

	@Autowired
	private TransactionMessageProducer transactionMessageProducer;

	private static final Random RANDOM = new Random();

	private static final String QUEUE = "alipay->yuEbao";

	public boolean process(Long uid, Long amount) throws Exception {
		final AccountRequest request = new AccountRequest();
		request.setUserId(uid);
		request.setAmount(amount);
		String messageId = UUID.randomUUID().toString();
		transactionMessageProducer.sendMessageInTransaction(
				messageId,
				messageId,
				new Message(JSON.toJSONString(request).getBytes("UTF-8"), new MessageProperties()),
				new LocalTransactionExecutor() {
					@Override
					public LocalTransactionStats doInLocalTransaction() {
						LocalTransactionStats localTransactionStats = LocalTransactionStats.UNKNOWN;
						try {
							Thread.sleep(100); //模拟本地事务耗时
							localTransactionStats = LocalTransactionStats.values()[RANDOM.nextInt(3)];
							System.out.println("alipay本地事务处理状态 --> " + localTransactionStats);
						} catch (InterruptedException e) {
							//ignore
						}
						return localTransactionStats;
					}
				},
				CustomLocalTransactionChecker.class,
				QUEUE,
				QUEUE,
				"DIRECT",
				QUEUE
		);
		return true;
	}
}
