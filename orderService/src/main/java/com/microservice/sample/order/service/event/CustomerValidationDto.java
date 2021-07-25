package com.microservice.sample.order.service.event;

import lombok.Data;

@Data
public class CustomerValidationDto {

	private String transactionId;
	
	private Integer customerId;

	private Integer bookCount;
}
