package com.microservice.sample.order.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.microservice.sample.order.OrderBook;
import com.microservice.sample.order.OrderRegisterRequest;
import com.microservice.sample.order.RestResult;
import com.microservice.sample.order.TxStatus;
import com.microservice.sample.order.repository.dao.BookOrderDao;
import com.microservice.sample.order.repository.dao.OrderDetailDao;
import com.microservice.sample.order.repository.entity.BookOrderEntity;
import com.microservice.sample.order.repository.entity.OrderDetailEntity;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Throwable.class)
public class OrderService {

	@Autowired
	protected BookOrderDao bookOrderDao;
	
	@Autowired
	protected OrderDetailDao orderDetailDao;
	
	@Autowired
	protected MessageSource messageSource;
	
	public RestResult register(OrderRegisterRequest req){
		RestResult restResult = new RestResult();
		
		BookOrderEntity entity = createBookOrder(req);
		
		//注文テーブルの登録
		int result = bookOrderDao.insert(entity);
		
		//データの登録エラーのチェック
		if (result != 1) {
			String message = getMessage("order.insert.error");
			restResult.setMessage(message);
			restResult.setSuccess(1);
			return restResult;
		}
		
		//注文明細の登録
		List<OrderDetailEntity> orderDetails = createDetails(entity.getOrderId(), req);
		
		int[] results = orderDetailDao.batchInsert(orderDetails);
		
		//データの登録エラーのチェック
		for (int count : results) {
			if (count != 1) {
				String message = getMessage("order.insert.error");
				restResult.setMessage(message);
				restResult.setSuccess(1);
				return restResult;
			}
		}
		restResult.setMessage(getMessage("order.success"));
		restResult.setResult(entity);
		restResult.setDetails(orderDetails);
		return restResult;
	}
	
	protected BookOrderEntity createBookOrder(OrderRegisterRequest req) {
		BookOrderEntity entity = new BookOrderEntity();
		entity.setCustomerId(req.getCustomerId());
		entity.setOrderDate(LocalDateTime.now());
		entity.setTxStatus(TxStatus.UPDATING.toString());
		return entity;
	}
	
	protected List<OrderDetailEntity> createDetails(Integer orderId, OrderRegisterRequest req){
		List<OrderDetailEntity> orderDetails = new ArrayList<>();
		Integer i = 1;
		for (OrderBook orderBook : req.getOrderBooks()) {
			OrderDetailEntity entity = new OrderDetailEntity();
			entity.setOrderId(orderId);
			entity.setOrderAsc(i);
			entity.setBookId(orderBook.getBookId());
			entity.setOrderCount(orderBook.getOrderCount());
			entity.setTxStatus(TxStatus.NONE.toString());
			orderDetails.add(entity);
			i++;
		}
		return orderDetails;
	}
	
	private String getMessage(String messageId, Object...args) {
		return messageSource.getMessage(messageId, args, Locale.getDefault());
	}
}
