package org.throwable.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.throwable.service.MockAlipayService;

import java.util.Random;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/31 1:34
 */
@Component
public class LocalCommandLineRunner implements CommandLineRunner {

	private static final Random RANDOM = new Random();

	@Autowired
	private MockAlipayService mockAlipayService;

	@Override
	public void run(String... args) throws Exception {
		System.out.println("5s后alipay服务将会向yuEbao发起1000笔金额更变请求...");
		Thread.sleep(5000);
		for (int i = 0; i < 1000; i++) {  //模拟发起100次付款
			mockAlipayService.process((long) i, (long) i * RANDOM.nextInt(10) + i);
		}
	}
}
