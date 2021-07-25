package com.microservice.sample.customer.service;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.seasar.doma.jdbc.SelectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.microservice.sample.customer.CustomerValidationDto;
import com.microservice.sample.customer.TxStatus;
import com.microservice.sample.customer.ValidationResult;
import com.microservice.sample.customer.repository.dao.CustomerDao;
import com.microservice.sample.customer.repository.entity.CustomerEntity;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class CustomerValidationService {

	@Autowired
	private CustomerDao dao;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;
	
	private ConcurrentHashMap<String, CustomerEntity> map = new ConcurrentHashMap<>();
	
	
	public void validate(CustomerValidationDto dto) {
		log.info("start customer validaiton");
		log.info(dto.toString());
		
		//トランザクションIDを確認し、重複したものが存在する場合は処理を行わない
		//kafkaは最低１回はメッセージを送ることを想定しているため、プログラムで二重チェックを行う
		if (!checkTransactionId(dto.getTransactionId())) {
			log.info("end customer validation duplicate transactionId " + dto.getTransactionId());
			return;
		}
		
		Optional<String> result = validateInternal(dto);
		publish(result, dto.getTransactionId());
		log.info("end customer validaiton");
	}
	
	public void rollback(String transactionId) {
		log.info("start customer rollback");
		CustomerEntity entity = map.get(transactionId);
		dao.update(entity);
		log.info("end customer rollback");
	}
	
	public void complete(CustomerValidationDto dto) {
		log.info("start customer complete");

		Optional<CustomerEntity> optEntity = dao.selectById(dto.getCustomerId(), TxStatus.UPDATING.toString() 
				,SelectOptions.get().forUpdate());
		CustomerEntity entity = optEntity.get();
		entity.setTxStatus(TxStatus.NONE.toString());
		
		dao.update(entity);
		
		log.info("end customer complete");
	}
	
	protected Optional<String> validateInternal(CustomerValidationDto dto) {
		
		//顧客情報の検索
		//:TODO 対象データが更新中の場合、データは取得できない
		//RDBMSのようにロック待ちにすべき？
		Optional<CustomerEntity> optEntity = dao.selectById(dto.getCustomerId(), TxStatus.NONE.toString(),
				SelectOptions.get());
		
		//対象の顧客がない場合はエラーを返却する
		if (optEntity.isEmpty()) {
			//取り込み済みとするため、ダミーデータを入れる
			map.put(dto.getTransactionId(), new CustomerEntity());
			return getMessage("customerId.notFound", dto.getCustomerId());
		}
		
		CustomerEntity entity = optEntity.get();
		
		//予約数の上限チェック
		if(entity.getBookCount() - dto.getBookCount() < 0) {
			//取り込み済みとするため、ダミーデータを入れる
			map.put(dto.getTransactionId(), new CustomerEntity());
			return getMessage("bookCount.error", entity.getBookCount());
		}
		
		//ロールバックに備えて、データ戻し用のエンティティを保持する
		map.put(dto.getTransactionId(), dao.selectById(dto.getCustomerId(), TxStatus.NONE.toString(),
				SelectOptions.get().forUpdate()).get());
		
		//データの更新。更新中であることを表すためにステータスを更新中にする
		entity.setBookCount(entity.getBookCount() - dto.getBookCount());
		entity.setTxStatus(TxStatus.UPDATING.toString());
		
		dao.update(entity);
		
		return Optional.empty();		
	}
	
	protected void publish(Optional<String> message, String transactionId) {
		ValidationResult result = new ValidationResult();
		result.setTransactionId(transactionId);
		result.setSuccess(message.isEmpty());
		message.ifPresent(s -> result.setMessage(s));
		
		kafkaTemplate.send("customer-validation-result", result);
	}
	
	protected boolean checkTransactionId(String transactionId) {
		return map.get(transactionId) == null;
	}
	
	private Optional<String> getMessage(String messageId, Object...args){
		String message = messageSource.getMessage(messageId, args, Locale.getDefault());
		log.error("error has occured");
		log.error("error message" + message);
		return Optional.of(message);
	}
	
	
	
}
