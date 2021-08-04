package com.microservice.sample.common.saga.impl;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import com.microservice.sample.common.TransactionIdRegistry;
import com.microservice.sample.common.saga.AbstractSagaParam;
import com.microservice.sample.common.saga.Saga;
import com.microservice.sample.common.saga.SagaManager;


/**
 * {@link SagaManager}の実装クラスです
 * 
 * @author yoshy0407
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SagaManagerImpl implements SagaManager {

	private ConcurrentHashMap<String, Saga<?>> map = new ConcurrentHashMap<>();
	
	private TransactionIdRegistry transactionIdRegistry;
	
	private ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate;
	
	private ThreadPoolTaskExecutor threadPool;
	
	/**
	 * インスタンスを生成します
	 * 
	 * @param transactionIdRegistry {@link TransactionIdRegistry}
	 * @param kafkaTemplate {@link ReplyingKafkaTemplate}
	 * @param threadPool {@link ThreadPoolTaskExecutor}
	 */
	public SagaManagerImpl(TransactionIdRegistry transactionIdRegistry, 
			ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate,
			ThreadPoolTaskExecutor threadPool) {
		this.transactionIdRegistry = transactionIdRegistry;
		this.kafkaTemplate = kafkaTemplate;
		this.threadPool = threadPool;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <P extends AbstractSagaParam> String start(String name, P param, Function<P, Saga<P>> sagaFunction) {
		String transactionId = transactionIdRegistry.getNextVal(name);
		Saga<P> saga = sagaFunction.apply(param);
		saga.setKafkaTemplate(kafkaTemplate);
		map.put(transactionId, saga);
		//Sagaを非同期で実行
		ListenableFuture<?> future = threadPool.submitListenable(() -> {
			saga.start();
		});
		//処理が成功 or 失敗したら、Mapから削除する
		future.addCallback(t -> {
				map.remove(transactionId);
			}, 
			t -> {
				map.remove(transactionId);
			});
		return transactionId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<Saga<?>> get(String transactionId) {
		return Optional.ofNullable(map.get(transactionId));
	}

}
