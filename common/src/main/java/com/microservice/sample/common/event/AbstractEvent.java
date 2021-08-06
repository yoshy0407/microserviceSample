package com.microservice.sample.common.event;


/**
 * 各サービスに渡すイベントのベースとなるクラスです
 * 
 * @author yoshy0407
 * @param <T> データとなるイベントオブジェクト
 *
 */
public abstract class AbstractEvent {

	/**
	 * トランザクションID
	 */
	private String transactionId;
	
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

}
