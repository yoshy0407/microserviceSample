package com.microservice.sample.order.model.order;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrderResultDto {

	private Integer orderId;
	
	private Integer customerId;
	
	private String customerName;
	
	private String customerNameKana;
	
	private LocalDateTime orderDate;
	
	private Integer orderAsc;
	
	private Integer bookId;
	
	private String bookName;
	
	private Integer orderCount;

}
