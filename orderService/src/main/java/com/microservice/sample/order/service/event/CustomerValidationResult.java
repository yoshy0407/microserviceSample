package com.microservice.sample.order.service.event;

import lombok.Data;

@Data
public class CustomerValidationResult {

	private String transactionId;
	
	private boolean success;
	
	private String message;

}
