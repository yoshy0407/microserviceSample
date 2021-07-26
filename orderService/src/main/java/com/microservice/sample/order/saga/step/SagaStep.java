package com.microservice.sample.order.saga.step;

import java.util.function.Function;
import org.springframework.kafka.core.KafkaTemplate;

import com.microservice.sample.order.service.event.ResultEvent;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SagaStep<PARAM> {

	@Getter
	private StepStatus status = StepStatus.BEFORE_EXECUTE;
	
	@Setter
	private Function<PARAM, MessageCall> createCallEvent;
	
	@Setter
	private Function<ResultEvent, Boolean> checkEventFunction;
	
	@Setter
	private Function<PARAM, MessageCall> createRollbackEvent;
	
	@Setter
	private Function<PARAM, MessageCall> createCompleteEvent;
	
	public void callService(KafkaTemplate<String, Object> kafkaTemplate, PARAM param) {
		MessageCall messageCall = createCallEvent.apply(param);
		log.info("send topic" + messageCall.getTopicName());
		kafkaTemplate.send(messageCall.getTopicName(), messageCall.getSendEvent());
		status = StepStatus.BEFORE_EXECUTE;
	}
	
	public boolean checkResult(ResultEvent result) {
		Boolean success = checkEventFunction.apply(result);
		if (success) {
			this.status = StepStatus.SUCCESS;
		} else {
			this.status = StepStatus.FAILURE;
		}
		return success;
	}

	public void callRollbackService(KafkaTemplate<String, Object> kafkaTemplate, PARAM param) {
		MessageCall messageCall = createRollbackEvent.apply(param);
		log.info("send topic for rollback" + messageCall.getTopicName());
		kafkaTemplate.send(messageCall.getTopicName(), messageCall.getSendEvent());
		status = StepStatus.ROLLBACK;
	}

	public void callCompleteService(KafkaTemplate<String, Object> kafkaTemplate, PARAM param) {
		MessageCall messageCall = createCompleteEvent.apply(param);
		log.info("send topic for complete" + messageCall.getTopicName());
		kafkaTemplate.send(messageCall.getTopicName(), messageCall.getSendEvent());
		status = StepStatus.SUCCESS;
	}

}
