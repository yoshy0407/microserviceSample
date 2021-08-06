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

import com.microservice.sample.common.saga.SagaManager;
import com.microservice.sample.order.OrderBook;
import com.microservice.sample.order.OrderRegisterRequest;
import com.microservice.sample.order.RestResult;
import com.microservice.sample.order.TxStatus;
import com.microservice.sample.order.repository.dao.BookOrderDao;
import com.microservice.sample.order.repository.dao.OrderDetailDao;
import com.microservice.sample.order.repository.entity.BookOrderEntity;
import com.microservice.sample.order.repository.entity.OrderDetailEntity;
import com.microservice.sample.order.service.event.BookStockDto;
import com.microservice.sample.order.service.event.BookStockResult;
import com.microservice.sample.order.service.event.CustomerValidationDto;
import com.microservice.sample.order.service.event.CustomerValidationResult;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Throwable.class)
public class OrderService {

	@Autowired
	protected BookOrderDao bookOrderDao;

	@Autowired
	protected OrderDetailDao orderDetailDao;

	@Autowired
	protected MessageSource messageSource;

	//@Autowired
	//protected SagaManager sagaManager;

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
		//OrderSagaParam param = new OrderSagaParam();
		//param.setCustomerId(req.getCustomerId());
		//param.setOrderBooks(req.getOrderBooks());
		
		//startSaga(param, req.getOrderBooks());
		
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

	/**	protected void startSaga(OrderSagaParam param, List<OrderBook> books) {
		sagaManager.start(param, p -> {
			Saga<OrderSagaParam> saga = new Saga<>(param);
			saga.addStep("customer-validation", createCustomer());
			int i = 1;
			for (OrderBook book : books) {
				saga.addStep("book-stock" + i, createBook(book, i));
			}
			return saga;
		});
	}
		*/

	private String getMessage(String messageId, Object...args) {
		return messageSource.getMessage(messageId, args, Locale.getDefault());
	}

/**	private SagaStep<OrderSagaParam> createCustomer(){
			SagaStep<OrderSagaParam> customerStep = new SagaStep<>();
			//顧客バリデーションの呼び出しイベントの設定
			customerStep.setCreateCallEvent(pm -> {
				MessageCall messageCall = new MessageCall();
				messageCall.setTopicName("customer-validation");
				messageCall.setSendEvent(createValidation(pm));
				return messageCall;
			});

			//バリデーションの結果のチェックロジックの設定
			customerStep.setCheckEventFunction(e-> {
				return e.get(CustomerValidationResult.class).isSuccess();
			});

			//バリデーションエラーの結果ロールバックの呼び出しロジック
			customerStep.setCreateRollbackEvent(pm -> {
				MessageCall messageCall = new MessageCall();
				messageCall.setTopicName("customer-validation-rollback");
				messageCall.setSendEvent(pm.getTransactionId());
				return messageCall;
			});

			//バリデーション結果を受けて完了を呼び出すロジック
			customerStep.setCreateCompleteEvent(pm -> {
				MessageCall messageCall = new MessageCall();
				messageCall.setTopicName("customer-validation-complete");
				messageCall.setSendEvent(createValidation(pm));
				return messageCall;
			});

			return customerStep;
		};
*/

/**	private CustomerValidationDto createValidation(OrderSagaParam param) {
		CustomerValidationDto dto = new CustomerValidationDto();
		dto.setTransactionId(param.getTransactionId());
		dto.setCustomerId(param.getCustomerId());
		int count = param.getOrderBooks().stream()
				.mapToInt(b -> b.getOrderCount())
				.sum();
		dto.setBookCount(count);
		return dto;
	}
*/
/**	private SagaStep<OrderSagaParam> createBook(OrderBook book, int i){
		SagaStep<OrderSagaParam> step = new SagaStep<>();
		//本の在庫サービスの在庫のマイナスを実行を呼び出すロジックを設定
		step.setCreateCallEvent(p -> {
			MessageCall messageCall = new MessageCall();
			messageCall.setTopicName("book-stock");
			messageCall.setSendEvent(createBookDto(p.getTransactionId() + "-" + i, book));
			return messageCall;
		});

		//本の在庫サービスの結果のチェック
		step.setCheckEventFunction(r -> {
			return r.get(BookStockResult.class).isResult();
		});

		//本の在庫サービスのロールバック呼び出しロジック
		step.setCreateCompleteEvent(p -> {
			MessageCall messageCall = new MessageCall();
			messageCall.setTopicName("book-stock-rollback");
			messageCall.setSendEvent(p.getTransactionId() + "-" + i);
			return messageCall;
		});

		//本の在庫サービスの完了の呼び出しロジック
		step.setCreateCompleteEvent(p -> {
			MessageCall messageCall = new MessageCall();
			messageCall.setTopicName("book-stock-complete");
			messageCall.setSendEvent(createBookDto(p.getTransactionId() + "-" + i, book));
			return messageCall;
		});
		return step;
	}
*/
	private BookStockDto createBookDto(String transactionId, OrderBook book) {
		BookStockDto dto = new BookStockDto();
		dto.setTransactionId(transactionId);
		dto.setBookId(book.getBookId());
		dto.setReduceCount(book.getOrderCount());
		return dto;
	}

}
