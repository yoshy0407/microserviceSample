package com.microservice.sample.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.sample.order.OrderRegisterRequest;
import com.microservice.sample.order.RestResult;
import com.microservice.sample.order.service.OrderService;

@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	protected OrderService service;
	
	@PostMapping("/register")
	public RestResult registerOrder(OrderRegisterRequest request){
		return service.register(request);
	}
}
