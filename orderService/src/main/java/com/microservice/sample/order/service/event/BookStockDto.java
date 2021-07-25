package com.microservice.sample.order.service.event;

import lombok.Data;

@Data
public class BookStockDto {

	private String transactionId;
	
	private Integer bookId;
	
	private Integer reduceCount;
}
