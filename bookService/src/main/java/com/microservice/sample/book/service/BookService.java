package com.microservice.sample.book.service;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.seasar.doma.jdbc.SelectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.microservice.sample.book.BookStockDto;
import com.microservice.sample.book.BookStockResult;
import com.microservice.sample.book.TopicConstant;
import com.microservice.sample.book.TxStatus;
import com.microservice.sample.book.repository.dao.BookDao;
import com.microservice.sample.book.repository.entity.BookEntity;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class BookService {

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;
	
	@Autowired
	private BookDao dao;
	
	@Autowired
	private MessageSource messageSource;
	
	private ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<String, Object>();
	
	public void reduceStock(BookStockDto dto) {
		
		log.info("start reduceStock :" + dto.toString());
		
		//重複するトランザクションIDは取り込まない
		//kafkaは１回はメッセージを送ることを補償するため、取り込み済みのものは処理しない
		if (!checkTransactionId(dto.getTransactionId())) {
			log.info("end reduceStock duplicate transactionId " + dto.getTransactionId());
			return;
		}
		
		//:TODO 処理中でないレコードを取りに行っているが、他で更新中の場合どうする？
		//今の状態だとレコードなしのエラーとなってしまう。本当であれば、RDBMSと同様にロック待ちにするべき
		BookEntity entity = getBook(dto.getBookId(), TxStatus.NONE, false);
		
		//データが存在しない場合
		if (entity == null) {
			//取り込み済みにするためダミーをセット
			map.put(dto.getTransactionId(), "dummy");
			String message = messageSource.getMessage("book.notFound", new Object[] {dto.getBookId()}, Locale.getDefault());
			publishResult(message, false, message);
			log.error("reduceStock error has occured. message" + message);
			return;
		}
		
		//取り込み済みにするために一度格納する
		map.put(dto.getTransactionId(), entity);

		//注文数が在庫数を超えてしまっている
		if (entity.getBookStock() - dto.getReduceCount() < 0) {
			String message = messageSource.getMessage("stock.error", new Object[] {entity.getBookStock(), dto.getReduceCount()}, Locale.getDefault());
			publishResult(dto.getTransactionId(), false, message);
			log.error("reduceStock error has occured. message" + message);
			return;
		}
		
		//forUpdateでデータを取得しデータを更新
		BookEntity updateEntity = getBook(entity.getBookId(), TxStatus.NONE, true);
		
		updateEntity.setBookStock(updateEntity.getBookStock() - dto.getReduceCount());
		updateEntity.setTxStatus(TxStatus.UPDATING.toString());
		
		dao.update(updateEntity);
		
		publishResult(dto.getTransactionId(), true, null);
		
		log.info("end reduceStock :" + dto.toString());
	}
	
	public void rollback(String transactionId) {
		log.info("start reduceStock rollback transactionId:" + transactionId);
		BookEntity targetEntity = (BookEntity) map.get(transactionId);
		
		getBook(targetEntity.getBookId(), TxStatus.UPDATING, true);
		
		dao.update(targetEntity);
		log.info("end reduceStock rollback");
	}
	
	public void complete(BookStockDto dto) {
		log.info("start reduceStock complete transactionId:" + dto.getTransactionId());
		BookEntity entity = getBook(dto.getBookId(), TxStatus.UPDATING, true);
		entity.setTxStatus(TxStatus.NONE.toString());
		
		dao.update(entity);
		log.info("end reduceStock complete");
	}
	
	protected boolean checkTransactionId(String transactionId) {
		return map.get(transactionId) == null;
	}
	
	private BookEntity getBook(Integer bookId, TxStatus status, boolean lock) {
		BookEntity entity = new BookEntity();
		entity.setBookId(bookId);
		entity.setTxStatus(status.toString());
		
		SelectOptions option = SelectOptions.get();
		if (lock) {
			option.forUpdate();
		}
		return dao.select(entity, option);
	}
	
	private void publishResult(String transactonId, boolean success, String message) {
		BookStockResult result = new BookStockResult();
		result.setResult(success);
		result.setTransactionId(transactonId);
		if (StringUtils.hasText(message)) {
			result.setMessage(message);
		}
		sendTopic(TopicConstant.BOOK_STOCK_RESULT, result);
	}
	
	private void sendTopic(String topic, Object data) {
		kafkaTemplate.send(topic, data);
		log.info("send message topic:" + topic);		
		log.info("send data" + data.toString());		
	}
	
}
