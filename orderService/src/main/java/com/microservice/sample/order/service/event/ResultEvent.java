package com.microservice.sample.order.service.event;

public class ResultEvent {

	private Object result;
	
	public ResultEvent(Object result) {
		this.result = result;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz){
		return (T) result;
	}
}
