package com.microservice.sample.customer.listner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.microservice.sample.common.TransactionIdRegistry;
import com.microservice.sample.customer.CustomerTopicConstant;
import com.microservice.sample.customer.CustomerValidationDto;
import com.microservice.sample.customer.service.CustomerValidationService;

@Component
public class CustomerValidationListener {

	@Autowired
	private CustomerValidationService service;
	
	@Autowired
	private TransactionIdRegistry registry;
		
	@KafkaListener(topics = CustomerTopicConstant.CUSTOMER)
	public void validationCustomer(CustomerValidationDto dto) {
		if (registry.validateId(CustomerTopicConstant.CUSTOMER, dto.getTransactionId())) {
			service.validate(dto);			
		}
	}

	@KafkaListener(topics = CustomerTopicConstant.CUSTOMER_COMPLETE)
	public void validationCustomerRollback(String transactionId) {
		service.rollback(transactionId);
	}

	@KafkaListener(topics = CustomerTopicConstant.CUSTOMER_ROLLBACK)
	public void validationCustomerComplete(CustomerValidationDto dto) {
		if (registry.validateId(CustomerTopicConstant.CUSTOMER_ROLLBACK, dto.getTransactionId())) {
			service.complete(dto);	
		}
	}
}
