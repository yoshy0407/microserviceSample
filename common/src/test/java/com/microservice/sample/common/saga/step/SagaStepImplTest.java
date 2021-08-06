package com.microservice.sample.common.saga.step;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import com.microservice.sample.common.event.AbstractEvent;
import com.microservice.sample.common.saga.AbstractSagaParam;
import com.microservice.sample.common.saga.KafkaMockSupport;

import lombok.Data;

class SagaStepImplTest {

	@SuppressWarnings("unchecked")
	ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate = Mockito.mock(ReplyingKafkaTemplate.class);
	
	@Test
	void testCallOnly() throws InterruptedException, ExecutionException {
		
		//モックの準備
		TestEvent event = new TestEvent();
		KafkaMockSupport.mockSend(kafkaTemplate, KafkaMockSupport.createMessage(event, "testTopic"));
		
		//テスト対象の準備
		SagaStepImpl<TestEvent, TestParam, TestResult, TestRollback, TestComplete> step =
				new SagaStepImpl<>("testStep");
		
		step.callService("testTopic", p -> {
			return event;
		});
		
		OnReplyStatus status = step.call(kafkaTemplate, new TestParam());
		
		//assert
		assertThat(status).isEqualTo(OnReplyStatus.FORWORD_NEXT);
		assertThat(step.getStatus()).isEqualTo(StepStatus.SUCCESS);
	}
	
	@Test
	void testCallAndReply() throws InterruptedException, ExecutionException {
		
		// モックの準備
		TestEvent event = new TestEvent();
		KafkaMockSupport.mockSendAndReceive(kafkaTemplate, KafkaMockSupport.createMessage(new TestResult(), "dummy"));

		//テスト対象の準備
		SagaStepImpl<TestEvent, TestParam, TestResult, TestRollback, TestComplete> step =
				new SagaStepImpl<>("testStep");
		
		step.callService("testTopic", p -> {
			return event;
		});
		step.receiveReply("testReplyTopic", e -> {
			return OnReplyStatus.FORWORD_NEXT;
		});
		
		OnReplyStatus status = step.call(kafkaTemplate, new TestParam());
		
		assertThat(status).isEqualTo(OnReplyStatus.FORWORD_NEXT);
		assertThat(step.getStatus()).isEqualTo(StepStatus.SUCCESS);		
	}
	
	@Test
	void testCallAndReplyComplete() throws InterruptedException, ExecutionException {
		
		// モックの準備
		TestEvent event = new TestEvent();
		KafkaMockSupport.mockSendAndReceive(kafkaTemplate, KafkaMockSupport.createMessage(new TestResult(), "dummy"));

		//テスト対象の準備
		SagaStepImpl<TestEvent, TestParam, TestResult, TestRollback, TestComplete> step =
				new SagaStepImpl<>("testStep");
		
		TestParam param = new TestParam();
		
		step.callService("testTopic", p -> {
			return event;
		});
		step.receiveReply("testReplyTopic", e -> {
			return OnReplyStatus.FORWORD_NEXT;
		});
		step.callComplete("testCompleteTopic", p -> {
			return new TestComplete();
		});
		
		OnReplyStatus status = step.call(kafkaTemplate, param);
		
		assertThat(status).isEqualTo(OnReplyStatus.FORWORD_NEXT);
		assertThat(step.getStatus()).isEqualTo(StepStatus.SUCCESS);
		
		step.complete(kafkaTemplate, param);
		
		assertThat(step.getStatus()).isEqualTo(StepStatus.COMPLETE);
	}
	
	@Test
	void testCallAndReplyRollback() throws InterruptedException, ExecutionException {
		
		// モックの準備
		TestEvent event = new TestEvent();
		KafkaMockSupport.mockSendAndReceive(kafkaTemplate, KafkaMockSupport.createMessage(new TestResult(), "dummy"));

		//テスト対象の準備
		SagaStepImpl<TestEvent, TestParam, TestResult, TestRollback, TestComplete> step =
				new SagaStepImpl<>("testStep");
		
		TestParam param = new TestParam();
		
		step.callService("testTopic", p -> {
			return event;
		});
		step.receiveReply("testReplyTopic", e -> {
			return OnReplyStatus.ROLLBACK;
		});
		step.callRollback("testRollbackTopic", p -> {
			return new TestRollback();
		});
		
		OnReplyStatus status = step.call(kafkaTemplate, param);
		
		assertThat(status).isEqualTo(OnReplyStatus.ROLLBACK);
		assertThat(step.getStatus()).isEqualTo(StepStatus.FAILURE);
		
		step.rollback(kafkaTemplate, param);
		
		assertThat(step.getStatus()).isEqualTo(StepStatus.ROLLBACK);
	}

	@Data
	class TestEvent extends AbstractEvent {
		
		private String testField;
		
	}
	
	class TestParam extends AbstractSagaParam {
		
	}
	
	class TestResult extends AbstractEvent {
		
	}
	
	class TestRollback extends AbstractEvent {
		
	}
	
	class TestComplete extends AbstractEvent {
		
	}
	
}
