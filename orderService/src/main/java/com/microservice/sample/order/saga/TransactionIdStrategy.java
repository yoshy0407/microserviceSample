package com.microservice.sample.order.saga;

/**
 * トランザクションIDを採番するStrategyパターンのインタフェースです
 * 
 * @author Hiroshi Yoshioka
 *
 */
public interface TransactionIdStrategy {

	/**
	 * トランザクションIDを取得します
	 * @return トランザクションID
	 */
	public String getNextVal();
}
