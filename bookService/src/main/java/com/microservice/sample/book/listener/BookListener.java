package com.microservice.sample.book.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.microservice.sample.book.BookStockDto;
import com.microservice.sample.book.TopicConstant;
import com.microservice.sample.book.service.BookService;

@Component
public class BookListener {

	@Autowired
	private BookService service;
	
	@KafkaListener(topics = TopicConstant.BOOK_STOCK)
	public void reduceStock(BookStockDto dto) {
		service.reduceStock(dto);
	}

	@KafkaListener(topics = TopicConstant.BOOK_STOCK_ROLLBACK)
	public void rollback(String transactionId) {
		service.rollback(transactionId);
	}

	@KafkaListener(topics = TopicConstant.BOOK_STOCK_COMPLETE)
	public void complete(BookStockDto dto) {
		service.complete(dto);
	}
}
