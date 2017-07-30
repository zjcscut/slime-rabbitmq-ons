package org.throwable.server.configuration;

import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.util.Assert;
import org.throwable.common.constants.Constants;
import org.throwable.configuration.OnsServerProperties;
import org.throwable.server.task.TransactionCheckerFireTask;
import org.throwable.server.task.TransactionMessagePushStatsInspectionTask;

import java.util.Properties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/29 10:27
 */
@Configuration
@EnableConfigurationProperties(value = OnsServerProperties.class)
public class OnsServerConfiguration {

	private final OnsServerProperties onsServerProperties;

	public OnsServerConfiguration(OnsServerProperties onsServerProperties) {
		this.onsServerProperties = onsServerProperties;
	}

	@Bean
	public SchedulerFactoryBean scheduler(@Qualifier(Constants.FIRETRANSACTIONCHECKERTRIGGER_KEY) SimpleTrigger fireTransactionCheckerTrigger,
										  @Qualifier(Constants.PUSHSTATSINSPECTIONTRIGGER_KEY) SimpleTrigger pushStatsInspectionTrigger) throws Exception {
		Resource resource = new ClassPathResource("quartz.properties");
		Assert.isTrue(resource.exists(), "quartz.properties must be defined in classpath!");
		SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		schedulerFactory.setApplicationContextSchedulerContextKey(Constants.APPLICATIONCONTEXT_KEY);
		schedulerFactory.setAutoStartup(true); //容器启动完毕自动启动调度器
		schedulerFactory.setStartupDelay(onsServerProperties.getSchedulerStartDelaySeconds());  //容器启动完毕延时X秒启动调度器
		schedulerFactory.setOverwriteExistingJobs(true); //true:覆盖数据库中的job,要自行重新装载;false:以数据库中已经存在的job为准
		schedulerFactory.setConfigLocation(resource); //设置quartz.properties资源,demo暂时用内存,不需设置
		Properties properties = new Properties();
		PropertiesLoaderUtils.fillProperties(properties, resource);
		schedulerFactory.setApplicationContextSchedulerContextKey(Constants.APPLICATIONCONTEXT_KEY);
		schedulerFactory.setSchedulerName(properties.getProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME)); //spring的一个bug,quartz.properties的instanceName不生效
		schedulerFactory.setTriggers(fireTransactionCheckerTrigger, pushStatsInspectionTrigger);
		return schedulerFactory;
	}

	@Bean
	public JobDetailFactoryBean fireTransactionCheckerJob() {
		JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();
		jobDetail.setJobClass(TransactionCheckerFireTask.class);
		jobDetail.setName(Constants.FIRETRANSACTIONCHECKERJOB_KEY);
		jobDetail.setGroup(Constants.QUARTZ_JOB_GROUP);
		jobDetail.setApplicationContextJobDataKey(Constants.APPLICATIONCONTEXT_KEY);
		return jobDetail;
	}

	@Bean
	public JobDetailFactoryBean pushStatsInspectionJob() {
		JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();
		jobDetail.setJobClass(TransactionMessagePushStatsInspectionTask.class);
		jobDetail.setName(Constants.PUSHSTATSINSPECTIONJOB_KEY);
		jobDetail.setGroup(Constants.QUARTZ_JOB_GROUP);
		jobDetail.setApplicationContextJobDataKey(Constants.APPLICATIONCONTEXT_KEY);
		return jobDetail;
	}

	@Bean
	public SimpleTriggerFactoryBean fireTransactionCheckerTrigger(@Qualifier(Constants.FIRETRANSACTIONCHECKERJOB_KEY) JobDetail fireTransactionCheckerJob) {
		SimpleTriggerFactoryBean simpleTrigger = new SimpleTriggerFactoryBean();
		simpleTrigger.setJobDetail(fireTransactionCheckerJob);
		simpleTrigger.setRepeatInterval(onsServerProperties.getCheckerFireIntervalSeconds() * 1000);
		simpleTrigger.setStartDelay(onsServerProperties.getCheckerFireTaskStartDelaySeconds());
		simpleTrigger.setName(Constants.FIRETRANSACTIONCHECKERTRIGGER_KEY);
		simpleTrigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
		return simpleTrigger;
	}

	@Bean
	public SimpleTriggerFactoryBean pushStatsInspectionTrigger(@Qualifier(Constants.PUSHSTATSINSPECTIONJOB_KEY) JobDetail pushStatsInspectionJob) {
		SimpleTriggerFactoryBean simpleTrigger = new SimpleTriggerFactoryBean();
		simpleTrigger.setJobDetail(pushStatsInspectionJob);
		simpleTrigger.setRepeatInterval(onsServerProperties.getPushStatsInspectionIntervalSeconds() * 1000);
		simpleTrigger.setStartDelay(onsServerProperties.getPushStatsInspectionTaskStartDelaySeconds());
		simpleTrigger.setName(Constants.PUSHSTATSINSPECTIONTRIGGER_KEY);
		simpleTrigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
		return simpleTrigger;
	}
}
