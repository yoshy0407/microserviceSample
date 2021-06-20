package com.microservice.sample.book;

import lombok.Data;

@Data
public class BookStockResult {

	private String transactionId;
	
	private boolean result;
	
	private String message;
}
