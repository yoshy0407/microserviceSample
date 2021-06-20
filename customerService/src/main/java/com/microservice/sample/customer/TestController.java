package com.microservice.sample.customer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.sample.customer.repository.dao.CustomerDao;
import com.microservice.sample.customer.repository.entity.CustomerEntity;

@RestController
@RequestMapping("/customer")
public class TestController {

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;
	
	@Autowired
	private CustomerDao dao;
	
	@GetMapping("validation")
	public String sendValidation(CustomerValidationDto dto) {
		kafkaTemplate.send("customer-validation", dto);
		
		return "sendMessage to validation";
	}

	@GetMapping("rollback")
	public String sendRollback(String transactionId) {
		kafkaTemplate.send("customer-validation-rollback", transactionId);
		
		return "sendMessage to rollback";
	}

	@GetMapping("complete")
	public String sendComplete(CustomerValidationDto dto) {
		kafkaTemplate.send("customer-validation-complete", dto);
		
		return "sendMessage to complete";
	}

	@GetMapping("list")
	public List<CustomerEntity> getEntity(CustomerEntity entity){
		return dao.getAll(entity);
	}
}
