package com.microservice.sample.common.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.microservice.sample.common.api.ApiCompositionBuilder;
import com.microservice.sample.common.api.QueryApiCompositionBuilder;

@Configuration
public class ApiCompositionConfig {

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
	
	@Bean
	public ApiCompositionBuilder apiCompositionBuilder(RestTemplate restTemplate) {
		return new ApiCompositionBuilder(restTemplate);
	}
	
}
