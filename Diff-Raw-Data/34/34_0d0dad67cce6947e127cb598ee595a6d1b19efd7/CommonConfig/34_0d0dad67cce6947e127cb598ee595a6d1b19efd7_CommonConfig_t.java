 package me.itzgeoff.vidsync.common;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.core.task.TaskExecutor;
 import org.springframework.scheduling.TaskScheduler;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
 
 @Configuration
 @EnableTransactionManagement
 public class CommonConfig {
 
 	@Bean
 	public TaskExecutor taskExecutor() {
 		return new ThreadPoolTaskExecutor();
 	}
 	
 	@Bean
 	public TaskScheduler taskScheduler() {
 		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
 		scheduler.setThreadNamePrefix("ScheduledTask-");
 		return scheduler;
 	}
 	
 	@Bean
 	public MetricRegistry metrics() {
	    return new MetricRegistry();
 	}
 	
 	@Bean
 	public JmxReporter metricsJmxReporter() {
 	    JmxReporter reporter = JmxReporter.forRegistry(metrics()).build();
 	    reporter.start();
         return reporter;
 	}
 }
