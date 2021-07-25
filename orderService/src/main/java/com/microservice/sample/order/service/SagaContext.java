package com.microservice.sample.order.service;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.springframework.kafka.core.KafkaTemplate;

import com.microservice.sample.order.service.event.ResultEvent;

import ch.qos.logback.core.status.Status;
import lombok.Data;

@Data
public class SagaContext<PARAM> {

	private LinkedHashMap<String, SagaContextDetails<?, PARAM>> step;
	
	private PARAM param;
	
	private KafkaTemplate<String, Object> kafkaTemplate;
	
	private SagaStatus status;
	
	public SagaContext(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public SagaContextDetails<?, PARAM> getDetails(String details){
		return step.get(details);
	}
	
	public void checkComplete() {
		if (doCheckComplete()) {
			status = SagaStatus.COMPLETE;
			doComplete();
		}
	}
	
	public void callNext() {
		SagaContextDetails<?, PARAM> contextDetails = getNext();
		contextDetails.callService(kafkaTemplate, param);
	}
	
	protected SagaContextDetails<?, PARAM> getNext(){
		return step.entrySet().stream()
				.filter(e -> {
					SagaContextDetails<?, PARAM> contextDetails = e.getValue();
					boolean result = contextDetails.getStatus().equals(StepStatus.BEFORE_CALL);
					return result;
				})
				.map(e -> e.getValue())
				.findFirst()
				.get();
				
	}
	
	protected void doComplete() {
		step.entrySet().forEach(e -> {
			e.getValue().callCompleteService(kafkaTemplate, param);
		});
	}
	
	protected boolean doCheckComplete() {
		for (Entry<String, SagaContextDetails<?, PARAM>> entry : step.entrySet()) {
			SagaContextDetails<?, PARAM> contextDetails = entry.getValue();
			if (contextDetails.getStatus().equals(StepStatus.BEFORE_CALL)
				|| contextDetails.getStatus().equals(StepStatus.CALLING)
				|| contextDetails.getStatus().equals(StepStatus.ROLLBACK)) {
				return false;
			}

		}
		return true;
	}
		
}
