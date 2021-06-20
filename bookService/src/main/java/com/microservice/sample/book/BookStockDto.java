package com.microservice.sample.book;

import lombok.Data;

@Data
public class BookStockDto {

	private String transactionId;
	
	private Integer bookId;
	
	private Integer reduceCount;
}
