package com.microservice.sample.book;

import java.util.List;

import org.seasar.doma.jdbc.SelectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.sample.book.repository.dao.BookDao;
import com.microservice.sample.book.repository.entity.BookEntity;

@RestController
@RequestMapping("book")
public class TestController {

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;
	
	@Autowired
	private BookDao dao;
	
	@GetMapping("reduceStock")
	public String sendReduceStock(BookStockDto dto) {
		kafkaTemplate.send(TopicConstant.BOOK_STOCK, dto);
		return "sendMessage";
	}

	@GetMapping("rollback")
	public String sendRollback(String transactionId) {
		kafkaTemplate.send(TopicConstant.BOOK_STOCK_ROLLBACK, transactionId);
		return "sendMessage";
	}

	@GetMapping("complete")
	public String sendComplete(BookStockDto dto) {
		kafkaTemplate.send(TopicConstant.BOOK_STOCK_COMPLETE, dto);
		return "sendMessage";
	}
	
	@GetMapping("list")
	public List<BookEntity> getList(BookEntity entity){
		return dao.selectList(entity, SelectOptions.get());
	}

}
