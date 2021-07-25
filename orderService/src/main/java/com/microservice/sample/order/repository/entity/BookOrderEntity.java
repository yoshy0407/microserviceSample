package com.microservice.sample.order.repository.entity;

import java.time.LocalDateTime;

import org.seasar.doma.Entity;
import org.seasar.doma.GeneratedValue;
import org.seasar.doma.GenerationType;
import org.seasar.doma.Id;
import org.seasar.doma.SequenceGenerator;
import org.seasar.doma.Table;
import org.seasar.doma.jdbc.entity.NamingType;

import lombok.Data;

@Data
@Entity(naming = NamingType.SNAKE_UPPER_CASE)
@Table(name = "BOOK_ORDER")
public class BookOrderEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(sequence = "ORDER_ID_SEQ")
	private Integer orderId;
	
	private Integer customerId;
	
	private LocalDateTime orderDate;
	
	private String txStatus;
}
