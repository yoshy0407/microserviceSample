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
		
		Optional<CustomerEntity> optEntity = dao.selectById(dto.getCustomerId(), TxStatus.NONE.toString(),
				SelectOptions.get());
		if (optEntity.isEmpty()) {
			return getMessage("customerId.notFound", dto.getCustomerId());
		}
		
		CustomerEntity entity = optEntity.get();
		
		if(entity.getBookCount() - dto.getBookCount() < 0) {
			return getMessage("bookCount.error", entity.getBookCount());
		}
		
		map.put(dto.getTransactionId(), dao.selectById(dto.getCustomerId(), TxStatus.NONE.toString(),
				SelectOptions.get().forUpdate()).get());
		
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
		
		kafkaTemplate.send("cutomer-validation-result", result);
	}
	
	private Optional<String> getMessage(String messageId, Object...args){
		return Optional.of(messageSource.getMessage("customerId.notFound", args, Locale.getDefault()));
	}
	
	
}
