package com.microservice.sample.order;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class OrderConfig {

	@Bean
	public Executor asyncThreadPool() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setQueueCapacity(5);
        executor.setMaxPoolSize(100);
        executor.setThreadNamePrefix("asyncThread");
        executor.initialize();
        return executor;
	}
}
