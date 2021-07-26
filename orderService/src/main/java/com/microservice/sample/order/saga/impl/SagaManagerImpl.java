package com.microservice.sample.order.saga.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.microservice.sample.order.saga.Saga;
import com.microservice.sample.order.saga.SagaManager;
import com.microservice.sample.order.saga.TransactionIdStrategy;

/**
 * 
 * @author yoshiokahiroshi
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SagaManagerImpl implements SagaManager {

	private ConcurrentHashMap<String, Saga<?>> map = new ConcurrentHashMap<>();
	
	private TransactionIdStrategy strategy;
	
	private KafkaTemplate<String, Object> kafkaTemplate;
	
	public SagaManagerImpl(TransactionIdStrategy strategy, 
			KafkaTemplate<String, Object> kafkaTemplate) {
		this.strategy = strategy;
		this.kafkaTemplate = kafkaTemplate;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <P> String start(P param, Function<P, Saga<P>> sagaFunction) {
		String transactionId = strategy.getNextVal();
		Saga<P> saga = sagaFunction.apply(param);
		saga.setKafkaTemplate(kafkaTemplate);
		map.put(transactionId, saga);
		saga.start();
		return transactionId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Saga<?> get(String transactionId) {
		return map.get(transactionId);
	}

}
