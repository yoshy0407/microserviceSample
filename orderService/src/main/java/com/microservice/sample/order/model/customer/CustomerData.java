package com.microservice.sample.order.model.customer;


import lombok.Data;

@Data
public class CustomerData {

	private Integer customerId;
	
	private String customerName;
	
	private String customerNameKana;
	
	private String customerGender;
	
	private Integer bookCount;
	
	private String customerBirthday;
	
	private String txStatus;
	
}
