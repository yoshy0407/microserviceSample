package com.microservice.sample.order.service;

public enum SagaStatus {
	EXECUTING,
	COMPLETE,
	ROLLBACK
}
