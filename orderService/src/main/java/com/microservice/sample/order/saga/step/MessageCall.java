package com.microservice.sample.order.saga.step;

import lombok.Data;

@Data
public class MessageCall {

	private String topicName;
	
	private Object sendEvent;
}
