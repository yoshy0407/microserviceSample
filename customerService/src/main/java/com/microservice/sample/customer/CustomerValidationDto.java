package com.microservice.sample.customer;

import com.microservice.sample.common.event.AbstractEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CustomerValidationDto extends AbstractEvent{

	private Integer customerId;

	private Integer bookCount;
}
