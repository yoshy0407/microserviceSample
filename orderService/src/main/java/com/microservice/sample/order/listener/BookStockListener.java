package com.microservice.sample.order.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.microservice.sample.order.saga.listener.AbstractSagaListener;
import com.microservice.sample.order.saga.step.SagaStep;
import com.microservice.sample.order.service.event.BookStockResult;
import com.microservice.sample.order.service.event.ResultEvent;

@Component
@KafkaListener(topics = "book-stock-result")
public class BookStockListener extends AbstractSagaListener<BookStockResult>{

	@Override
	protected void doConsume(String transactionId, BookStockResult result) {
		String[] strs = transactionId.split("-");
		String transactionID = strs[0];
		String index = strs[1];
		SagaStep<?> step = sagaManager.get(transactionID).getStep(stepName + index);
		ResultEvent event = new ResultEvent(result);
		boolean success = step.checkResult(event);
		sagaManager.get(transactionID).callNext(success);
	}

	protected BookStockListener() {
		super("book-stock");
	}

	@Override
	protected String getTransactionId(BookStockResult result) {
		return result.getTransactionId();
	}

}
