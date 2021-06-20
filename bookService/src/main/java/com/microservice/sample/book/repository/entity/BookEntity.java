package com.microservice.sample.book.repository.entity;

import org.seasar.doma.Column;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "BOOK")
public class BookEntity {

	@Id
	@Column(name = "BOOK_ID")
	private Integer bookId;
	
	@Column(name = "BOOK_NAME")
	private String bookName;
	
	@Column(name = "BOOK_STOCK")
	private Integer bookStock;
	
	@Column(name = "TX_STATUS")
	private String txStatus;

}
