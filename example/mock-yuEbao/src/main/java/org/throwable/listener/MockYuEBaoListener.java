package org.throwable.listener;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.throwable.model.AccountRequest;
import org.throwable.service.MockYuEBaoService;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/31 1:14
 */
@Component
public class MockYuEBaoListener {

	@Autowired
	private MockYuEBaoService mockYuEBaoService;

	@RabbitListener(queues = "alipay->yuEbao", admin = "amqpAdmin", containerFactory = "rabbitListenerContainerFactory")
	public void onMessage(Message message) throws Exception {
		String body = new String(message.getBody(), "UTF-8");
		mockYuEBaoService.processAccountRequest(JSON.parseObject(body, AccountRequest.class));
	}
}
