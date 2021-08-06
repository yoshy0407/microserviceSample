package com.microservice.sample.order.model.book;


import lombok.Data;

@Data
public class BookData {

	private Integer bookId;
	
	private String bookName;
	
	private Integer bookStock;
	
	private String txStatus;

}
