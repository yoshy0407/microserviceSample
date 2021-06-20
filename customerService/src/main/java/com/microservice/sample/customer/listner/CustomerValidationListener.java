package com.microservice.sample.customer.listner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.microservice.sample.customer.CustomerValidationDto;
import com.microservice.sample.customer.service.CustomerValidationService;

@Component
public class CustomerValidationListener {

	@Autowired
	private CustomerValidationService service;
		
	@KafkaListener(topics = "customer-validation")
	public void validationCustomer(CustomerValidationDto dto) {
		service.validate(dto);
	}

	@KafkaListener(topics = "customer-validation-rollback")
	public void validationCustomerRollback(String transactionId) {
		service.rollback(transactionId);
	}

	@KafkaListener(topics = "customer-validation-complete")
	public void validationCustomerComplete(CustomerValidationDto dto) {
		service.complete(dto);
	}
}
