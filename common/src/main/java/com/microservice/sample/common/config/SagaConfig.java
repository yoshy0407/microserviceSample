package com.microservice.sample.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.microservice.sample.common.TransactionIdRegistry;
import com.microservice.sample.common.saga.SagaManager;
import com.microservice.sample.common.saga.impl.SagaManagerImpl;

@Configuration
public class SagaConfig {

	@Bean
	public SagaManager sagaManager(TransactionIdRegistry registry, 
			ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate, ThreadPoolTaskExecutor threadPool) {
		return new SagaManagerImpl(registry, kafkaTemplate, threadPool);
	}
	
	@Bean
	public ThreadPoolTaskExecutor threadPool() {
		ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
		threadPool.initialize();
		return threadPool;
	}
}
