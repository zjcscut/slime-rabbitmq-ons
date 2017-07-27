package org.throwable.server.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.throwable.Application;
import org.throwable.server.model.TransactionMessage;

import static org.junit.Assert.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 18:03
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class TransactionMessageDaoTest {

    @Autowired
    private TransactionMessageDao transactionMessageDao;

    @Test
    public void testSave()throws Exception{
        TransactionMessage message = new TransactionMessage();
        message.setQueue("queue");
        message.setExchange("exchange");
        message.setRoutingKey("routingKey");
        message.setContent("content");
        message = transactionMessageDao.save(message);
        System.out.println("id --> " + message.getId());
    }

    @Test
    public void testSelect()throws Exception{
        TransactionMessage target = transactionMessageDao.fetchById(1L);
        System.out.println(target.getContent());
    }

}