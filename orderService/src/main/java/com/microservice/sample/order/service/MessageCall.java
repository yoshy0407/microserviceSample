package com.microservice.sample.order.service;

import lombok.Data;

@Data
public class MessageCall {

	private String topicName;
	
	private Object sendEvent;
}
