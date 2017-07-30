package org.throwable.service;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.throwable.model.AccountRequest;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/31 1:12
 */
@Service
public class MockYuEBaoService implements InitializingBean{

	@Autowired
	private AmqpAdmin amqpAdmin;

	@Override
	public void afterPropertiesSet() throws Exception {
		Queue queue = new Queue("alipay->yuEbao");
		amqpAdmin.declareQueue(queue);
		DirectExchange exchange = new DirectExchange("",true,false,null);
		amqpAdmin.declareExchange(exchange);
		amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("alipay->yuEbao"));
	}

	public boolean processAccountRequest(AccountRequest accountRequest) throws Exception {
		Thread.sleep(200);
		System.out.println(String.format("模拟处理alipay的一笔支付款项的事务,用户id:%s,更变金额(x100):%s",
				accountRequest.getUserId(), accountRequest.getAmount()));
		return true;
	}
}
