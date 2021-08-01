package com.microservice.sample.common.saga;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import com.microservice.sample.common.saga.step.OnReplyStatus;
import com.microservice.sample.common.saga.step.StepStatus;

class SagaTest {

	@SuppressWarnings("unchecked")
	ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate = Mockito.mock(ReplyingKafkaTemplate.class);
	
	@Test
	void testNormal() {
		//テスト用サーガの準備
		TestSagaParam param = new TestSagaParam();
		SagaBuilder<TestSagaParam> builder = SagaBuilder.saga(param);
			builder.step("test1")
				.callService("test1-topic", p -> {
					p.test1Call = true;
					return new TestEvent();
				})
				.callComplete("test1-complete-topic", p -> {
					p.test1Complete = true;
					return new TestCompolete();
				})
				.callRollback("test1-rollback-topic", p -> {
					p.test1Rollback = true;
					return new TestRollback();
				});
			builder.step("test2")
				.callService("test2-topic", p -> {
					p.test2Call = true;
					return new TestEvent();
				})
				.receiveReply("test2-result-topic", p -> {
					return OnReplyStatus.FORWORD_NEXT;
				})
				.callComplete("test2-complete-topic", p -> {
					p.test2Complete = true;
					return new TestCompolete();
				})
				.callRollback("test2-rollback-topic", p -> {
					p.test2Rollback = true;
					return new TestRollback();
				});
			builder.step("test3")
				.callService("test3-topic", p -> {
					p.test3Call = true;
					return new TestEvent();
				})
				.receiveReply("test3-result-topic", p -> {
					return OnReplyStatus.FORWORD_NEXT;
				})
				.callComplete("test3-complete-topic", p -> {
					p.test3Complete = true;
					return new TestCompolete();
				})
				.callRollback("test3-rollback-topic", p -> {
					p.test3Rollback = true;
					return new TestRollback();
				});
			builder
				.finishComplete(p -> {
					p.completeLogic = true;
				})
				.finishRollback(p -> {
					p.rollbackLogic = true;
				});
			
			Saga<TestSagaParam> saga = builder.build();
			
			//モックの準備
			KafkaMockSupport.mockSendAndReceive(kafkaTemplate, KafkaMockSupport.createMessage(new TestResult(), "dummy"));
			
			saga.setKafkaTemplate(kafkaTemplate);
			saga.start();
			
			assertThat(saga.getStep("test1").getStatus()).isEqualTo(StepStatus.COMPLETE);
			assertThat(saga.getStep("test2").getStatus()).isEqualTo(StepStatus.COMPLETE);
			assertThat(saga.getStep("test3").getStatus()).isEqualTo(StepStatus.COMPLETE);
			
			assertThat(param.test1Call).isTrue();
			assertThat(param.test1Complete).isTrue();
			assertThat(param.test1Rollback).isFalse();
			assertThat(param.test2Call).isTrue();
			assertThat(param.test2Complete).isTrue();
			assertThat(param.test2Rollback).isFalse();
			assertThat(param.test3Call).isTrue();
			assertThat(param.test3Complete).isTrue();
			assertThat(param.test3Rollback).isFalse();
			assertThat(param.completeLogic).isTrue();
			assertThat(param.rollbackLogic).isFalse();
	}
	
	@Test
	void testRollback() {
		//テスト用サーガの準備
		TestSagaParam param = new TestSagaParam();
		SagaBuilder<TestSagaParam> builder = SagaBuilder.saga(param);
			builder.step("test1")
				.callService("test1-topic", p -> {
					p.test1Call = true;
					return new TestEvent();
				})
				.callComplete("test1-complete-topic", p -> {
					p.test1Complete = true;
					return new TestCompolete();
				})
				.callRollback("test1-rollback-topic", p -> {
					p.test1Rollback = true;
					return new TestRollback();
				});
			builder.step("test2")
				.callService("test2-topic", p -> {
					p.test2Call = true;
					return new TestEvent();
				})
				.receiveReply("test2-result-topic", p -> {
					return OnReplyStatus.FORWORD_NEXT;
				})
				.callComplete("test2-complete-topic", p -> {
					p.test2Complete = true;
					return new TestCompolete();
				})
				.callRollback("test2-rollback-topic", p -> {
					p.test2Rollback = true;
					return new TestRollback();
				});
			builder.step("test3")
				.callService("test3-topic", p -> {
					p.test3Call = true;
					return new TestEvent();
				})
				.receiveReply("test3-result-topic", p -> {
					//３つ目のステップでロールバックにする
					return OnReplyStatus.ROLLBACK;
				})
				.callComplete("test3-complete-topic", p -> {
					p.test3Complete = true;
					return new TestCompolete();
				})
				.callRollback("test3-rollback-topic", p -> {
					p.test3Rollback = true;
					return new TestRollback();
				});
			builder
				.finishComplete(p -> {
					p.completeLogic = true;
				})
				.finishRollback(p -> {
					p.rollbackLogic = true;
				});
			
			Saga<TestSagaParam> saga = builder.build();
			
			//モックの準備
			KafkaMockSupport.mockSendAndReceive(kafkaTemplate, KafkaMockSupport.createMessage(new TestResult(), "dummy"));
			
			saga.setKafkaTemplate(kafkaTemplate);
			saga.start();
			
			assertThat(saga.getStep("test1").getStatus()).isEqualTo(StepStatus.ROLLBACK);
			assertThat(saga.getStep("test2").getStatus()).isEqualTo(StepStatus.ROLLBACK);
			assertThat(saga.getStep("test3").getStatus()).isEqualTo(StepStatus.FAILURE);
			
			assertThat(param.test1Call).isTrue();
			assertThat(param.test1Complete).isFalse();
			assertThat(param.test1Rollback).isTrue();
			assertThat(param.test2Call).isTrue();
			assertThat(param.test2Complete).isFalse();
			assertThat(param.test2Rollback).isTrue();
			assertThat(param.test3Call).isTrue();
			assertThat(param.test3Complete).isFalse();
			assertThat(param.test3Rollback).isFalse();
			assertThat(param.completeLogic).isFalse();
			assertThat(param.rollbackLogic).isTrue();
	}

	class TestSagaParam extends AbstractSagaParam {
	
		public boolean test1Call = false;
		
		public boolean test1Complete = false;

		public boolean test1Rollback = false;

		public boolean test2Call = false;
		
		public boolean test2Complete = false;

		public boolean test2Rollback = false;

		public boolean test3Call = false;
		
		public boolean test3Complete = false;
		
		public boolean test3Rollback = false;
		
		public boolean completeLogic = false;

		public boolean rollbackLogic = false;

}
	
	class TestEvent {
		
	}
	
	class TestResult {
		
	}
	
	class TestCompolete {
		
	}
	
	class TestRollback {
		
	}
	
}
