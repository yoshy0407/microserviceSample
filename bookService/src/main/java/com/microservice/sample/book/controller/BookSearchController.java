package com.microservice.sample.book.controller;

import java.util.List;

import org.seasar.doma.jdbc.SelectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.sample.book.repository.dao.BookDao;
import com.microservice.sample.book.repository.entity.BookEntity;

@RestController
@RequestMapping("/book")
public class BookSearchController {

	@Autowired
	private BookDao dao;
	
	@GetMapping("list")
	public List<BookEntity> getList(BookEntity entity){
		return dao.selectList(entity, SelectOptions.get());
	}

}
