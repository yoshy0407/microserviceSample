package com.microservice.sample.order.model.order;

import java.time.LocalDateTime;

import org.seasar.doma.Entity;
import org.seasar.doma.jdbc.entity.NamingType;

import lombok.Data;

@Entity(naming = NamingType.SNAKE_UPPER_CASE)
@Data
public class OrderDto {

	private Integer orderId;
	
	private Integer customerId;
	
	private LocalDateTime orderDate;
	
	private Integer orderAsc;
	
	private Integer bookId;
	
	private Integer orderCount;

}
