package com.microservice.sample.order.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.sample.order.OrderRegisterRequest;
import com.microservice.sample.order.RestResult;
import com.microservice.sample.order.model.order.OrderResultDto;
import com.microservice.sample.order.service.OrderSearchService;
import com.microservice.sample.order.service.OrderService;

@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	protected OrderService service;
	
	@Autowired
	protected OrderSearchService searchService;
	
	@PostMapping("/register")
	public RestResult registerOrder(OrderRegisterRequest request){
		return service.register(request);
	}

	@GetMapping("/list")
	public List<OrderResultDto> getList(){
		return searchService.selectApiComposition();
	}
}
