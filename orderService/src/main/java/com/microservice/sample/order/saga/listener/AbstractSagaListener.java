package com.microservice.sample.order.saga.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;

import com.microservice.sample.order.saga.SagaManager;
import com.microservice.sample.order.saga.step.SagaStep;
import com.microservice.sample.order.service.event.ResultEvent;

public abstract class AbstractSagaListener<R> {

	@Autowired
	protected SagaManager sagaManager;
	
	protected String stepName;
	
	protected AbstractSagaListener(String stepName) {
		this.stepName = stepName;
	}
	
	@KafkaHandler
	public void consume(R result) {
		String transactionId = getTransactionId(result);
		doConsume(transactionId, result);
	}
	
	protected void doConsume(String transactionId, R result) {
		SagaStep<?> sagaStep = sagaManager.get(transactionId).getStep(stepName);
		ResultEvent resultEvent = new ResultEvent(result);
		
		boolean success = sagaStep.checkResult(resultEvent);
		
		sagaManager.get(transactionId).callNext(success);		
	}
	
	protected abstract String getTransactionId(R result);
}
