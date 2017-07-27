package org.throwable.server.dao;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.throwable.Application;
import org.throwable.server.model.TransactionLog;

import static org.junit.Assert.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 18:53
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class TransactionLogDaoTest {


    @Autowired
    private TransactionLogDao transactionLogDao;

    public void testSave()throws Exception{
        TransactionLog log = new TransactionLog();
        transactionLogDao.save(log, 1L);
    }
}