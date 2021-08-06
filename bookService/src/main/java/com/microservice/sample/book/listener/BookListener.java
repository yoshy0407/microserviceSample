package com.microservice.sample.book.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.microservice.sample.book.BookStockDto;
import com.microservice.sample.book.TopicConstant;
import com.microservice.sample.book.service.BookService;
import com.microservice.sample.common.TransactionIdRegistry;
import com.microservice.sample.common.event.AbstractEvent;

@Component
public class BookListener {

	@Autowired
	private BookService service;
	
	@Autowired
	private TransactionIdRegistry registry;
	
	@KafkaListener(topics = TopicConstant.BOOK_STOCK)
	public void reduceStock(BookStockDto event) {
		if (registry.validateId(TopicConstant.BOOK_STOCK, event.getTransactionId())) {
			service.reduceStock(event);			
		}
	}

	@KafkaListener(topics = TopicConstant.BOOK_STOCK_ROLLBACK)
	public void rollback(BookStockDto event) {
		service.rollback(event.getTransactionId());
	}

	@KafkaListener(topics = TopicConstant.BOOK_STOCK_COMPLETE)
	public void complete(BookStockDto event) {
		if (registry.validateId(TopicConstant.BOOK_STOCK_COMPLETE, event.getTransactionId())) {
			service.complete(event);			
		}
	}
}
