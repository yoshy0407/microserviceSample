package com.microservice.sample.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer")
public class TestController {

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;
	
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

}
