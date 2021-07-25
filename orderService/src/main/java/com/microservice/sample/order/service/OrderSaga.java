package com.microservice.sample.order.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Async;

import com.microservice.sample.order.OrderBook;
import com.microservice.sample.order.service.event.BookStockDto;
import com.microservice.sample.order.service.event.BookStockResult;
import com.microservice.sample.order.service.event.CustomerValidationDto;
import com.microservice.sample.order.service.event.CustomerValidationResult;

@Async("asyncThreadPool")
public class OrderSaga {

	private int transactionCount = 1;
	
	private ConcurrentHashMap<String, SagaContext<OrderSagaParam>> sagaMap = new ConcurrentHashMap<>();
	
	public void start(OrderSagaParam params) {
		String transactionId = String.format("%010d", transactionCount);
		params.setTransactionId(transactionId);
		SagaContext<OrderSagaParam> context = createContext(params);
		sagaMap.put(transactionId, context);
	}
	
	protected SagaContext<OrderSagaParam> createContext(OrderSagaParam params) {
		SagaContext<OrderSagaParam> context = new SagaContext<>();
		context.setParam(params);
		
		LinkedHashMap<String, SagaContextDetails<?, OrderSagaParam>> map = new LinkedHashMap<>();
		map.put("customerValidation", createCustomer());
		int i = 1;
		for (OrderBook orderBook : params.getOrderBooks()) {
			map.put("bookReduce" + i, createBook(params.getTransactionId(), orderBook));
			i++;
		}
		context.setStep(map);
		return context;
	}
	
	private SagaContextDetails<CustomerValidationResult, OrderSagaParam> createCustomer(){
		SagaContextDetails<CustomerValidationResult, OrderSagaParam> customerContext = new SagaContextDetails<>();
		//顧客バリデーションの呼び出しイベントの設定
		customerContext.setCreateCallEvent(p -> {
			MessageCall messageCall = new MessageCall();
			messageCall.setTopicName("customer-validation");
			messageCall.setSendEvent(createValidation(p));
			return messageCall;
		});
		
		//バリデーションの結果のチェックロジックの設定
		customerContext.setCheckEventFunction(e-> {
			return e.isSuccess();
		});
		
		//バリデーションエラーの結果ロールバックの呼び出しロジック
		customerContext.setCreateRollbackEvent(p -> {
			MessageCall messageCall = new MessageCall();
			messageCall.setTopicName("customer-validation-rollback");
			messageCall.setSendEvent(p.getTransactionId());
			return messageCall;
		});
		
		//バリデーション結果を受けて完了を呼び出すロジック
		customerContext.setCreateCompleteEvent(p -> {
			MessageCall messageCall = new MessageCall();
			messageCall.setTopicName("customer-validation-complete");
			messageCall.setSendEvent(createValidation(p));
			return messageCall;
		});
		
		return customerContext;
	}
	
	private CustomerValidationDto createValidation(OrderSagaParam param) {
		CustomerValidationDto dto = new CustomerValidationDto();
		dto.setTransactionId(param.getTransactionId());
		dto.setCustomerId(param.getCustomerId());
		int count = param.getOrderBooks().stream()
				.mapToInt(b -> b.getOrderCount())
				.sum();
		dto.setBookCount(count);
		return dto;
	}
	
	private SagaContextDetails<BookStockResult, OrderSagaParam> createBook(String transactionId, OrderBook book){
		SagaContextDetails<BookStockResult, OrderSagaParam> details = new SagaContextDetails<>();
		//本の在庫サービスの在庫のマイナスを実行を呼び出すロジックを設定
		details.setCreateCallEvent(p -> {
			MessageCall messageCall = new MessageCall();
			messageCall.setTopicName("book-stock");
			messageCall.setSendEvent(createBookDto(transactionId, book));
			return messageCall;
		});
		
		//本の在庫サービスの結果のチェック
		details.setCheckEventFunction(r -> {
			return r.isResult();
		});
		
		//本の在庫サービスのロールバック呼び出しロジック
		details.setCreateCompleteEvent(p -> {
			MessageCall messageCall = new MessageCall();
			messageCall.setTopicName("book-stock-rollback");
			messageCall.setSendEvent(p.getTransactionId());
			return messageCall;
		});
		
		//本の在庫サービスの完了の呼び出しロジック
		details.setCreateCompleteEvent(p -> {
			MessageCall messageCall = new MessageCall();
			messageCall.setTopicName("book-stock-complete");
			messageCall.setSendEvent(createBookDto(transactionId, book));
			return messageCall;
		});
		return details;
	}
	
	private BookStockDto createBookDto(String transactionId, OrderBook book) {
		BookStockDto dto = new BookStockDto();
		dto.setTransactionId(transactionId);
		dto.setBookId(book.getBookId());
		dto.setReduceCount(book.getOrderCount());
		return dto;
	}
	
}
