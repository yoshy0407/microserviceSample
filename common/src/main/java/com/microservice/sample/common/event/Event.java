package com.microservice.sample.common.event;


/**
 * 各サービスに渡すイベントのベースとなるクラスです
 * 
 * @author yoshy0407
 * @param <T> データとなるイベントオブジェクト
 *
 */
public class Event<T> {

	/**
	 * トランザクションID
	 */
	private String transactionId;
	
	private T event;

	/**
	 * @return transactionId
	 */
	public String getTransactionId() {
		return transactionId;
	}

	/**
	 * @param transactionId セットする transactionId
	 */
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	/**
	 * @return event
	 */
	public T getEvent() {
		return event;
	}

	/**
	 * @param event セットする event
	 */
	public void setEvent(T event) {
		this.event = event;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Event [transactionId=" + transactionId + ", event=" + event + "]";
	}
}
