package com.microservice.sample.order.service.event;

import lombok.Data;

@Data
public class BookStockResult {

	private String transactionId;
	
	private boolean result;
	
	private String message;
}
