package com.microservice.sample.customer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.sample.customer.repository.dao.CustomerDao;
import com.microservice.sample.customer.repository.entity.CustomerEntity;

@RestController
@RequestMapping("/customer")
public class CustomerSearchController {

	@Autowired
	protected CustomerDao dao;
	
	@GetMapping("/list")
	public List<CustomerEntity> get(CustomerEntity entity) {
		return dao.getAll(entity);
	}
}
