
package com.iask.seo.task.config;
 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

/**
 *  
 *-----------------------------------------------------------------------------
 * <p>  定时任务-多线并发配置  </p>
 *  
 *  
 * @project name	: seo-run-job
 * @package name	: com.iask.seo.task.config
 * @file name    	: TaskConfig.java
 * @author   		: flm  
 * @date 	 		: 2019年3月29日 
 * <p>Copyright (c)  2018   深圳爱问股份有限公司</p>
 *-----------------------------------------------------------------------------
 */
@Component
public class TaskConfig {
	
	/*
	 * @EnableScheduling TaskScheduler poolSize = 1 默认下是单线程的
	 * 
	 * 当定义多个任务时，无法并发运行，需求重写线程数 ：poolSize = n
	 * 
	 * 建议并发定时任务时，任务数量 < poolSize
	 */
	
	@Value("${poolSize}")
	private int poolSize;
	
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(poolSize);     // 根据实际情况 设置
        return taskScheduler;
    }

}