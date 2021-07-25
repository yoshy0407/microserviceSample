package com.microservice.sample.order;

import java.util.List;

import lombok.Data;

@Data
public class OrderRegisterRequest {

	private Integer customerId;
	
	private List<OrderBook> orderBooks;
}
