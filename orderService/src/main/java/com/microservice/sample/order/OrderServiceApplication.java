package com.microservice.sample.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.microservice.sample.common.config.ApiCompositionConfig;
import com.microservice.sample.common.config.SagaConfig;

@SpringBootApplication
@Import({ApiCompositionConfig.class})
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}
