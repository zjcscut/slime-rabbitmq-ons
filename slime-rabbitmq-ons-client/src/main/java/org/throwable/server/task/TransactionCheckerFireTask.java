package org.throwable.server.task;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.throwable.server.service.TransactionCheckerFireService;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/29 2:25
 */
@Slf4j
@DisallowConcurrentExecution
public class TransactionCheckerFireTask extends QuartzJobBean{

	private ApplicationContext applicationContext;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		if (log.isInfoEnabled()){
			log.info("start to execute task TransactionCheckerFireTask...");
		}
		applicationContext.getBean(TransactionCheckerFireService.class).doFireTransactionChecker();
		if (log.isInfoEnabled()){
			log.info("finish executing task TransactionCheckerFireTask successfully...");
		}
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
