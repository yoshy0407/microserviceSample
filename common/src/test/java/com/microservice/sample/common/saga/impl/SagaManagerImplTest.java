package com.microservice.sample.common.saga.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.microservice.sample.common.saga.AbstractSagaParam;
import com.microservice.sample.common.saga.Saga;
import com.microservice.sample.common.saga.SagaBuilder;
import com.microservice.sample.common.saga.TransactionIdStrategy;
import com.microservice.sample.common.saga.step.OnReplyStatus;

class SagaManagerImplTest {

	@SuppressWarnings("unchecked")
	ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate = Mockito.mock(ReplyingKafkaTemplate.class);
	
	TransactionIdStrategy strategy = Mockito.mock(TransactionIdStrategy.class);
	
	SagaManagerImpl sagaManager;
	
	@BeforeEach
	void setup() {
		Mockito.when(strategy.getNextVal()).thenReturn("00001");
		
		ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
		threadPool.initialize();
		sagaManager = new SagaManagerImpl(strategy, kafkaTemplate, threadPool);
	}
	
	@Test
	void testNormal() throws InterruptedException {
		String transactionId = sagaManager.start(new TestSagaParam(), createFunction());
		
		//Sagaが実行中のため、管理されている
		assertThat(sagaManager.get(transactionId).isPresent()).isTrue();
		
		Thread.sleep(5000);
		
		//Sagaが終了しているため、削除されている
		assertThat(sagaManager.get(transactionId).isPresent()).isFalse();
	}
	
	class TestSagaParam extends AbstractSagaParam {

	}
	
	class TestEvent {
		
	}
	
	class TestResult {
		
	}
	
	class TestCompolete {
		
	}
	
	class TestRollback {
		
	}
	
	Function<TestSagaParam, Saga<TestSagaParam>> createFunction(){
		return p -> {
			SagaBuilder<TestSagaParam> builder = SagaBuilder.saga(p);
				builder.step("test1")
					.callService("test1-topic", pm -> {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO 自動生成された catch ブロック
							e.printStackTrace();
						}
						return new TestEvent();
					})
					.callComplete("test1-complete-topic", pm -> {
						return new TestCompolete();
					})
					.callRollback("test1-rollback-topic", pm -> {
						return new TestRollback();
					});
				builder.step("test2")
					.callService("test2-topic", pm -> {
						return new TestEvent();
					})
					.receiveReply("test2-result-topic", pm -> {
						return OnReplyStatus.FORWORD_NEXT;
					})
					.callComplete("test2-complete-topic", pm -> {
						return new TestCompolete();
					})
					.callRollback("test2-rollback-topic", pm -> {
						return new TestRollback();
					});
				builder.step("test3")
					.callService("test3-topic", pm -> {
						return new TestEvent();
					})
					.receiveReply("test3-result-topic", pm -> {
						//３つ目のステップでロールバックにする
						return OnReplyStatus.FORWORD_NEXT;
					})
					.callComplete("test3-complete-topic", pm -> {
						return new TestCompolete();
					})
					.callRollback("test3-rollback-topic", pm -> {
						return new TestRollback();
					});
				builder
					.finishComplete(pm -> {
					})
					.finishRollback(pm -> {
					});
				
				return builder.build();
		};
	}

}
