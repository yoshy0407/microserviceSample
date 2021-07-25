package com.microservice.sample.order.repository.entity;

import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.jdbc.entity.NamingType;

import lombok.Data;

@Data
@Entity(naming = NamingType.SNAKE_UPPER_CASE)
@Table(name = "ORDER_DETAIL")
public class OrderDetailEntity {

	@Id
	private Integer orderId;
	
	private Integer orderAsc;
	
	private Integer bookId;
	
	private Integer orderCount;
	
	private String txStatus;
}
