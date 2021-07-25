package com.microservice.sample.order.service;

public enum StepStatus {
	BEFORE_CALL,
	CALLING,
	SUCCESS,
	ROLLBACK;
}
