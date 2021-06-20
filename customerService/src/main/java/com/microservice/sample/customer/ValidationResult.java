package com.microservice.sample.customer;

import lombok.Data;

@Data
public class ValidationResult {

	private String transactionId;
	
	private boolean success;
	
	private String message;
}
