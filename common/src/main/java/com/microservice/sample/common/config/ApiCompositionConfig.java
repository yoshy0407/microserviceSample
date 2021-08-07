package com.microservice.sample.common.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import com.microservice.sample.common.api.composition.ApiComposition;
import com.microservice.sample.common.api.composition.QueryApiComposition;


@Configuration
public class ApiCompositionConfig {

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
	
	@Bean
	public ThreadPoolTaskExecutor threadPool() {
		ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
		threadPool.initialize();
		return threadPool;
	}
	
	@Bean
	public ApiComposition apiComposition(ThreadPoolTaskExecutor threadpool) {
		return new QueryApiComposition(threadpool);
	}
	
}
