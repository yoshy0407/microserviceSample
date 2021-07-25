package com.microservice.sample.order.service;

import java.util.List;

import com.microservice.sample.order.OrderBook;

import lombok.Data;

@Data
public class OrderSagaParam {

	private String transactionId;
	
	private Integer customerId;
	
	private List<OrderBook> orderBooks;
}
