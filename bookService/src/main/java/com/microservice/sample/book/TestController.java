package com.microservice.sample.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.sample.common.event.AbstractEvent;

@RestController
@RequestMapping("book")
public class TestController {

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;
	
	@GetMapping("reduceStock")
	public String sendReduceStock(BookStockDto dto) {
		kafkaTemplate.send(TopicConstant.BOOK_STOCK, dto);
		return "sendMessage";
	}

	@GetMapping("rollback")
	public String sendRollback(String transactionId) {
		BookStockDto dto = new BookStockDto();
		dto.setTransactionId(transactionId);
		kafkaTemplate.send(TopicConstant.BOOK_STOCK_ROLLBACK, dto);
		return "sendMessage";
	}

	@GetMapping("complete")
	public String sendComplete(BookStockDto dto) {
		kafkaTemplate.send(TopicConstant.BOOK_STOCK_COMPLETE, dto);
		return "sendMessage";
	}
	
}
