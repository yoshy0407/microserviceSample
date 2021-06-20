package com.microservice.sample.customer;

import lombok.Data;

@Data
public class CustomerValidationDto {

	private String transactionId;
	
	private Integer customerId;

	private Integer bookCount;
}
