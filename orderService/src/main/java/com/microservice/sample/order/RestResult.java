package com.microservice.sample.order;

import java.util.List;

import com.microservice.sample.order.repository.entity.BookOrderEntity;
import com.microservice.sample.order.repository.entity.OrderDetailEntity;

import lombok.Data;

@Data
public class RestResult {

	private int success;
	
	private String message;
	
	private BookOrderEntity result;
	
	private List<OrderDetailEntity> details;
}
