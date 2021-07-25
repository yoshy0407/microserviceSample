package com.microservice.sample.order.service;

import java.util.function.Function;
import org.springframework.kafka.core.KafkaTemplate;

import com.microservice.sample.order.service.event.ResultEvent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class SagaContextDetails<RESULT extends ResultEvent, PARAM> {

	private StepStatus status = StepStatus.BEFORE_CALL;
	
	private Function<PARAM, MessageCall> createCallEvent;
	
	private Function<RESULT, Boolean> checkEventFunction;
	
	private Function<PARAM, MessageCall> createRollbackEvent;
	
	private Function<PARAM, MessageCall> createCompleteEvent;
	
	public void callService(KafkaTemplate<String, Object> kafkaTemplate, PARAM param) {
		MessageCall messageCall = createCallEvent.apply(param);
		log.info("send topic" + messageCall.getTopicName());
		kafkaTemplate.send(messageCall.getTopicName(), messageCall.getSendEvent());
		status = StepStatus.CALLING;
	}
	
	public boolean checkResult(RESULT result) {
		return checkEventFunction.apply(result);
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
