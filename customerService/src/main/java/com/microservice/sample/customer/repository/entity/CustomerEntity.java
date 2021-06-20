package com.microservice.sample.customer.repository.entity;

import org.seasar.doma.Column;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;

import lombok.Data;

@Entity
@Table(name = "CUSTOMER")
@Data
public class CustomerEntity {

	@Id
	@Column(name = "CUSTOMER_ID")
	private Integer customerId;
	
	@Column(name = "CUSTOMER_NAME")
	private String customerName;
	
	@Column(name = "CUSTOMER_NAME_KANA")
	private String customerNameKana;
	
	@Column(name = "CUSTOMER_GENDER")
	private String customerGender;
	
	@Column(name = "BOOK_COUNT")
	private Integer bookCount;
	
	@Column(name = "CUSTOMER_BIRTHDAY")
	private String customerBirthday;
	
	@Column(name = "TX_STATUS")
	private String txStatus;
	
}
