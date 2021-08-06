package com.microservice.sample.book;

import com.microservice.sample.common.event.AbstractEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BookStockDto extends AbstractEvent{

	
	private Integer bookId;
	
	private Integer reduceCount;
}
