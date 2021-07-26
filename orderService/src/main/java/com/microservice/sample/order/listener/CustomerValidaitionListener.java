package com.microservice.sample.order.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.microservice.sample.order.saga.listener.AbstractSagaListener;
import com.microservice.sample.order.service.event.CustomerValidationResult;

@Component
@KafkaListener(topics = "customer-validation-result")
public class CustomerValidaitionListener extends AbstractSagaListener<CustomerValidationResult> {

	protected CustomerValidaitionListener() {
		super("customer-validation");
	}

	@Override
	protected String getTransactionId(CustomerValidationResult result) {
		return result.getTransactionId();
	}

}
